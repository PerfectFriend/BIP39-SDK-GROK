# Sync status — BIP39-SDK-GROK (main)

**HEAD after this wave:** see latest commits

## On GitHub now (2026-11-19)

- `docs/VAULT_FORMAT.md` ✅
- `docs/EVOLUTION_UNIFIED.md` ✅
- `BiometricPinCache.kt` ✅
- **StandardKeyboard.kt** ✅ (just pushed)
- **MnemonicWordEntry.kt** ✅
- **PassphraseScreen.kt** ✅ (updated with SecureShell + in-app kbd)
- Keyboard module nearly complete (all except Keys.kt rendering helpers)
- VaultV1, VaultCrypto, kdf.rs, most engine ✅

## Last remaining

| Path | Notes |
|------|-------|
| `keyboard/.../Keys.kt` | ~22 KB — TapKey, CharKeyButton, BackspaceKey, SpaceKey, AltStrip |

One more push and the auditor tree is complete.
