package mx.donchambitas.app.ui.tema

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color Casco de Material 3 segun DISENO.md.
 * Sin modo oscuro en el MVP (DEC-15). Los contenedores de superficie se fijan
 * a mano: si se dejan por defecto, Material 3 pinta menus, dialogos y la barra
 * inferior con su lila de base.
 */
val EsquemaColoresCasco = lightColorScheme(
    primary = Amarillo,
    onPrimary = Tinta,
    primaryContainer = AmarilloSuave,
    onPrimaryContainer = Tinta,
    inversePrimary = Amarillo,
    secondary = Naranja,
    onSecondary = Blanco,
    secondaryContainer = AmarilloSuave,
    onSecondaryContainer = Tinta,
    tertiary = Grafito,
    onTertiary = Blanco,
    tertiaryContainer = Hueso,
    onTertiaryContainer = Grafito,
    background = Marfil,
    onBackground = Tinta,
    surface = Hueso,
    onSurface = Tinta,
    surfaceVariant = Hueso,
    onSurfaceVariant = Gris,
    surfaceTint = Hueso,
    surfaceBright = Marfil,
    surfaceDim = HuesoAlto,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Marfil,
    surfaceContainer = Hueso,
    surfaceContainerHigh = HuesoAlto,
    surfaceContainerHighest = Borde,
    inverseSurface = Tinta,
    inverseOnSurface = Marfil,
    outline = Borde,
    outlineVariant = Borde,
    error = Error,
    onError = Blanco,
    errorContainer = Hueso,
    onErrorContainer = Error,
    scrim = Tinta
)

@Composable
fun DonChambitasTema(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaColoresCasco,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}

object DonChambitasTema {
    val colores: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val tipografia: Typography
        @Composable
        get() = MaterialTheme.typography

    val formas: Shapes
        @Composable
        get() = MaterialTheme.shapes

    val espaciado: Espaciado
        get() = Espaciado
}
