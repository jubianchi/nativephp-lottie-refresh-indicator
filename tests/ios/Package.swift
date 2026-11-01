// swift-tools-version:6.0
import PackageDescription

let package = Package(
    name: "LottieRefreshIndicatorLogic",
    platforms: [.macOS(.v15)],
    targets: [
        .target(name: "LottieRefreshIndicatorLogic", path: "Sources/LottieRefreshIndicatorLogic"),
        .testTarget(
            name: "LottieRefreshIndicatorLogicTests",
            dependencies: ["LottieRefreshIndicatorLogic"],
            path: "Tests/LottieRefreshIndicatorLogicTests"
        ),
    ],
    swiftLanguageModes: [.v5]
)
