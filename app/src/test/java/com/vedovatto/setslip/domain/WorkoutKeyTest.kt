package com.vedovatto.setslip.domain

import com.vedovatto.setslip.data.Catalog
import com.vedovatto.setslip.data.Prefs
import com.vedovatto.setslip.data.decodeVariations
import com.vedovatto.setslip.data.encodeVariations
import com.vedovatto.setslip.domain.MuscleGroup.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutKeyTest {

    private val generator = WorkoutGenerator(Catalog.exercises)

    @Test fun `ficha sobrevive ao ciclo de chave`() {
        val w = generator.generate(1, listOf(BACK, TRICEPS), mapOf(BACK to 2))
        assertEquals(w.sections, Workout.fromKey(9, w.key, Catalog.byId::get)?.sections)
    }

    @Test fun `chave invalida volta nula`() {
        assertNull(Workout.fromKey(1, "lixo", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|CHEST|nao-existe", Catalog.byId::get))
        assertNull(Workout.fromKey(1, "0|FOO|bench-press", Catalog.byId::get))
    }

    @Test fun `riscado nao e sugerido de novo`() {
        val base = generator.generate(1, listOf(CHEST), emptyMap())
        val struck = base.exercises.take(2).map { it.id }.toSet()
        val next = generator.generate(2, listOf(CHEST), emptyMap(), struck)
        assertTrue(next.exercises.none { it.id in struck })
        assertEquals(6, next.exerciseCount)
    }

    @Test fun `riscar quase tudo ainda rende ficha completa`() {
        val struck = Catalog.exercises.getValue(ABS).drop(1).map { it.id }.toSet()
        assertEquals(6, generator.generate(1, listOf(ABS), emptyMap(), struck).exerciseCount)
    }

    @Test fun `contadores de variacao fazem ciclo de texto`() {
        val map = mapOf(CHEST to 4, GLUTES to 1)
        assertEquals(map, decodeVariations(encodeVariations(map)))
        assertEquals(emptyMap<MuscleGroup, Int>(), decodeVariations(""))
    }

    @Test fun `ids de panturrilha ficam como complemento`() {
        val ids = Catalog.exercises.getValue(HAMSTRINGS).map { it.id } + Catalog.exercises.getValue(GLUTES).map { it.id }
        assertTrue("standing-calf-raise" in ids && "seated-calf-raise" in ids)
        assertFalse(Prefs().struck.isNotEmpty())
    }
}
