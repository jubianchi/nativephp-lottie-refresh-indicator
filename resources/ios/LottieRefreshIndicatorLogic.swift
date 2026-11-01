import Foundation

/// The animation the indicator plays: `dark_asset` in dark mode when given,
/// `asset` otherwise; none without an `asset`.
func lottieRefreshIndicatorAsset(_ options: [String: Any], dark: Bool) -> String? {
    guard let asset = options["asset"] as? String, !asset.isEmpty else { return nil }
    if dark, let darkAsset = options["dark_asset"] as? String, !darkAsset.isEmpty {
        return darkAsset
    }
    return asset
}

/// An asset path, such as `animations/splash.lottie`, as a resource of the
/// app bundle.
struct LottieRefreshIndicatorResource: Equatable {
    let name: String
    let fileExtension: String
    let subdirectory: String?

    var isDotLottie: Bool { fileExtension == "lottie" }

    init(asset: String) {
        let path = asset as NSString
        let directory = path.deletingLastPathComponent.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let fileName = path.lastPathComponent as NSString
        name = fileName.deletingPathExtension
        fileExtension = fileName.pathExtension.isEmpty ? "lottie" : fileName.pathExtension
        subdirectory = directory.isEmpty ? nil : directory
    }

    /// The file in its own directory or, since the app's Xcode project
    /// copies resources flat, at the bundle's root.
    func url(in bundle: Bundle) -> URL? {
        if let subdirectory, let url = bundle.url(forResource: name, withExtension: fileExtension, subdirectory: subdirectory) {
            return url
        }
        return bundle.url(forResource: name, withExtension: fileExtension)
    }
}

struct LottieRefreshSegment: Equatable {
    let from: Double
    let to: Double
    let mode: Mode

    enum Mode: Equatable {
        case once
        case loop
        case autoReverse
    }
}

/// The parts of the animation, as progress ranges: `pull` plays as the list
/// is pulled, `refresh` loops forward while it refreshes. Without a refresh
/// segment, the refresh plays the pull segment back and forth instead.
struct LottieRefreshSegments: Equatable {
    let pull: ClosedRange<Double>
    let refresh: ClosedRange<Double>?

    static let whole = LottieRefreshSegments(pull: 0...1, refresh: nil)

    /// The segments the `pull` and `refresh` markers give, either missing:
    /// no `pull` marker leaves what comes before `refresh` to the pull, or
    /// the whole animation when nothing does. Empty markers count as missing.
    static func markers(pull: ClosedRange<Double>?, refresh: ClosedRange<Double>?) -> LottieRefreshSegments {
        let refreshSegment = refresh.flatMap { $0.isEmptySegment ? nil : $0 }
        let pullSegment = pull.flatMap { $0.isEmptySegment ? nil : $0 }
            ?? refreshSegment.flatMap { $0.lowerBound > 0 ? 0...$0.lowerBound : nil }
            ?? 0...1
        return LottieRefreshSegments(pull: pullSegment, refresh: refreshSegment)
    }

    func pullProgress(_ pull: Double) -> Double {
        self.pull.lowerBound + min(max(pull, 0), 1) * (self.pull.upperBound - self.pull.lowerBound)
    }
}

private extension ClosedRange where Bound == Double {
    var isEmptySegment: Bool { upperBound <= lowerBound }
}

/// What the animation does as the list reports its pull: it follows the
/// pull over the pull segment, then loops while refreshing, starting where
/// the pull left it since the list reports a progress of 1 once
/// refreshing. A loop is a chain of segments, each starting where the
/// previous one ends, the last one repeating.
struct LottieRefreshPlayback {
    enum Step: Equatable {
        case show(Double)
        case loop([LottieRefreshSegment])
        case keep
    }

    private var shown: Double?
    private var looping = false

    mutating func update(progress: Double, isRefreshing: Bool, segments: LottieRefreshSegments = .whole) -> Step {
        let pull = segments.pullProgress(progress)
        guard isRefreshing else {
            looping = false
            shown = pull
            return .show(pull)
        }
        guard !looping else { return .keep }
        looping = true
        return .loop(Self.loop(from: shown ?? pull, segments: segments))
    }

    /// The refresh from where the pull left the animation, never through a
    /// jump: up to the refresh segment, then that segment in a forward loop.
    /// Without a refresh segment, the pull segment plays back and forth
    /// instead, since looping it forward would wrap to its start, blank for
    /// a draw-in logo.
    private static func loop(from start: Double, segments: LottieRefreshSegments) -> [LottieRefreshSegment] {
        guard let refresh = segments.refresh else {
            let pull = segments.pull
            let backAndForth = LottieRefreshSegment(from: pull.upperBound, to: pull.lowerBound, mode: .autoReverse)
            guard start < pull.upperBound else { return [backAndForth] }
            return [LottieRefreshSegment(from: start, to: pull.upperBound, mode: .once), backAndForth]
        }

        let forward = LottieRefreshSegment(from: refresh.lowerBound, to: refresh.upperBound, mode: .loop)
        if start < refresh.lowerBound {
            return [LottieRefreshSegment(from: start, to: refresh.lowerBound, mode: .once), forward]
        }
        if start > refresh.lowerBound && start < refresh.upperBound {
            return [LottieRefreshSegment(from: start, to: refresh.upperBound, mode: .once), forward]
        }
        return [forward]
    }
}
