# Audit status vs «BIP39-SDK-GROK — аудит и план до релиза 1.0 (2)»

**Date:** 2026-11-21  
**Source audit:** static review dated 2026-11-17 (+ §16 archive `_5`)  
**Working tree:** local `bip39-onboarding-android` / GitHub `PerfectFriend/BIP39-SDK-GROK`

## Blockers §16.1

| ID | Claim | Status now |
|----|--------|------------|
| B1 | `library` missing `:keyboard` | **Fixed** |
| B2 | Vault not wired | **Fixed** |
| B3 | No JNI for KDF | **Code fixed**; **CI builds `.so`** (job `rust-android-ndk`) |
| B4 | Vault tests | **Partial** — VaultV1Test expanded |
| B5 | SYNC overclaims | This file is source of truth |

## Session log

### 2026-11-21a
DevicePepper KeyLost, atomic vault write, NavHost NUL, 15/21-word tests.

### 2026-11-21b
Payload ownership, passphrase strings, sessionLockTouch, wrongPassphrase test.

### 2026-11-21c — native CI
- CI jobs: `android-unit`, `rust-host` (fmt/clippy/test), `rust-android-ndk` (NDK r28b → jniLibs artifact, 16KB pages).
- `build-rust-android.sh` fails if <3 ABIs.
- `RustNativeKdf.selfTest()` + preferRust best-effort probe.

## Still before 1.0 secure

1. Green CI on GitHub (watch `rust-android-ndk` artifact)
2. Commit jniLibs into repo **or** download artifact into release AAR pipeline
3. External audit (Колян)
4. Residual P1 polish / rename
