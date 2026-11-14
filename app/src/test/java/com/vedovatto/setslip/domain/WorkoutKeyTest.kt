package com.vedovatto.setslip.domain

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.data.decodeVariations
import com.vedovatto.setslip.data.encodeVariations
import com.vedovatto.setslip.domain.MuscleGroup.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutKeyTest {

    private val generator = WorkoutGenerator(Catalog.exercises)

    @Test fun `slip survives a key round trip`() {
        val w = generator.generate(1, listOf(BACK, TRICEPS), mapOf(BACK to 2))
        val restored = Workout.fromKey(9, w.key, Catalog.byId::get)
        assertEquals(w.sections, restored?.sections)
        assertEquals(w.variation, restored?.variation)
    }

    @Test fun `invalid key returns null`() {
        assertNull(Workout.fromKey(1, "garbage", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|CHEST|does-not-exist", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|FOO|bench-press", Catalog.byId::get))
    }

    @Test fun `malformed keys are rejected`() {
        assertNotNull(Workout.fromKey(1, keyOf(0, CHEST, BACK), Catalog.byId::get))
        assertNull(Workout.fromKey(1, keyOf(0, CHEST, BACK, BICEPS, TRICEPS), Catalog.byId::get))
        assertNull(Workout.fromKey(1, keyOf(0, CHEST, CHEST), Catalog.byId::get))
        assertNull(Workout.fromKey(1, keyOf(VARIATIONS, CHEST), Catalog.byId::get))
        assertNull(Workout.fromKey(1, keyOf(-1, CHEST), Catalog.byId::get))
        assertNull(Workout.fromKey(1, "A|CHEST|bench-press", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|CHEST|bench-press,does-not-exist", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|CHEST+BICEPS|bench-press", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|CHEST|bench-press|extra", Catalog.byId::get))
    }

    private fun keyOf(variation: Int, vararg groups: MuscleGroup) = listOf(
        variation.toString(),
        groups.joinToString("+") { it.name },
        groups.joinToString(";") { Catalog.exercises.getValue(it).first().id },
    ).joinToString("|")

    @Test fun `variation counters survive a text round trip`() {
        val map = mapOf(CHEST to 4, GLUTES to 1)
        assertEquals(map, decodeVariations(encodeVariations(map)))
        assertEquals(emptyMap<MuscleGroup, Int>(), decodeVariations(""))
    }

    @Test fun `calf raises complement the leg groups`() {
        val ids = (Catalog.exercises.getValue(HAMSTRINGS) + Catalog.exercises.getValue(GLUTES)).map { it.id }
        assertTrue("standing-calf-raise" in ids && "seated-calf-raise" in ids)
    }
}
