package mx.donchambitas.app.ui.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraNavegacionFlotante
import mx.donchambitas.app.ui.componentes.ElementoBarraFlotante
import mx.donchambitas.app.ui.tema.DonChambitasTema

/**
 * Destino representativo en la barra inferior del trabajador.
 */
data class ElementoBarraTrabajador(
    val ruta: Ruta,
    val etiquetaRes: Int,
    val icono: ImageVector
)

/**
 * Los 4 destinos exactos de la barra inferior del trabajador según PANTALLAS.md:
 * P-10 Inicio · P-12 Servicios · P-15 Chats · P-18 Cuenta.
 */
val DESTINOS_BARRA_TRABAJADOR: List<ElementoBarraTrabajador> = listOf(
    ElementoBarraTrabajador(
        ruta = Ruta.InicioTrabajador,
        etiquetaRes = R.string.navegacion_inicio,
        icono = Icons.Outlined.Home
    ),
    ElementoBarraTrabajador(
        ruta = Ruta.MisServicios,
        etiquetaRes = R.string.navegacion_servicios,
        icono = Icons.Outlined.Build
    ),
    ElementoBarraTrabajador(
        ruta = Ruta.Conversaciones,
        etiquetaRes = R.string.navegacion_chats,
        icono = Icons.AutoMirrored.Outlined.Chat
    ),
    ElementoBarraTrabajador(
        ruta = Ruta.MiCuenta,
        etiquetaRes = R.string.navegacion_cuenta,
        icono = Icons.Outlined.Person
    )
)

/**
 * Barra de navegación inferior para el rol de Trabajador.
 * Contiene exactamente 4 destinos y usa la capsula flotante de DISENO.md.
 */
@Composable
fun BarraInferiorTrabajador(
    rutaActual: String?,
    alSeleccionarRuta: (Ruta) -> Unit,
    modifier: Modifier = Modifier
) {
    BarraNavegacionFlotante(
        elementos = DESTINOS_BARRA_TRABAJADOR.map { elemento ->
            val seleccionado = when (elemento.ruta) {
                Ruta.MisServicios -> {
                    rutaActual == Ruta.MisServicios.ruta || rutaActual == Ruta.MisPostulaciones.ruta
                }
                Ruta.MiCuenta -> {
                    rutaActual == Ruta.MiCuenta.ruta || rutaActual == Ruta.MiPerfil.ruta
                }
                else -> {
                    rutaActual == elemento.ruta.ruta
                }
            }
            ElementoBarraFlotante(
                etiqueta = stringResource(elemento.etiquetaRes),
                icono = elemento.icono,
                seleccionado = seleccionado,
                alPulsar = { alSeleccionarRuta(elemento.ruta) }
            )
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviaBarraInferiorTrabajador() {
    DonChambitasTema {
        BarraInferiorTrabajador(
            rutaActual = Ruta.InicioTrabajador.ruta,
            alSeleccionarRuta = {}
        )
    }
}
