package com.example

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.trim().toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun sha256Hash_matchesPrecalculatedLabHash() {
        // Verifica que el cálculo SHA-256 sea determinista y coincida con el formato de 64 hex chars
        val hash = sha256("test")
        assertEquals(64, hash.length)
        assertEquals(SecurityManager.PRECALCULATED_LAB_HASH, hash)
    }

    @Test
    fun emergencyBrakeRule_activatesAtOrBelow15Cm() {
        val safetyThreshold = IoTViewModel.DISTANCIA_SEGURIDAD
        assertEquals(15, safetyThreshold)

        val distanceSafe = 16
        val distanceLimit = 15
        val distanceDanger = 8

        assertFalse(distanceSafe <= safetyThreshold)
        assertTrue(distanceLimit <= safetyThreshold)
        assertTrue(distanceDanger <= safetyThreshold)
    }
}
