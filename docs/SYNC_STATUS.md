# Sync status (2026-11-19)

## Problem

U0–U2 was developed in the agent sandbox. Only **partial** files were pushed to GitHub via API (size limits). The auditor correctly saw a broken/incomplete tree.

## What is on GitHub now (partial)

Present after incremental sync commits:

- `WordListValidator.kt`, `KeyDerivationKit.kt`, `Hkdf.kt`
- `VaultRepository.kt`, `VaultError.kt` (via earlier commits), `RustNativeKdf.kt`
- `SecureOnboardingInput.kt`, `Bip39Features`, bridges
- `docs/AUDIT_BRIEF_RECHECK.md`

**Still may be missing on GitHub** (use local zip / force-sync):

- `VaultV1.kt`, `VaultCrypto.kt`
- Full `:keyboard` module sources
- `docs/VAULT_FORMAT.md`, `docs/EVOLUTION_UNIFIED.md`
- `rust/.../kdf.rs` and related

## Source of truth

Local / package:

- `BIP39-SDK-GROK-FULL-SYNC.zip` (artifacts)
- Tree: `bip39-onboarding-android/`

## Full force-sync (required before re-audit)

On a machine with `gh` + `git` authenticated as PerfectFriend:

```bash
unzip BIP39-SDK-GROK-FULL-SYNC.zip
cd bip39-onboarding-android
bash scripts/force-sync-to-github.sh
```

Or manually:

```bash
gh repo clone PerfectFriend/BIP39-SDK-GROK
cd BIP39-SDK-GROK
# replace tree with zip contents (keep .git)
git add -A
git commit -m "sync: full local U0-U2 tree"
git push origin main
```

Record the resulting commit SHA and give **that** to the auditor with `docs/AUDIT_BRIEF_RECHECK.md`.

## Do not re-audit until

```bash
curl -sI https://raw.githubusercontent.com/PerfectFriend/BIP39-SDK-GROK/main/library/src/main/java/com/example/bip39onboarding/vault/VaultV1.kt | head -1
# expect HTTP/2 200
curl -sI https://raw.githubusercontent.com/PerfectFriend/BIP39-SDK-GROK/main/keyboard/build.gradle.kts | head -1
# expect 200
curl -sI https://raw.githubusercontent.com/PerfectFriend/BIP39-SDK-GROK/main/docs/VAULT_FORMAT.md | head -1
# expect 200
```
