package com.vedovatto.setslip.data

import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.MuscleGroup

data class Prefs(
    val soundOn: Boolean = true,
    val vibrationOn: Boolean = true,
    val language: Language = Language.PT,
    val variations: Map<MuscleGroup, Int> = emptyMap(),
    val favorites: List<String> = emptyList(),
    val struck: Set<String> = emptySet(),
    val tipSeen: Boolean = false,
)

fun encodeVariations(map: Map<MuscleGroup, Int>): String = map.entries.joinToString(",") { "${it.key.name}=${it.value}" }

fun decodeVariations(text: String): Map<MuscleGroup, Int> = text.split(",").mapNotNull { part ->
    val (name, count) = part.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
    val group = MuscleGroup.entries.firstOrNull { it.name == name } ?: return@mapNotNull null
    count.toIntOrNull()?.let { group to it }
}.toMap()
