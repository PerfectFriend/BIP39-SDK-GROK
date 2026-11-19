# Sync status — BIP39-SDK-GROK (main)

**Last MCP push wave:** 2026-11-19

## On GitHub now

- `docs/VAULT_FORMAT.md` ✅
- `docs/EVOLUTION_UNIFIED.md` ✅
- `library/.../vault/BiometricPinCache.kt` ✅
- Keyboard module (almost complete):
  - EditTextBridge, EmojiData, EmojiPanel, KeyIcons, KeyboardColors, KeyboardConfig, KeyboardLayout, KeyboardState, NumericKeypad, Security, StdKey, Suggestions, TextEditing, TextFieldHelpers ✅
- VaultV1, VaultCrypto, kdf.rs, most engine/UI ✅

## Still missing on GitHub (local complete)

| Path | Approx size | Notes |
|------|-------------|-------|
| `keyboard/src/main/java/com/narde/keyboard/StandardKeyboard.kt` | ~15 KB | Core Compose keyboard entrypoint |
| `keyboard/src/main/java/com/narde/keyboard/Keys.kt` | ~22 KB | TapKey / CharKeyButton / rendering |
| `library/.../ui/components/MnemonicWordEntry.kt` | ~8 KB | Word entry + SecureSeedKeyboard |
| `library/.../ui/components/PassphraseScreen.kt` | ~6 KB | Passphrase + in-app kbd |

These four files are the last gap for a complete auditor tree.

## How to finish (recommended)

On a machine with `gh` authenticated as PerfectFriend:

```bash
cd bip39-onboarding-android
bash scripts/force-sync-to-github.sh
```

Or unzip `artifacts/BIP39-SDK-GROK-FULL-SYNC.zip` and push the remaining four files.

After that, update this file with the new HEAD SHA and call the auditor.
