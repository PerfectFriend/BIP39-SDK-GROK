# Audit status vs «BIP39-SDK-GROK — аудит и план до релиза 1.0 (2)»

**Date:** 2026-11-21  
**Source audit:** static review dated 2026-11-17 (+ §16 archive `_5`)  
**Working tree:** local `bip39-onboarding-android` / GitHub `PerfectFriend/BIP39-SDK-GROK`

## How to read this

The audit is largely correct for the **archive it read**. This tree has already closed several blockers from §16.1–16.2. Below: audit claim → current status.

## Blockers §16.1

| ID | Claim | Status now |
|----|--------|------------|
| B1 | `library` missing `:keyboard` | **Fixed** — `api(project(":keyboard"))` in `library/build.gradle.kts` |
| B2 | Vault not wired to ViewModel / unlock | **Fixed** — `OnboardingViewModel.completeSetup` → `VaultRepository.create`; `OnboardingNavHost` injects `VaultRepository`; `PinUnlockScreen` → `VaultUnlock` |
| B3 | No JNI for KDF | **Fixed in tree** — `jni_bridge.rs` exports KDF natives; `RustNativeKdf.kt` binds them. **CI must still ship `.so`** |
| B4 | No vault tests | **Partial** — `VaultV1Test` (round-trip, wrong PIN, panic, 15/21-word). Spec resource dir optional |
| B5 | SYNC_STATUS overclaims Done | **Open** — this file is the source of truth |

## P0 map (product decisions applied)

| ID | Topic | Status |
|----|--------|--------|
| P0-1 | PIN hash = encryption key | **Remediated in vault path** — DEK/KEK in `VaultV1` |
| P0-2 | Offline PIN brute | Device pepper + `PinLockout` (`elapsedRealtime`) + wipe after N |
| P0-3 | Biometrics | **Disabled** — PIN-only product policy |
| P0-4 | Backup / KeyLost | `noBackupFilesDir`; hmac never auto-creates key |
| P0-5 | CharArray / String | Residual JVM risk; keyboard reduces IME leak |
| P0-6 | Fingerprint | Prefer HMAC domain fingerprint |
| P0-7 | Passphrase check | Per `VAULT_FORMAT.md` — verify always on |

## Fixes 2026-11-21

1. `DevicePepper`: no auto-create on hmac → real `KeyLost`
2. `VaultRepository.create`: ensureKey + atomic `*.tmp` rename
3. NavHost NUL byte removed
4. VaultV1Test: 15/21-word entropy body
5. Biometrics / QR / PDF defaults off (prior)

## Still before 1.0 secure

1. CI native `.so` + self-test
2. Passphrase pp-check UX
3. Residual P1 (payload, SecureWindow, clipboard)
4. External audit (Колян)
5. Rename when ready
