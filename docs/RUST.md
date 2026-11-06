# Rust core (Phase C)

## Layout

```text
rust/
  Cargo.toml                 # workspace
  bip39-engine/              # pure Rust BIP39
  bip39-engine-ffi/          # FFI façade + bip39_engine.udl (UniFFI)
  README.md
```

## API (`bip39-engine`)

| Function | Description |
|----------|-------------|
| `generate_mnemonic(Strength)` | 12 or 24 English words |
| `validate_mnemonic(phrase)` | checksum + wordlist |
| `mnemonic_to_seed(phrase, passphrase)` | 64-byte BIP39 seed |
| `fingerprint_seed_prefix(phrase)` | first 8 bytes of seed as hex (matches Kotlin) |

## Kotlin bridge

```kotlin
interface MnemonicEngine { ... }

KotlinMnemonicEngine()     // default — Bip39Manager
RustMnemonicEngine()       // after libbip39_engine_ffi is packaged
MnemonicEngines.current    // process-wide selection
MnemonicEngines.useRustIfAvailable()
```

Until `jniLibs` ships, **default remains Kotlin**. Hosts must not assume Rust is loaded.

## Build (developer machine / CI)

```bash
cd rust
cargo test -p bip39-engine

# Android (example)
cargo install cargo-ndk
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 \
  -o ../library/src/main/jniLibs \
  build -p bip39-engine-ffi --release
```

Then implement UniFFI bindings from `bip39-engine-ffi/src/bip39_engine.udl` and wire `RustMnemonicEngine` JNI calls.

## What stays on Android

- Keystore / Tink / PIN KDF  
- Biometric CryptoObject  
- Compose UI, FLAG_SECURE, PDF/QR  

## Status

| Item | State |
|------|--------|
| Pure Rust crate + unit tests (source) | done |
| UniFFI UDL draft | done |
| Kotlin `MnemonicEngine` | done |
| cargo-ndk / jniLibs in AAR | **done** (host build log 2026-11-06) |
| Parity tests C.3 | instrumented + JVM golden |
| Default engine = Rust | optional via `useRustIfAvailable()` |

**Note:** Restricted CI sandboxes may block `cargo` build-scripts; run `cargo test` on a normal dev host.


## C.2 Native packaging (Android)

### Build

```bash
# requires Android NDK + cargo-ndk
export ANDROID_NDK_HOME=/path/to/ndk
./scripts/build-rust-android.sh
```

Outputs:

```text
library/src/main/jniLibs/
  arm64-v8a/libbip39_engine_ffi.so
  armeabi-v7a/libbip39_engine_ffi.so
  x86_64/libbip39_engine_ffi.so
```

### JNI surface (`RustMnemonicEngine`)

| Kotlin | JNI / Rust |
|--------|------------|
| `nativeGenerate(12\|24)` | `Java_..._nativeGenerate` |
| `nativeValidate(phrase)` | `Java_..._nativeValidate` |
| `nativeToSeed(phrase, pass)` | `Java_..._nativeToSeed` |
| `nativeFingerprint(phrase)` | `Java_..._nativeFingerprint` |

Also exports a plain **C ABI** (`bip39_generate`, `bip39_validate`, …) for non-JVM hosts.

### Runtime selection

```kotlin
val usingRust = MnemonicEngines.useRustIfAvailable()
// false → KotlinMnemonicEngine (safe default)
```


## C.3 Parity (Kotlin ↔ Rust)

| Check | Where |
|-------|--------|
| seed prefix `5eb00bbddcf06908` | Rust `cargo test`, Kotlin `GoldenVectorTest` |
| validate / generate cross | `EngineParityInstrumentedTest` on device |
| passphrase TREZOR seed match | instrumented |

```bash
# host
./scripts/verify-parity.sh

# device/emulator with jniLibs present
./gradlew :library:connectedDebugAndroidTest
```


## Migration (C.4) — Rust-first

Architecture after refactor:

```text
App / UI / Bip39Manager
        │
        ▼
MnemonicEngines.current  ──preferRust()──► RustMnemonicEngine (JNI → bip39-engine)
        │ fallback
        ▼
KotlinMnemonicEngine → JvmBip39Core (zcash bip39)
```

- `Bip39Manager.generate|validate|toSeed|fingerprint` → active engine  
- Quiz helpers stay on JVM (`JvmBip39Core`)  
- Auto-init: first `OnboardingNavHost` / `PinUnlockScreen` / `Bip39Manager` call  
- Unit tests call `MnemonicEngines.useKotlin()` in `@Before`  
- Sample logs `MnemonicEngines.backend`

Still **not** in Rust: PIN KDF, Tink/Keystore, Compose, PDF/QR.
