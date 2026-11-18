package com.example.bip39onboarding.vault

sealed class VaultError : Exception() {
    object WrongPin : VaultError()
    object Panic : VaultError()
    object Corrupted : VaultError()
    object WrongPassphrase : VaultError()
    object NotSetup : VaultError()
    object KeyLost : VaultError()
    data class Other(override val message: String) : VaultError()
}
