import Foundation
import Testing
@testable import LottieRefreshIndicatorLogic

struct LottieRefreshIndicatorAssetTests {
    let options: [String: Any] = ["asset": "animations/splash.lottie", "dark_asset": "animations/splash-dark.lottie"]

    @Test func theLightModeShowsTheAsset() {
        #expect(lottieRefreshIndicatorAsset(options, dark: false) == "animations/splash.lottie")
    }

    @Test func theDarkModeShowsTheDarkAsset() {
        #expect(lottieRefreshIndicatorAsset(options, dark: true) == "animations/splash-dark.lottie")
    }

    @Test func theDarkModeFallsBackToTheAssetWithoutADarkOne() {
        #expect(lottieRefreshIndicatorAsset(["asset": "a.lottie"], dark: true) == "a.lottie")
        #expect(lottieRefreshIndicatorAsset(["asset": "a.lottie", "dark_asset": ""], dark: true) == "a.lottie")
        #expect(lottieRefreshIndicatorAsset(["asset": "a.lottie", "dark_asset": 3], dark: true) == "a.lottie")
    }

    @Test func thereIsNothingToShowWithoutAnAsset() {
        #expect(lottieRefreshIndicatorAsset([:], dark: false) == nil)
        #expect(lottieRefreshIndicatorAsset(["asset": ""], dark: false) == nil)
        #expect(lottieRefreshIndicatorAsset(["asset": NSNull()], dark: false) == nil)
        #expect(lottieRefreshIndicatorAsset(["asset": ["a.lottie"]], dark: false) == nil)
        #expect(lottieRefreshIndicatorAsset(["dark_asset": "dark.lottie"], dark: true) == nil)
    }
}

struct LottieRefreshIndicatorResourceTests {
    @Test func splitsAnAssetPathIntoABundleResource() {
        let resource = LottieRefreshIndicatorResource(asset: "animations/splash.lottie")

        #expect(resource.name == "splash")
        #expect(resource.fileExtension == "lottie")
        #expect(resource.subdirectory == "animations")
        #expect(resource.isDotLottie)
    }

    @Test func readsAnAssetAtTheRootOrDeeplyNested() {
        #expect(LottieRefreshIndicatorResource(asset: "splash.lottie").subdirectory == nil)
        #expect(LottieRefreshIndicatorResource(asset: "/splash.lottie").subdirectory == nil)
        #expect(LottieRefreshIndicatorResource(asset: "a/b/splash.lottie").subdirectory == "a/b")
    }

    @Test func readsAJsonAnimation() {
        let resource = LottieRefreshIndicatorResource(asset: "animations/splash.json")

        #expect(resource.name == "splash")
        #expect(resource.fileExtension == "json")
        #expect(!resource.isDotLottie)
    }

    @Test func takesAnAssetWithoutExtensionForADotLottieFile() {
        let resource = LottieRefreshIndicatorResource(asset: "animations/splash")

        #expect(resource.name == "splash")
        #expect(resource.fileExtension == "lottie")
    }

    @Test func findsTheFileWhereTheAppBuildFlattensIt() throws {
        let bundle = try temporaryBundle(files: ["splash.lottie"])

        #expect(LottieRefreshIndicatorResource(asset: "animations/splash.lottie").url(in: bundle)?.lastPathComponent == "splash.lottie")
    }

    @Test func prefersTheFileInItsOwnDirectory() throws {
        let bundle = try temporaryBundle(files: ["splash.lottie", "animations/splash.lottie"])

        let url = LottieRefreshIndicatorResource(asset: "animations/splash.lottie").url(in: bundle)
        #expect(url?.deletingLastPathComponent().lastPathComponent == "animations")
    }

    @Test func findsNothingForAMissingFile() throws {
        let bundle = try temporaryBundle(files: ["other.lottie"])

        #expect(LottieRefreshIndicatorResource(asset: "animations/splash.lottie").url(in: bundle) == nil)
    }

    private func temporaryBundle(files: [String]) throws -> Bundle {
        let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        for file in files {
            let url = root.appendingPathComponent(file)
            try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
            try Data().write(to: url)
        }
        return try #require(Bundle(url: root))
    }
}

struct LottieRefreshPlaybackTests {
    @Test func thePullDrivesTheAnimation() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: 0.4, isRefreshing: false) == .show(0.4))
    }

    @Test func thePullIsClamped() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: -0.2, isRefreshing: false) == .show(0))
        #expect(playback.update(progress: 1.6, isRefreshing: false) == .show(1))
    }

    @Test func theLoopStartsWhereThePullLeftTheAnimation() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 0.9, isRefreshing: false)

        #expect(playback.update(progress: 1, isRefreshing: true) == .loop(from: 0.9))
    }

    @Test func theLoopStartsOnce() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false)
        _ = playback.update(progress: 1, isRefreshing: true)

        #expect(playback.update(progress: 1, isRefreshing: true) == .keep)
    }

    @Test func aRefreshShownWithoutAPullLoopsFromTheGivenProgress() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: 1, isRefreshing: true) == .loop(from: 1))
    }

    @Test func thePullTakesOverWhenTheRefreshEnds() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false)
        _ = playback.update(progress: 1, isRefreshing: true)

        #expect(playback.update(progress: 0.6, isRefreshing: false) == .show(0.6))
        #expect(playback.update(progress: 1, isRefreshing: true) == .loop(from: 0.6))
    }
}
