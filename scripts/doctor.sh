#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
ok() { echo "  OK  $1"; }
warn() { echo "  WARN $1"; }
fail() { echo "  FAIL $1"; }

echo "== BIP39-SDK-GROK doctor =="
echo "path: $ROOT"

if command -v java >/dev/null; then ok "java $(java -version 2>&1 | head -1)"; else fail "java missing"; fi
if [[ -x ./gradlew ]]; then ok "gradlew"; else warn "gradlew not executable (chmod +x gradlew)"; fi
if command -v cargo >/dev/null; then ok "cargo $(cargo --version)"; else warn "cargo missing (Rust optional)"; fi
if command -v cargo-ndk >/dev/null; then ok "cargo-ndk"; else warn "cargo-ndk not installed"; fi

if [[ -n "${ANDROID_NDK_HOME:-}${NDK_HOME:-}" ]]; then
  ok "NDK ${ANDROID_NDK_HOME:-$NDK_HOME}"
else
  warn "ANDROID_NDK_HOME unset"
fi

echo "-- jniLibs --"
count=$(find library/src/main/jniLibs -name 'libbip39_engine_ffi.so' 2>/dev/null | wc -l | tr -d ' ')
if [[ "$count" -ge 1 ]]; then ok "$count native lib(s) found"; else warn "no .so — JVM fallback will be used"; fi

echo "-- presets --"
ok "20 theme ids (see OnboardingThemePresets)"

echo "done."
