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
        }

        switch coordinator.playback.update(progress: progress, isRefreshing: isRefreshing) {
        case .show(let pull):
            view.currentProgress = pull
        case .loop(let from) where from >= 1:
            view.play(fromProgress: 0, toProgress: 1, loopMode: .loop)
        case .loop(let from):
            view.play(fromProgress: from, toProgress: 1, loopMode: .playOnce) { [weak view] finished in
                guard finished else { return }
                view?.play(fromProgress: 0, toProgress: 1, loopMode: .loop)
            }
        case .keep:
            break
        }
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
