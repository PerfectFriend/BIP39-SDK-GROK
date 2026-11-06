#!/usr/bin/env bash
# Build libbip39_engine_ffi.so for Android ABIs (with JNI)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUST="$ROOT/rust"
OUT="$ROOT/library/src/main/jniLibs"
TARGETS=("arm64-v8a" "armeabi-v7a" "x86_64")

echo "==> bip39-engine-ffi (android-jni) → $OUT"
if ! command -v cargo-ndk >/dev/null 2>&1; then
  cargo install cargo-ndk
fi
mkdir -p "$OUT"
cd "$RUST"
for abi in "${TARGETS[@]}"; do
  echo "--- $abi"
  cargo ndk -t "$abi" -o "$OUT" build -p bip39-engine-ffi --release --features android-jni
done
echo "==> done"
find "$OUT" -name "*.so" -type f 2>/dev/null || true
