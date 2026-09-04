package com.example.fitswap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.fitswap.domain.model.ColorPalette

private val ClassicDark = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val ClassicLight = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private val EnergyDark = darkColorScheme(primary = EnergyOrange80, secondary = EnergyWarmGrey80, tertiary = EnergyAmber80)
private val EnergyLight = lightColorScheme(primary = EnergyOrange40, secondary = EnergyWarmGrey40, tertiary = EnergyAmber40)

private val SteelDark = darkColorScheme(primary = SteelBlue80, secondary = SteelSlate80, tertiary = SteelIce80)
private val SteelLight = lightColorScheme(primary = SteelBlue40, secondary = SteelSlate40, tertiary = SteelIce40)

private val NeonDark = darkColorScheme(primary = NeonLime80, secondary = NeonCarbon80, tertiary = NeonCyan80)
private val NeonLight = lightColorScheme(primary = NeonLime40, secondary = NeonCarbon40, tertiary = NeonCyan40)

private fun fixedColorScheme(palette: ColorPalette, darkTheme: Boolean): ColorScheme = when (palette) {
    ColorPalette.CLASSIC -> if (darkTheme) ClassicDark else ClassicLight
    ColorPalette.ENERGY -> if (darkTheme) EnergyDark else EnergyLight
    ColorPalette.STEEL -> if (darkTheme) SteelDark else SteelLight
    ColorPalette.NEON -> if (darkTheme) NeonDark else NeonLight
    ColorPalette.DYNAMIC -> if (darkTheme) ClassicDark else ClassicLight // fallback si no hay Material You (< Android 12)
}

/**
 * La paleta de color es independiente del toggle claro/oscuro ([darkTheme]): `DYNAMIC` usa
 * Material You (colores del wallpaper, Android 12+, con el mismo fallback previo); el resto son
 * paletas fijas de identidad "gimnasio" (ver `domain/model/AppSettings.kt#ColorPalette`).
 */
@Composable
fun FitSwapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ColorPalette = ColorPalette.DYNAMIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        palette == ColorPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> fixedColorScheme(palette, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
