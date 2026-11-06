#!/usr/bin/env bash
# Build static lib + XCFramework for iOS (requires macOS + Xcode + cargo)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUST="$ROOT/rust"
OUT="$ROOT/ios/Frameworks"
TARGET_DIR="${CARGO_TARGET_DIR:-$RUST/target}"

echo "==> bip39-engine-ffi → iOS XCFramework"
command -v cargo >/dev/null || { echo "cargo required"; exit 1; }
command -v xcodebuild >/dev/null || { echo "xcodebuild required (macOS)"; exit 1; }

rustup target add aarch64-apple-ios aarch64-apple-ios-sim x86_64-apple-ios 2>/dev/null || true

cd "$RUST"
# device
cargo build -p bip39-engine-ffi --release --target aarch64-apple-ios
# simulator (Apple Silicon)
cargo build -p bip39-engine-ffi --release --target aarch64-apple-ios-sim || true
# simulator (Intel)
cargo build -p bip39-engine-ffi --release --target x86_64-apple-ios || true

DEVICE="$TARGET_DIR/aarch64-apple-ios/release/libbip39_engine_ffi.a"
SIM_ARM="$TARGET_DIR/aarch64-apple-ios-sim/release/libbip39_engine_ffi.a"
SIM_X86="$TARGET_DIR/x86_64-apple-ios/release/libbip39_engine_ffi.a"

mkdir -p "$OUT/include" "$OUT/build"
cp "$ROOT/ios/Sources/Bip39SDK/bip39_engine.h" "$OUT/include/"

# Merge sim libs if both exist
SIM_LIB=""
if [[ -f "$SIM_ARM" && -f "$SIM_X86" ]]; then
  lipo -create "$SIM_ARM" "$SIM_X86" -output "$OUT/build/libbip39_engine_ffi-sim.a"
  SIM_LIB="$OUT/build/libbip39_engine_ffi-sim.a"
elif [[ -f "$SIM_ARM" ]]; then
  SIM_LIB="$SIM_ARM"
elif [[ -f "$SIM_X86" ]]; then
  SIM_LIB="$SIM_X86"
fi

rm -rf "$OUT/Bip39Engine.xcframework"
if [[ -n "$SIM_LIB" && -f "$DEVICE" ]]; then
  xcodebuild -create-xcframework \
    -library "$DEVICE" -headers "$OUT/include" \
    -library "$SIM_LIB" -headers "$OUT/include" \
    -output "$OUT/Bip39Engine.xcframework"
  echo "OK $OUT/Bip39Engine.xcframework"
elif [[ -f "$DEVICE" ]]; then
  xcodebuild -create-xcframework \
    -library "$DEVICE" -headers "$OUT/include" \
    -output "$OUT/Bip39Engine.xcframework"
  echo "OK device-only XCFramework"
else
  echo "FAIL: device static lib missing"
  exit 1
fi
