#!/bin/zsh
# Usage: typecheck.sh <mobile-ui checkout> <mobile checkout> <Lottie.xcframework/ios-arm64_x86_64-simulator>
set -u
HERE=${0:A:h}
ROOT=${HERE:h:h:h}
UI=$1; CORE=$2/resources/xcode; LOTTIE=$3
SDK=$(xcrun -sdk iphonesimulator --show-sdk-path)
xcrun -sdk iphonesimulator swiftc -typecheck \
  -target arm64-apple-ios18.2-simulator -sdk $SDK -swift-version 5 -I $CORE/Include -F $LOTTIE \
  $(find $CORE/NativePHP -name '*.swift' -not -path '*Preview*' -not -name AppUpdateManager.swift) \
  $UI/resources/ios/*.swift $HERE/CoreStubs.swift $ROOT/resources/ios/*.swift 2>&1 | grep -E "error:" | grep -v "^ "
exit ${pipestatus[1]}
