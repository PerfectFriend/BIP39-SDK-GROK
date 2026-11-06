package com.example.bip39onboarding

import com.example.bip39onboarding.engine.MnemonicEngines
import org.junit.Before

import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.CryptoUtils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden / regression vectors for Phase B API freeze.
 */
class GoldenVectorTest {

    @Before
    fun forceKotlinEngine() {
        MnemonicEngines.useKotlin()
    }


    /** BIP39 test vector: 12× abandon + about */
    private val abandon12 =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    @Test
    fun bip39_abandon12_validates() {
        val m = abandon12.toCharArray()
        assertTrue(Bip39Manager.validate(m))
        Bip39Manager.clear(m)
    }

    @Test
    fun bip39_abandon12_seed_stable() {
        val m = abandon12.toCharArray()
        val seed1 = Bip39Manager.toSeed(m, charArrayOf())
        val seed2 = Bip39Manager.toSeed(m, charArrayOf())
        assertArrayEquals(seed1, seed2)
        assertEquals(64, seed1.size)
        // First 8 bytes of BIP39 seed for this vector (UTF-8 empty passphrase)
        val head = seed1.take(8).joinToString("") { "%02x".format(it) }
        // BIP39 published vector prefix (empty passphrase)
        assertEquals("5eb00bbddcf06908", head)
        Bip39Manager.clear(m)
        Bip39Manager.clear(seed1)
        Bip39Manager.clear(seed2)
    }

    @Test
    fun bip39_passphrase_changes_seed() {
        val m = abandon12.toCharArray()
        val a = Bip39Manager.toSeed(m, charArrayOf())
        val b = Bip39Manager.toSeed(m, "TREZOR".toCharArray())
        assertFalse(a.contentEquals(b))
        Bip39Manager.clear(m)
        Bip39Manager.clear(a)
        Bip39Manager.clear(b)
    }

    @Test
    fun crypto_encrypt_decrypt_roundtrip() {
        val m = abandon12.toCharArray()
        val pin = "847293".toCharArray()
        val payload = CryptoUtils.encrypt(m, pin)
        val unlocked = CryptoUtils.decrypt(payload, pin)
        assertNotNull(unlocked)
        assertTrue(unlocked!!.contentEquals(m))
        assertTrue(Bip39Manager.validate(unlocked))
        Bip39Manager.clear(m)
        Bip39Manager.clear(unlocked)
        Bip39Manager.clear(pin)
    }

    @Test
    fun crypto_wrong_pin_fails() {
        val m = abandon12.toCharArray()
        val payload = CryptoUtils.encrypt(m, "847293".toCharArray())
        val unlocked = CryptoUtils.decrypt(payload, "000000".toCharArray())
        assertEquals(null, unlocked)
        Bip39Manager.clear(m)
    }
}
