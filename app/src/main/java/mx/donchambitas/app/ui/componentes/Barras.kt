package mx.donchambitas.app.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Marfil
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo

/**
 * Modelo de datos para un destino en la barra de navegacion inferior.
 */
data class DestinoBarraInferior(
    val id: Int,
    val etiqueta: String,
    val icono: ImageVector
)

/**
 * Destinos base de navegacion para la barra inferior segun DISENO.md.
 */
@Composable
fun obtenerDestinosPorDefecto(): List<DestinoBarraInferior> = listOf(
    DestinoBarraInferior(0, stringResource(R.string.navegacion_inicio), Icons.Outlined.Home),
    DestinoBarraInferior(1, stringResource(R.string.navegacion_buscar), Icons.Outlined.Search),
    DestinoBarraInferior(2, stringResource(R.string.navegacion_solicitudes), Icons.AutoMirrored.Outlined.Assignment),
    DestinoBarraInferior(3, stringResource(R.string.navegacion_perfil), Icons.Outlined.Person)
)

/**
 * Barra superior de la aplicacion segun DISENO.md.
 *
 * Sin relleno propio por omision: toma el fondo de la pantalla, para que el
 * amarillo quede en las acciones y no en un bloque que cansa la vista. Una
 * pantalla con cabecera de color le pasa ese color en [colorFondo] para que
 * barra y cabecera se lean como una sola pieza. El titulo va en Tinta y
 * la flecha de regreso dentro de un circulo Hueso, facil de atinar con el pulgar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    alRegresar: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    colorFondo: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = titulo,
                // Sin flecha es una pantalla raiz (pestaña): el titulo crece.
                style = if (alRegresar == null) DonChambitasTema.tipografia.titulo else DonChambitasTema.tipografia.subtitulo,
                fontWeight = if (alRegresar == null) FontWeight.Bold else FontWeight.SemiBold,
                color = Tinta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (alRegresar != null) {
                IconButton(
                    onClick = alRegresar,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Hueso)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.regresar),
                            tint = Tinta
                        )
                    }
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorFondo,
            scrolledContainerColor = if (colorFondo == Color.Transparent) Marfil else colorFondo,
            titleContentColor = Tinta,
            navigationIconContentColor = Tinta,
            actionIconContentColor = Tinta
        ),
        modifier = modifier
    )
}

/** Un destino ya resuelto de la barra flotante: que dice, que icono lleva y si es el actual. */
data class ElementoBarraFlotante(
    val etiqueta: String,
    val icono: ImageVector,
    val seleccionado: Boolean,
    val alPulsar: () -> Unit
)

/**
 * Barra de navegacion inferior en forma de capsula flotante Tinta.
 *
 * El destino actual se abre en una pildora Amarillo con su etiqueta (Tinta sobre
 * Amarillo, 11.78:1); los demas muestran solo el icono en Borde sobre Tinta
 * (13.4:1) y conservan la etiqueta como descripcion para lectores de pantalla.
 */
@Composable
fun BarraNavegacionFlotante(
    elementos: List<ElementoBarraFlotante>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Marfil)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Tinta, spotColor = Tinta)
                .clip(CircleShape)
                .background(Tinta)
                .selectableGroup()
                .padding(6.dp)
        ) {
            elementos.forEach { elemento ->
                PildoraDestino(elemento)
            }
        }
    }
}

@Composable
private fun PildoraDestino(elemento: ElementoBarraFlotante) {
    val fondo by animateColorAsState(
        targetValue = if (elemento.seleccionado) Amarillo else Color.Transparent,
        label = "fondoDestino"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(fondo)
            .selectable(
                selected = elemento.seleccionado,
                onClick = elemento.alPulsar,
                role = Role.Tab
            )
            .heightIn(min = 48.dp)
            .padding(horizontal = if (elemento.seleccionado) 18.dp else 14.dp)
            .animateContentSize()
    ) {
        Icon(
            imageVector = elemento.icono,
            contentDescription = if (elemento.seleccionado) null else elemento.etiqueta,
            tint = if (elemento.seleccionado) Tinta else Borde
        )
        if (elemento.seleccionado) {
            Text(
                text = elemento.etiqueta,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold,
                color = Tinta,
                maxLines = 1
            )
        }
    }
}

/**
 * Barra inferior generica con 4 destinos, sobre [BarraNavegacionFlotante].
 */
@Composable
fun BarraInferior(
    destinoActual: Int,
    alSeleccionarDestino: (Int) -> Unit,
    destinos: List<DestinoBarraInferior> = obtenerDestinosPorDefecto(),
    modifier: Modifier = Modifier
) {
    BarraNavegacionFlotante(
        elementos = destinos.map { destino ->
            ElementoBarraFlotante(
                etiqueta = destino.etiqueta,
                icono = destino.icono,
                seleccionado = destino.id == destinoActual,
                alPulsar = { alSeleccionarDestino(destino.id) }
            )
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviaBarraSuperiorConRegreso() {
    DonChambitasTema {
        BarraSuperior(
            titulo = "Detalle del servicio",
            alRegresar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviaBarraSuperiorPrincipal() {
    DonChambitasTema {
        BarraSuperior(
            titulo = "Don Chambitas"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviaBarraInferior() {
    DonChambitasTema {
        BarraInferior(
            destinoActual = 0,
            alSeleccionarDestino = {}
        )
    }
}
