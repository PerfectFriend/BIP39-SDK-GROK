package com.example.bip39onboarding

import com.example.bip39onboarding.engine.MnemonicEngines
import org.junit.Before

import com.example.bip39onboarding.engine.KotlinMnemonicEngine
import com.example.bip39onboarding.engine.MnemonicEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase C.3 — JVM-side engine contract (Kotlin implementation). */
class MnemonicEngineTest {

    @Before
    fun forceKotlinEngine() {
        MnemonicEngines.useKotlin()
    }


    private val engine: MnemonicEngine = KotlinMnemonicEngine()
    private val abandon12 =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    @Test
    fun kotlinEngine_abandonFingerprint() {
        val m = abandon12.toCharArray()
        assertTrue(engine.validate(m))
        assertEquals("5eb00bbddcf06908", engine.fingerprint(m))
        engine.clear(m)
    }

    @Test
    fun kotlinEngine_generateValid() {
        val m = engine.generate(MnemonicEngine.Strength.WORDS_12)
        assertTrue(engine.validate(m))
        assertEquals(12, String(m).split(" ").size)
        engine.clear(m)
    }
}
