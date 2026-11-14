package com.vedovatto.setslip.data

import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.ui.screens.exerciseImageIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {

    private val all = Catalog.exercises.values.flatten()

    @Test fun `every group has a catalog`() =
        assertEquals(MuscleGroup.entries.toSet(), Catalog.exercises.keys)

    @Test fun `every group has eight exercises`() =
        Catalog.exercises.forEach { (group, list) -> assertEquals(group.name, 8, list.size) }

    @Test fun `ids are unique`() = assertEquals(all.size, all.map { it.id }.toSet().size)

    @Test fun `required fields are filled in`() = all.forEach {
        assertTrue(it.id, it.id.matches(Regex("[a-z0-9]+(-[a-z0-9]+)*")))
        assertTrue(it.id, it.name.isNotBlank() && it.nameEn.isNotBlank())
        assertTrue(it.id, it.prescription.matches(Regex("[0-9]+ x [0-9]+s?")))
        assertTrue(it.id, it.muscles.isNotEmpty())
        assertTrue(it.id, it.steps.size >= 3 && it.steps.all { s -> s.isNotBlank() })
    }

    @Test fun `every exercise has a picture`() = assertEquals(Catalog.byId.keys, exerciseImageIds)
}
