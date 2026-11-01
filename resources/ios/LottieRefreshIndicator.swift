import Lottie
import SwiftUI
import UIKit

/// Registers the `lottie` pull-to-refresh indicator with mobile-ui. Declared
/// as the plugin's `ios.init_function`, which core calls before the first
/// render.
func registerLottieRefreshIndicator() {
    NativeUIRefreshIndicators.shared.register("lottie") { scope in
        AnyView(LottieRefreshIndicatorView(scope: scope))
    }
}

/// Plays the Lottie animation of the app bundle the options name: it draws
/// in as the list is pulled and loops while refreshing.
struct LottieRefreshIndicatorView: View {
    let scope: NativeUIRefreshIndicatorScope

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        if let asset = lottieRefreshIndicatorAsset(scope.options, dark: colorScheme == .dark) {
            LottieRefreshAnimation(asset: asset, progress: scope.progress, isRefreshing: scope.isRefreshing)
        }
    }
}

struct LottieRefreshAnimation: UIViewRepresentable {
    let asset: String
    let progress: Double
    let isRefreshing: Bool

    final class Coordinator {
        var asset: String?
        var segments = LottieRefreshSegments.whole
        var playback = LottieRefreshPlayback()
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> LottieAnimationView {
        let view = LottieAnimationView()
        view.contentMode = .scaleAspectFit
        view.backgroundBehavior = .pauseAndRestore
        for axis in [NSLayoutConstraint.Axis.horizontal, .vertical] {
            view.setContentHuggingPriority(.defaultLow, for: axis)
            view.setContentCompressionResistancePriority(.defaultLow, for: axis)
        }
        return view
    }

    func sizeThatFits(_ proposal: ProposedViewSize, uiView: LottieAnimationView, context: Context) -> CGSize? {
        proposal.replacingUnspecifiedDimensions()
    }

    func updateUIView(_ view: LottieAnimationView, context: Context) {
        let coordinator = context.coordinator
        if coordinator.asset != asset {
            coordinator.asset = asset
            coordinator.playback = LottieRefreshPlayback()
            Self.load(asset, into: view)
            coordinator.segments = Self.segments(of: view.animation)
        }

        switch coordinator.playback.update(progress: progress, isRefreshing: isRefreshing, segments: coordinator.segments) {
        case .show(let pull):
            view.currentProgress = pull
        case .loop(let segments):
            Self.play(segments[...], on: view)
        case .keep:
            break
        }
    }

    private static func play(_ segments: ArraySlice<LottieRefreshSegment>, on view: LottieAnimationView) {
        guard let segment = segments.first else { return }
        view.play(
            fromProgress: segment.from,
            toProgress: segment.to,
            loopMode: loopMode(segment.mode)
        ) { [weak view] finished in
            guard finished, let view else { return }
            play(segments.dropFirst(), on: view)
        }
    }

    private static func loopMode(_ mode: LottieRefreshSegment.Mode) -> LottieLoopMode {
        switch mode {
        case .once: .playOnce
        case .loop: .loop
        case .autoReverse: .autoReverse
        }
    }

    /// The segments the animation's `pull` and `refresh` markers give.
    private static func segments(of animation: LottieAnimation?) -> LottieRefreshSegments {
        guard let animation else { return .whole }
        func range(_ marker: String) -> ClosedRange<Double>? {
            guard let start = animation.frameTime(forMarker: marker),
                  let duration = animation.durationFrameTime(forMarker: marker) else { return nil }
            let lower = Double(animation.progressTime(forFrame: start))
            let upper = Double(animation.progressTime(forFrame: start + duration))
            return upper >= lower ? lower...upper : nil
        }
        return .markers(pull: range("pull"), refresh: range("refresh"))
    }

    /// Loads synchronously, from Lottie's cache after the first pull: the
    /// list rebuilds the indicator for every pull, and an asynchronous load
    /// would leave it empty for its first frames each time.
    private static func load(_ asset: String, into view: LottieAnimationView) {
        let resource = LottieRefreshIndicatorResource(asset: asset)
        guard let url = resource.url(in: .main) else {
            view.animation = nil
            return
        }

        if !resource.isDotLottie {
            view.animation = LottieAnimation.filepath(url.path)
        } else if case .success(let file) = DotLottieFile.SynchronouslyBlockingCurrentThread.loadedFrom(filepath: url.path) {
            view.loadAnimation(from: file)
        } else {
            view.animation = nil
        }
    }
}
