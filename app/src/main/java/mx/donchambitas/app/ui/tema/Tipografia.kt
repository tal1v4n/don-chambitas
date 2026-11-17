package mx.donchambitas.app.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Estilos tipograficos con fuente del sistema segun DISENO.md

/** Encabezado grande de portada: la primera frase de una pantalla con hero. */
val Portada = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.02).em
)

val Titulo = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.01).em
)

val Subtitulo = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp
)

val CuerpoFuerte = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp
)

val Cuerpo = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

val Secundario = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)

val Pie = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)

// Configuracion de tipografia de Material 3
val Tipografia = Typography(
    headlineSmall = Titulo,
    titleLarge = Titulo,
    titleMedium = Subtitulo,
    titleSmall = CuerpoFuerte,
    bodyLarge = Cuerpo,
    bodyMedium = Secundario,
    bodySmall = Pie,
    labelLarge = CuerpoFuerte,
    labelMedium = Secundario,
    labelSmall = Pie,
    displaySmall = Portada
)

// Extensiones para acceso directo a los estilos con nombres exactos de DISENO.md
val Typography.portada: TextStyle
    get() = Portada

val Typography.titulo: TextStyle
    get() = Titulo

val Typography.subtitulo: TextStyle
    get() = Subtitulo

val Typography.cuerpoFuerte: TextStyle
    get() = CuerpoFuerte

val Typography.cuerpo: TextStyle
    get() = Cuerpo

val Typography.secundario: TextStyle
    get() = Secundario

val Typography.pie: TextStyle
    get() = Pie
