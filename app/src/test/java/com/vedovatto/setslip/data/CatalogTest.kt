package com.vedovatto.setslip.data

import com.vedovatto.setslip.domain.MuscleGroup
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {

    private val all = Catalog.exercises.values.flatten()

    @Test fun `todos os grupos tem catalogo`() =
        assertEquals(MuscleGroup.entries.toSet(), Catalog.exercises.keys)

    @Test fun `cada grupo tem 8 exercicios`() =
        Catalog.exercises.forEach { (group, list) -> assertEquals(group.name, 8, list.size) }

    @Test fun `ids sao unicos`() = assertEquals(all.size, all.map { it.id }.toSet().size)

    @Test fun `campos obrigatorios preenchidos`() = all.forEach {
        assertTrue(it.id, it.id.matches(Regex("[a-z0-9]+(-[a-z0-9]+)*")))
        assertTrue(it.id, it.name.isNotBlank() && it.nameEn.isNotBlank())
        assertTrue(it.id, it.prescription.matches(Regex("[0-9]+ x [0-9]+s?")))
        assertTrue(it.id, it.muscles.isNotEmpty())
        assertTrue(it.id, it.steps.size >= 3 && it.steps.all { s -> s.isNotBlank() })
    }

    @Test fun `cada exercicio tem imagem`() {
        val dir = File("src/main/res/drawable-nodpi")
        all.forEach { assertTrue(it.imageName, File(dir, it.imageName + ".jpg").isFile) }
    }
}
