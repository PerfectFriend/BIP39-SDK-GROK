#!/usr/bin/env bash
# Unpack archive into ~/B39, then:
#   cd ~/B39 && bash RUN_IN_B39.sh
set -euo pipefail
cd "$(dirname "$0")"

echo "=============================================="
echo " BIP39 engine — Phase C.2 local check"
echo " pwd: $(pwd)"
echo "=============================================="

echo
echo "==> 1) Host Rust tests (bip39-engine)"
if ! command -v cargo >/dev/null; then
  echo "ERROR: cargo not found. Install Rust: https://rustup.rs"
  exit 1
fi
(cd rust && cargo test -p bip39-engine 2>&1) | tee /tmp/bip39-cargo-test.log
echo "cargo test log: /tmp/bip39-cargo-test.log"

echo
echo "==> 2) Android NDK + cargo-ndk → jniLibs"
if [[ -z "${ANDROID_NDK_HOME:-}" && -z "${NDK_HOME:-}" ]]; then
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}/ndk" ]]; then
    export ANDROID_NDK_HOME="$(ls -d "${ANDROID_HOME}"/ndk/* 2>/dev/null | sort -V | tail -1 || true)"
  fi
fi

if [[ -z "${ANDROID_NDK_HOME:-}${NDK_HOME:-}" ]]; then
  echo "SKIP native Android build: set ANDROID_NDK_HOME"
  echo "  export ANDROID_NDK_HOME=\$ANDROID_HOME/ndk/<version>"
  echo "  bash scripts/build-rust-android.sh"
  echo
  echo "Host tests finished. Paste this log + any cargo errors."
  exit 0
fi

echo "ANDROID_NDK_HOME=${ANDROID_NDK_HOME:-$NDK_HOME}"
bash scripts/build-rust-android.sh 2>&1 | tee /tmp/bip39-ndk-build.log
echo "ndk build log: /tmp/bip39-ndk-build.log"
echo
echo "==> jniLibs:"
find library/src/main/jniLibs -name '*.so' 2>/dev/null || echo "(no .so yet)"
echo
echo "Done. Send /tmp/bip39-cargo-test.log and /tmp/bip39-ndk-build.log (or terminal output)."
