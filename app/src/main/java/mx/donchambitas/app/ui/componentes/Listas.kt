package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie

private val FormaIconoFila = RoundedCornerShape(12.dp)

/**
 * Grupo de opciones en una sola tarjeta Blanca, con titulo opcional encima.
 * Las filas se separan con una linea fina que arranca despues del icono.
 */
@Composable
fun GrupoOpciones(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
        if (titulo != null) {
            Text(
                text = titulo.uppercase(),
                style = DonChambitasTema.tipografia.pie,
                fontWeight = FontWeight.SemiBold,
                color = Gris,
                modifier = Modifier.padding(start = Espaciado.dp4)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FormaTarjeta)
                .background(Blanco)
                .border(1.dp, Borde, FormaTarjeta),
            content = contenido
        )
    }
}

/** Linea entre dos [FilaOpcion] de un mismo grupo. */
@Composable
fun SeparadorOpciones() {
    HorizontalDivider(color = Borde, modifier = Modifier.padding(start = 68.dp))
}

/**
 * Una opcion de menu: icono en un cuadro redondeado, texto, detalle opcional y
 * flecha. Toda la fila es el boton.
 *
 * @param color Texto e icono. Tinta normalmente; Error en acciones que cierran algo.
 * @param fondoIcono Cuadro detras del icono.
 */
@Composable
fun FilaOpcion(
    texto: String,
    icono: ImageVector,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    color: Color = Tinta,
    fondoIcono: Color = AmarilloSuave,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = habilitado && !cargando, role = Role.Button, onClick = alPulsar)
            .alpha(if (habilitado) 1f else 0.5f)
            .padding(horizontal = Espaciado.dp12, vertical = Espaciado.dp12)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(FormaIconoFila)
                .background(fondoIcono)
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpo,
                fontWeight = FontWeight.Medium,
                color = color
            )
            if (detalle != null) {
                Text(text = detalle, style = DonChambitasTema.tipografia.pie, color = Gris)
            }
        }
        if (cargando) {
            CircularProgressIndicator(color = color, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Gris
            )
        }
    }
}

/**
 * Bloque de un formulario largo: tarjeta Blanca con su icono en un cuadro
 * AmarilloSuave y el titulo, y debajo los campos. Parte los formularios en
 * pedazos que se leen de un vistazo.
 */
@Composable
fun SeccionFormulario(
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Blanco)
            .border(1.dp, Borde, FormaTarjeta)
            .padding(Espaciado.dp16),
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(FormaIconoFila)
                    .background(AmarilloSuave)
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = Tinta, modifier = Modifier.size(20.dp))
            }
            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
        }
        contenido()
    }
}
