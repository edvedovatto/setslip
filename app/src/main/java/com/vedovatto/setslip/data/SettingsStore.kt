package com.vedovatto.setslip.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vedovatto.setslip.domain.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("setslip")

private val SOUND = booleanPreferencesKey("sound")
private val VIBRATION = booleanPreferencesKey("vibration")
private val LANGUAGE = stringPreferencesKey("language")
private val VARIATIONS = stringPreferencesKey("variations")
private val FAVORITES = stringPreferencesKey("favorites")
private val STRUCK = stringPreferencesKey("struck")
private val TIP_SEEN = booleanPreferencesKey("tip_seen")

class SettingsStore(private val context: Context) {

    val prefs: Flow<Prefs> = context.dataStore.data.map { it.toPrefs() }

    suspend fun update(block: (Prefs) -> Prefs) {
        context.dataStore.edit { p ->
            val next = block(p.toPrefs())
            p[SOUND] = next.soundOn
            p[VIBRATION] = next.vibrationOn
            p[LANGUAGE] = next.language.name
            p[VARIATIONS] = encodeVariations(next.variations)
            p[FAVORITES] = next.favorites.joinToString("\n")
            p[STRUCK] = next.struck.joinToString(",")
            p[TIP_SEEN] = next.tipSeen
        }
    }
}

private fun Preferences.toPrefs() = Prefs(
    soundOn = this[SOUND] ?: true,
    vibrationOn = this[VIBRATION] ?: true,
    language = Language.entries.firstOrNull { it.name == this[LANGUAGE] } ?: Language.PT,
    variations = decodeVariations(this[VARIATIONS].orEmpty()),
    favorites = this[FAVORITES].orEmpty().split("\n").filter { it.isNotBlank() },
    struck = this[STRUCK].orEmpty().split(",").filter { it.isNotBlank() }.toSet(),
    tipSeen = this[TIP_SEEN] ?: false,
)
