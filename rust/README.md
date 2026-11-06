# bip39-engine (Rust) — Phase C

Portable BIP39 core shared by Android (and later iOS) onboarding UI.

## Crates

| Crate | Role |
|-------|------|
| `bip39-engine` | Pure Rust: generate / validate / seed / fingerprint |
| `bip39-engine-ffi` | FFI façade + UniFFI UDL (`bip39_engine.udl`) |

## Build & test

```bash
cd rust
cargo test -p bip39-engine
```

## Android integration (next slices)

1. Kotlin `MnemonicEngine` interface (done in library).
2. `KotlinMnemonicEngine` — current `Bip39Manager` (default).
3. `RustMnemonicEngine` — load `libbip39_engine_ffi.so` via UniFFI/JNI when packaged.
4. CI: `cargo ndk` → `jniLibs/arm64-v8a` etc.

## Security

- Use `zeroize` on entropy and seed buffers in Rust.
- Never log mnemonics.
- Platform Keystore / PIN KDF remain on Android (not in this crate yet).
