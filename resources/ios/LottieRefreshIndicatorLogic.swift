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

/// What the animation does as the list reports its pull: it follows the
/// pull, then loops while refreshing, starting where the pull left it since
/// the list reports a progress of 1 once refreshing. A loop is a chain of
/// segments, each starting where the previous one ends, the last one
/// playing back and forth.
struct LottieRefreshSegment: Equatable {
    let from: Double
    let to: Double
    let autoReverses: Bool
}

struct LottieRefreshPlayback {
    enum Step: Equatable {
        case show(Double)
        case loop([LottieRefreshSegment])
        case keep
    }

    private var shown: Double?
    private var looping = false

    mutating func update(progress: Double, isRefreshing: Bool) -> Step {
        let pull = min(max(progress, 0), 1)
        guard isRefreshing else {
            looping = false
            shown = pull
            return .show(pull)
        }
        guard !looping else { return .keep }
        looping = true
        return .loop(Self.loop(from: shown ?? pull))
    }

    /// Back and forth from where the pull left the animation, never through
    /// a jump: a release happens at the end of the pull, and wrapping from
    /// the end to the start would blank a draw-in logo.
    private static func loop(from start: Double) -> [LottieRefreshSegment] {
        let backAndForth = LottieRefreshSegment(from: 1, to: 0, autoReverses: true)
        guard start < 1 else { return [backAndForth] }
        return [LottieRefreshSegment(from: start, to: 1, autoReverses: false), backAndForth]
    }
}
