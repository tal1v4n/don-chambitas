package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Tinta
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Naranja
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario

private const val MAXIMO_ESTRELLAS = 5
private const val UMBRAL_MEDIA_ESTRELLA = 0.25f
private const val UMBRAL_ESTRELLA_COMPLETA = 0.75f

/**
 * Componente de calificacion con estrellas en color Naranja segun DISENO.md.
 * Admite media estrella en modo lectura.
 */
@Composable
fun Estrellas(
    calificacion: Float,
    puntuacionMaxima: Int = MAXIMO_ESTRELLAS,
    tamano: Dp = 16.dp,
    mostrarNumero: Boolean = false,
    totalResenas: Int? = null,
    modifier: Modifier = Modifier
) {
    val descripcionAccesibilidad = stringResource(
        R.string.formato_estrellas_descripcion,
        calificacion
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = descripcionAccesibilidad
        }
    ) {
        for (indice in 1..puntuacionMaxima) {
            val diferencia = calificacion - (indice - 1)
            val (icono, tinte) = when {
                diferencia >= UMBRAL_ESTRELLA_COMPLETA -> Icons.Outlined.Star to Naranja
                diferencia >= UMBRAL_MEDIA_ESTRELLA -> Icons.AutoMirrored.Outlined.StarHalf to Naranja
                else -> Icons.Outlined.StarBorder to Borde
            }

            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tinte,
                modifier = Modifier.size(tamano)
            )
        }

        if (mostrarNumero) {
            Spacer(modifier = Modifier.width(Espaciado.dp4))
            val texto = if (totalResenas != null) {
                stringResource(R.string.formato_calificacion, calificacion, totalResenas)
            } else {
                stringResource(R.string.formato_calificacion_simple, calificacion)
            }
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.secundario,
                color = Gris
            )
        }
    }
}

/**
 * Calificacion en una pildora AmarilloSuave: una estrella Naranja, el numero en
 * negritas y las resenas en Gris. Para tarjetas donde cinco estrellas no caben
 * o estorban. Lleva la misma descripcion que [Estrellas].
 */
@Composable
fun CalificacionCompacta(
    calificacion: Float,
    totalResenas: Int? = null,
    modifier: Modifier = Modifier
) {
    val descripcionAccesibilidad = stringResource(R.string.formato_estrellas_descripcion, calificacion)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp4),
        modifier = modifier
            .clip(CircleShape)
            .background(AmarilloSuave)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = descripcionAccesibilidad }
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Naranja,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.formato_calificacion_simple, calificacion),
            style = DonChambitasTema.tipografia.pie,
            fontWeight = FontWeight.Bold,
            color = Tinta
        )
        if (totalResenas != null) {
            Text(
                text = stringResource(R.string.formato_resenas_parentesis, totalResenas),
                style = DonChambitasTema.tipografia.pie,
                color = Gris
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaEstrellasCinco() {
    DonChambitasTema {
        Estrellas(calificacion = 5.0f)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaEstrellasCuatroYMedio() {
    DonChambitasTema {
        Estrellas(
            calificacion = 4.5f,
            mostrarNumero = true,
            totalResenas = 28
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaEstrellasTresConTexto() {
    DonChambitasTema {
        Estrellas(
            calificacion = 3.0f,
            mostrarNumero = true
        )
    }
}
