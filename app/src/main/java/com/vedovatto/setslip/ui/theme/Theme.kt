package com.vedovatto.setslip.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.vedovatto.setslip.R

object Palette {
    val Graphite = Color(0xFF0B0B0D)
    val Ink = Color(0xFFECE6D8)
    val Warm = Color(0xFFE9A35C)
    val Paper = Color(0xFFEFE8D8)
    val Paper2 = Color(0xFFE4DCC9)
    val PaperText = Color(0xFF1C1A17)
    val PaperMuted = Color(0xFF6B665C)
    val PaperSoft = Color(0xFF4A463E)

    val PrinterTop = Color(0xFF3A3A43)
    val PrinterMid = Color(0xFF26262C)
    val PrinterBottom = Color(0xFF1B1B20)
    val Slot = Color(0xFF030304)
    val LedOff = Color(0xFF3B2C1B)

    val TrayLight = Color(0xFF3A342E)
    val TrayMid = Color(0xFF232227)
    val TrayDark = Color(0xFF151519)

    val PanelTop = Color(0xFF222228)
    val PanelBottom = Color(0xFF141418)
    val CapTop = Color(0xFF4B4B55)
    val CapBottom = Color(0xFF2D2D34)
    val CapSide = Color(0xFF101013)
    val CapText = Color(0xFFD8D2C4)
    val KeyLedOff = Color(0xFF2B2118)

    val AmberTop = Color(0xFFF2B774)
    val AmberBottom = Color(0xFFC9823A)
    val AmberSide = Color(0xFF6E4115)
    val AmberText = Color(0xFF2A1706)
}

object Fonts {
    val Mono = FontFamily(
        Font(R.font.dm_mono_regular, FontWeight.Normal),
        Font(R.font.dm_mono_medium, FontWeight.Medium),
    )
}
