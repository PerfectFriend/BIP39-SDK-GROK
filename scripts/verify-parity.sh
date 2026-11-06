#!/usr/bin/env bash
# Phase C.3 — shared golden vector checks (host Rust + docs)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Rust unit tests (golden abandon prefix)"
(cd rust && cargo test -p bip39-engine abandon_vector_seed_prefix -- --nocapture)

echo
echo "Shared golden (must match Kotlin GoldenVectorTest / Rust):"
echo "  mnemonic: abandon … about (12 words)"
echo "  seed[0..8] hex: 5eb00bbddcf06908"
echo
echo "On device (with jniLibs):"
echo "  ./gradlew :library:connectedDebugAndroidTest"
echo "  → EngineParityInstrumentedTest"
