package com.vedovatto.setslip.ui.printer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.vedovatto.setslip.R
import kotlinx.coroutines.delay

class PrinterSounds(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val feed = pool.load(context, R.raw.printer_feed, 1)
    private val drop = pool.load(context, R.raw.paper_drop, 1)
    private var stream = 0

    fun playDrop() {
        pool.play(drop, DROP_VOLUME, DROP_VOLUME, 1, 0, 1f)
    }

    suspend fun playFeed() {
        stream = pool.play(feed, FEED_VOLUME, FEED_VOLUME, 1, 0, 1f)
        delay((PrinterTimings.FEED_SOUND_END_MS - PrinterTimings.FEED_FADE_MS).toLong())
        fadeOut()
    }

    suspend fun stopFeed() = fadeOut()

    private suspend fun fadeOut() {
        val s = stream
        if (s == 0) return
        try {
            for (i in FADE_STEPS - 1 downTo 0) {
                val v = FEED_VOLUME * i / FADE_STEPS
                pool.setVolume(s, v, v)
                delay(PrinterTimings.FEED_FADE_MS.toLong() / FADE_STEPS)
            }
        } finally {
            pool.stop(s)
            if (stream == s) stream = 0
        }
    }

    fun release() = pool.release()

    private companion object {
        const val FEED_VOLUME = 0.35f
        const val DROP_VOLUME = 1f
        const val FADE_STEPS = 16
    }
}

@Composable
fun rememberPrinterSounds(): PrinterSounds {
    val context = LocalContext.current
    val sounds = remember { PrinterSounds(context.applicationContext) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    return sounds
}
