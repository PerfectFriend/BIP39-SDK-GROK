#!/usr/bin/env bash
# Build libbip39_engine_ffi.so for Android ABIs (with JNI).
# Requires: rustup targets, Android NDK, cargo-ndk.
# Optional env:
#   ANDROID_NDK_HOME  — NDK root
#   RUSTFLAGS         — extra flags (CI sets 16KB max-page-size)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUST="$ROOT/rust"
OUT="$ROOT/library/src/main/jniLibs"
TARGETS=("arm64-v8a" "armeabi-v7a" "x86_64")

echo "==> bip39-engine-ffi (android-jni) → $OUT"
if ! command -v cargo >/dev/null 2>&1; then
  echo "error: cargo not found" >&2
  exit 1
fi
if ! command -v cargo-ndk >/dev/null 2>&1; then
  echo "==> installing cargo-ndk"
  cargo install cargo-ndk --locked
fi

# Play / targetSdk 35+: 16 KB page size (audit P1-10d)
export RUSTFLAGS="${RUSTFLAGS:--C link-arg=-Wl,-z,max-page-size=16384}"

mkdir -p "$OUT"
cd "$RUST"
for abi in "${TARGETS[@]}"; do
  echo "--- $abi"
  cargo ndk -t "$abi" -o "$OUT" build -p bip39-engine-ffi --release --features android-jni
done

echo "==> done"
mapfile -t SOS < <(find "$OUT" -name 'libbip39_engine_ffi.so' -type f 2>/dev/null | sort)
if [[ ${#SOS[@]} -lt 3 ]]; then
  echo "error: expected 3 ABI .so files, found ${#SOS[@]}" >&2
  find "$OUT" -type f 2>/dev/null || true
  exit 1
fi
for f in "${SOS[@]}"; do
  ls -la "$f"
done
