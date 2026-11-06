# Multiplatform (Android + iOS)

```text
                 ┌─────────────────────┐
                 │   bip39-engine      │  Rust
                 │   (BIP39 crypto)    │
                 └──────────┬──────────┘
                            │
                 ┌──────────▼──────────┐
                 │ bip39-engine-ffi    │
                 │ C ABI + JNI feature │
                 └──────────┬──────────┘
              ┌─────────────┴─────────────┐
              ▼                           ▼
     Android library                 iOS Bip39SDK
     (Compose, Tink, JNI)            (SwiftUI, C ABI)
```

## Build

| Platform | Script |
|----------|--------|
| Android `.so` | `./scripts/build-rust-android.sh` (`--features android-jni`) |
| iOS XCFramework | `./scripts/build-rust-ios.sh` (macOS + Xcode) |

## Shared guarantees

- Same golden vector: `abandon…about` → seed prefix `5eb00bbddcf06908`
- English BIP39 wordlist
- Fingerprint = first 8 bytes of seed (hex)

## Status

| Area | Android | iOS |
|------|---------|-----|
| Rust BIP39 | done | done (C ABI) |
| Welcome Aboard UI | done | skeleton |
| Full onboarding (PIN, quiz, PDF) | done | partial (extend) |
| Theme presets ×20 | done | colors injectable |

## Product name

**BIP39-SDK-GROK** — one repo, two mobile shells, one Rust core.
