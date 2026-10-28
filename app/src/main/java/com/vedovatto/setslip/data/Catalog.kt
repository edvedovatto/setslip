package com.vedovatto.setslip.data

import com.vedovatto.setslip.domain.Exercise
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.domain.MuscleGroup.*

object Catalog {

    val exercises: Map<MuscleGroup, List<Exercise>> = mapOf(
        CHEST to list(
            "Supino reto" to "4 x 8", "Supino inclinado" to "3 x 10", "Crucifixo" to "3 x 12",
            "Crossover" to "3 x 12", "Supino declinado" to "3 x 10", "Pec deck" to "3 x 12",
            "Flexão de braço" to "3 x 15", "Supino com halteres" to "4 x 10",
        ),
        BACK to list(
            "Puxada frontal" to "4 x 10", "Remada curvada" to "4 x 8", "Remada baixa" to "3 x 12",
            "Pull-over" to "3 x 12", "Barra fixa" to "3 x 8", "Serrote" to "3 x 10",
            "Remada cavalinho" to "3 x 10", "Puxada neutra" to "3 x 12",
        ),
        SHOULDERS to list(
            "Desenvolvimento" to "4 x 8", "Elevação lateral" to "4 x 12", "Elevação frontal" to "3 x 12",
            "Crucifixo inverso" to "3 x 12", "Remada alta" to "3 x 10", "Arnold press" to "3 x 10",
            "Face pull" to "3 x 15", "Elevação lateral no cabo" to "3 x 12",
        ),
        BICEPS to list(
            "Rosca direta" to "4 x 10", "Rosca martelo" to "3 x 12", "Rosca scott" to "3 x 10",
            "Rosca concentrada" to "3 x 12", "Rosca alternada" to "3 x 10", "Rosca no cabo" to "3 x 12",
            "Rosca inclinada" to "3 x 10", "Rosca inversa" to "3 x 12",
        ),
        TRICEPS to list(
            "Tríceps corda" to "3 x 12", "Tríceps testa" to "4 x 10", "Mergulho" to "3 x 10",
            "Tríceps francês" to "3 x 12", "Coice" to "3 x 12", "Supino fechado" to "3 x 10",
            "Tríceps barra" to "3 x 12", "Tríceps unilateral" to "3 x 12",
        ),
        QUADRICEPS to list(
            "Agachamento livre" to "4 x 8", "Leg press" to "4 x 10", "Cadeira extensora" to "3 x 12",
            "Agachamento hack" to "3 x 10", "Afundo" to "3 x 10", "Agachamento búlgaro" to "3 x 10",
            "Agachamento frontal" to "3 x 8", "Passada" to "3 x 12",
        ),
        HAMSTRINGS to list(
            "Mesa flexora" to "4 x 10", "Stiff" to "4 x 8", "Cadeira flexora" to "3 x 12",
            "Flexora em pé" to "3 x 12", "Levantamento terra" to "4 x 6", "Good morning" to "3 x 10",
            "Flexão nórdica" to "3 x 6", "Stiff unilateral" to "3 x 10",
        ),
        GLUTES to list(
            "Elevação pélvica" to "4 x 10", "Cadeira abdutora" to "3 x 15", "Coice no cabo" to "3 x 12",
            "Agachamento sumô" to "3 x 12", "Afundo reverso" to "3 x 10", "Abdução no cabo" to "3 x 12",
            "Glúteo 4 apoios" to "3 x 15", "Step-up" to "3 x 10",
        ),
        CALVES to list(
            "Panturrilha em pé" to "4 x 15", "Panturrilha sentada" to "4 x 15",
            "Panturrilha no leg press" to "3 x 15", "Panturrilha unilateral" to "3 x 12",
            "Panturrilha no smith" to "3 x 15", "Panturrilha no step" to "3 x 20",
            "Panturrilha no hack" to "3 x 15", "Panturrilha com halter" to "3 x 15",
        ),
        ABS to list(
            "Prancha" to "3 x 40s", "Abdominal supra" to "4 x 20", "Elevação de pernas" to "3 x 15",
            "Bicicleta" to "3 x 20", "Remador" to "3 x 15", "Russian twist" to "3 x 20",
            "Abdominal na polia" to "3 x 15", "Prancha lateral" to "3 x 30s",
        ),
    )

    private fun list(vararg items: Pair<String, String>) = items.map { (name, rx) -> Exercise(name, rx) }
}
