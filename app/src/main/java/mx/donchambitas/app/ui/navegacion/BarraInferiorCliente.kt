package mx.donchambitas.app.ui.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Chat
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
 * Destino representativo en la barra inferior del cliente.
 */
data class ElementoBarraCliente(
    val ruta: Ruta,
    val etiquetaRes: Int,
    val icono: ImageVector
)

/**
 * Los 4 destinos exactos de la barra inferior del cliente según PANTALLAS.md:
 * P-05 Inicio · P-09 Solicitudes · P-15 Chats · P-18 Cuenta.
 */
val DESTINOS_BARRA_CLIENTE: List<ElementoBarraCliente> = listOf(
    ElementoBarraCliente(
        ruta = Ruta.InicioCliente,
        etiquetaRes = R.string.navegacion_inicio,
        icono = Icons.Outlined.Home
    ),
    ElementoBarraCliente(
        ruta = Ruta.MisSolicitudes,
        etiquetaRes = R.string.navegacion_solicitudes,
        icono = Icons.AutoMirrored.Outlined.Assignment
    ),
    ElementoBarraCliente(
        ruta = Ruta.Conversaciones,
        etiquetaRes = R.string.navegacion_chats,
        icono = Icons.AutoMirrored.Outlined.Chat
    ),
    ElementoBarraCliente(
        ruta = Ruta.MiCuenta,
        etiquetaRes = R.string.navegacion_cuenta,
        icono = Icons.Outlined.Person
    )
)

/**
 * Barra de navegación inferior para el rol de Cliente.
 * Contiene exactamente 4 destinos y usa la capsula flotante de DISENO.md.
 */
@Composable
fun BarraInferiorCliente(
    rutaActual: String?,
    alSeleccionarRuta: (Ruta) -> Unit,
    modifier: Modifier = Modifier
) {
    BarraNavegacionFlotante(
        elementos = DESTINOS_BARRA_CLIENTE.map { elemento ->
            val seleccionado = rutaActual == elemento.ruta.ruta
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
private fun PreviaBarraInferiorCliente() {
    DonChambitasTema {
        BarraInferiorCliente(
            rutaActual = Ruta.InicioCliente.ruta,
            alSeleccionarRuta = {}
        )
    }
}
