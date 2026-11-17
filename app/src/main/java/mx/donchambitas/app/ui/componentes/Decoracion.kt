package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Tinta

/**
 * Firma grafica de Don Chambitas: arcos concentricos, como el ala de un casco
 * vista desde arriba. Es pura decoracion, sin semantica, y se dibuja con el
 * color que le toque a cada portada.
 *
 * @param anillos Cuantos arcos se dibujan del centro hacia afuera.
 * @param centro Posicion del centro, en fraccion del ancho y alto del lienzo.
 * @param radioInicial Radio libre en el centro, para rodear algo sin taparlo.
 */
@Composable
fun ArcosCasco(
    color: Color,
    modifier: Modifier = Modifier,
    anillos: Int = 5,
    grosor: Dp = 18.dp,
    centro: Offset = Offset(1f, 0f),
    radioInicial: Dp = grosor * 2
) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width * centro.x, size.height * centro.y)
        val paso = grosor.toPx() * 2f
        val trazo = Stroke(width = grosor.toPx())
        for (i in 1..anillos) {
            drawCircle(
                color = color.copy(alpha = color.alpha * (1f - (i - 1) * 0.12f)),
                radius = radioInicial.toPx() + paso * i,
                center = c,
                style = trazo
            )
        }
    }
}

/**
 * El casco de la marca dentro de una medalla Blanca con sombra suave. Lleva la
 * descripcion del logotipo para lectores de pantalla.
 */
@Composable
fun MedallaMarca(
    modifier: Modifier = Modifier,
    lado: Dp = 64.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(lado)
            .shadow(elevation = 10.dp, shape = CircleShape, ambientColor = Tinta, spotColor = Tinta)
            .clip(CircleShape)
            .background(Blanco)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.splash_logo_descripcion),
            modifier = Modifier.requiredSize(lado * 1.45f)
        )
    }
}
