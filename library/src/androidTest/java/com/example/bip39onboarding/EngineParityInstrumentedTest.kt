package com.example.bip39onboarding

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bip39onboarding.engine.KotlinMnemonicEngine
import com.example.bip39onboarding.engine.MnemonicEngine
import com.example.bip39onboarding.engine.RustMnemonicEngine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase C.3 — Kotlin ↔ Rust parity on device/emulator (requires jniLibs).
 *
 *   ./gradlew :library:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class EngineParityInstrumentedTest {

    private val abandon12 =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    @Test
    fun nativeLibraryLoads() {
        assumeTrue("libbip39_engine_ffi.so not packaged", RustMnemonicEngine.tryLoadNative())
        assertTrue(RustMnemonicEngine.nativeAvailable)
    }

    @Test
    fun abandon_seed_and_fingerprint_match() {
        assumeTrue("libbip39_engine_ffi.so not packaged", RustMnemonicEngine.tryLoadNative())
        val rust = RustMnemonicEngine.createOrNull()!!
        val kotlin = KotlinMnemonicEngine()
        val m = abandon12.toCharArray()

        assertTrue(kotlin.validate(m))
        assertTrue(rust.validate(m))

        val ks = kotlin.toSeed(m, charArrayOf())
        val rs = rust.toSeed(m, charArrayOf())
        assertArrayEquals(ks, rs)
        assertEquals(64, ks.size)

        assertEquals(kotlin.fingerprint(m), rust.fingerprint(m))
        assertEquals("5eb00bbddcf06908", kotlin.fingerprint(m))

        kotlin.clear(m)
        kotlin.clear(ks)
        rust.clear(rs)
    }

    @Test
    fun passphrase_seed_match() {
        assumeTrue("libbip39_engine_ffi.so not packaged", RustMnemonicEngine.tryLoadNative())
        val rust = RustMnemonicEngine.createOrNull()!!
        val kotlin = KotlinMnemonicEngine()
        val m = abandon12.toCharArray()
        val pp = "TREZOR".toCharArray()

        val ks = kotlin.toSeed(m, pp)
        val rs = rust.toSeed(m, pp)
        assertArrayEquals(ks, rs)

        kotlin.clear(m)
        kotlin.clear(pp)
        kotlin.clear(ks)
        rust.clear(rs)
    }

    @Test
    fun generate_rust_validates_on_kotlin() {
        assumeTrue("libbip39_engine_ffi.so not packaged", RustMnemonicEngine.tryLoadNative())
        val rust = RustMnemonicEngine.createOrNull()!!
        val kotlin = KotlinMnemonicEngine()

        val m12 = rust.generate(MnemonicEngine.Strength.WORDS_12)
        val m24 = rust.generate(MnemonicEngine.Strength.WORDS_24)
        assertTrue(kotlin.validate(m12))
        assertTrue(kotlin.validate(m24))
        assertEquals(12, String(m12).split(" ").size)
        assertEquals(24, String(m24).split(" ").size)

        rust.clear(m12)
        rust.clear(m24)
    }
}
