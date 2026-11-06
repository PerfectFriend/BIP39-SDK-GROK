// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Bip39SDK",
    platforms: [
        .iOS(.v16),
        .macOS(.v13)
    ],
    products: [
        .library(name: "Bip39SDK", targets: ["Bip39SDK"])
    ],
    targets: [
        // Link XCFramework produced by scripts/build-rust-ios.sh
        .binaryTarget(
            name: "Bip39Engine",
            path: "Frameworks/Bip39Engine.xcframework"
        ),
        .target(
            name: "Bip39SDK",
            dependencies: ["Bip39Engine"],
            path: "Sources/Bip39SDK",
            publicHeadersPath: ".",
            cSettings: [
                .headerSearchPath(".")
            ],
            linkerSettings: [
                .linkedLibrary("bip39_engine_ffi", .when(platforms: [.iOS, .macOS]))
            ]
        ),
        .testTarget(
            name: "Bip39SDKTests",
            dependencies: ["Bip39SDK"],
            path: "Tests/Bip39SDKTests"
        )
    ]
)
