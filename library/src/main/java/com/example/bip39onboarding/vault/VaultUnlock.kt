package com.example.bip39onboarding.vault

import android.content.Context
import com.example.bip39onboarding.data.UnlockResult
import com.example.bip39onboarding.security.PinUnlock
import com.example.bip39onboarding.security.SeedStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Prefer vault v1; fall back to legacy; migrate legacy → v1 on successful unlock.
 */
object VaultUnlock {

    fun tryUnlock(
        context: Context,
        pin: CharArray,
        legacy: SeedStorage?,
        passphrase: CharArray = charArrayOf(),
        migrateLegacy: Boolean = true
    ): UnlockResult {
        val repo = VaultRepository(context)
        if (repo.isSetup()) {
            return unlockV1(repo, pin, passphrase)
        }

        if (legacy == null || !legacy.isSetup()) return UnlockResult.NotSetup

        val legacyResult = PinUnlock.tryUnlock(pin, legacy)
        if (legacyResult is UnlockResult.Success && migrateLegacy) {
            try {
                repo.create(
                    mnemonic = legacyResult.mnemonic,
                    pin = pin,
                    panicPin = charArrayOf(),
                    passphrase = passphrase,
                    wordCount = legacy.getWordCount()
                )
                legacy.wipe()
            } catch (_: Exception) {
                // Keep legacy if migration fails; still return success
            }
        }
        return legacyResult
    }

    suspend fun tryUnlockAsync(
        context: Context,
        pin: CharArray,
        legacy: SeedStorage?,
        passphrase: CharArray = charArrayOf(),
        migrateLegacy: Boolean = true
    ): UnlockResult = withContext(Dispatchers.Default) {
        tryUnlock(context, pin, legacy, passphrase, migrateLegacy)
    }

    private fun unlockV1(
        repo: VaultRepository,
        pin: CharArray,
        passphrase: CharArray
    ): UnlockResult {
        return try {
            val opened = repo.unlock(pin, passphrase)
            UnlockResult.Success(opened.mnemonic)
        } catch (_: VaultError.Panic) {
            UnlockResult.Panic
        } catch (_: VaultError.WrongPin) {
            UnlockResult.WrongPin
        } catch (_: VaultError.WrongPassphrase) {
            UnlockResult.WrongPassphrase
        } catch (_: VaultError.KeyLost) {
            UnlockResult.KeyLost
        } catch (_: VaultError.Corrupted) {
            UnlockResult.Corrupted
        } catch (_: VaultError.NotSetup) {
            UnlockResult.NotSetup
        }
    }

    fun isAnySetup(context: Context, legacy: SeedStorage?): Boolean {
        if (VaultRepository(context).isSetup()) return true
        return legacy?.isSetup() == true
    }
}
