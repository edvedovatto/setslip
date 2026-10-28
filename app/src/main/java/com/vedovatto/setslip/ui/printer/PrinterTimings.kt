package com.vedovatto.setslip.ui.printer

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.unit.dp
import com.vedovatto.setslip.domain.Workout

object PrinterTimings {

    const val FEED_MS = 3800
    val feedSteps = listOf(
        0 to 1.10f,
        608 to 0.84f, 760 to 0.84f,
        1444 to 0.60f, 1596 to 0.60f,
        2584 to 0.24f, 2736 to 0.24f,
        FEED_MS to 0f,
    )
    val feedEasing: Easing = CubicBezierEasing(0.4f, 0.1f, 0.2f, 1f)

    const val FEED_SOUND_END_MS = 3100
    const val FEED_FADE_MS = 500

    const val JITTER_HALF_PERIOD_MS = 55L
    val jitter = 0.5.dp

    fun restOffsetFor(workout: Workout) = if (workout.isLong) 12.dp else 22.dp

    const val DROP_MS = 950
    const val IMPULSE_MS = 114
    val impulse = 8.dp
    val dropOvershoot = 60.dp
    val gravity: Easing = Easing { it * it }
    val swayX = listOf(0f, -1.5f, 1.2f, -0.6f, 0f)

    const val SLOT_GLOW_MS = 450
    const val LED_BLINK_MS = 250

    const val KEY_PRESS_MS = 70
    const val REJECT_SHAKE_MS = 300
    val rejectShake = 3.dp
    const val LIMIT_FLASH_MS = 90L
}
