package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.titulo
import mx.donchambitas.app.util.TipoError

/**
 * Estados visuales discretos para el patron de pantalla con datos segun ARQUITECTURA.md.
 */
enum class TipoEstadoVisual {
    CARGANDO,
    ERROR,
    VACIO,
    CONTENIDO
}

/**
 * Resuelve el estado visual prioritario para evitar que se superpongan pantallas.
 * Orden de precedencia estricto:
 * 1. Cargando
 * 2. Error
 * 3. Vacio
 * 4. Contenido
 */
fun resolverEstadoVisual(
    cargando: Boolean,
    tieneError: Boolean,
    vacio: Boolean
): TipoEstadoVisual {
    return when {
        cargando -> TipoEstadoVisual.CARGANDO
        tieneError -> TipoEstadoVisual.ERROR
        vacio -> TipoEstadoVisual.VACIO
        else -> TipoEstadoVisual.CONTENIDO
    }
}

/**
 * Envoltura estandar para los cuatro estados de una pantalla con datos:
 * cargando, vacio, error y contenido.
 *
 * Aplica resolverEstadoVisual para garantizar que nunca se dibuje un estado encima de otro.
 */
@Composable
fun ContenedorEstado(
    cargando: Boolean,
    error: TipoError? = null,
    vacio: Boolean = false,
    alReintentar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    errorMensaje: String? = null,
    tituloVacio: String? = null,
    mensajeVacio: String? = null,
    iconoVacio: ImageVector = Icons.Outlined.Inbox,
    textoBotonVacio: String? = null,
    alHacerClickBotonVacio: (() -> Unit)? = null,
    vistaCargando: @Composable (() -> Unit)? = null,
    vistaError: @Composable ((TipoError?, String?) -> Unit)? = null,
    vistaVacia: @Composable (() -> Unit)? = null,
    contenido: @Composable () -> Unit
) {
    val tieneError = error != null || errorMensaje != null
    val estadoVisual = resolverEstadoVisual(
        cargando = cargando,
        tieneError = tieneError,
        vacio = vacio
    )

    Box(modifier = modifier) {
        when (estadoVisual) {
            TipoEstadoVisual.CARGANDO -> {
                if (vistaCargando != null) {
                    vistaCargando()
                } else {
                    Cargando()
                }
            }
            TipoEstadoVisual.ERROR -> {
                if (vistaError != null) {
                    vistaError(error, errorMensaje)
                } else if (error != null) {
                    EstadoError(
                        tipoError = error,
                        alReintentar = alReintentar,
                        mensajePersonalizado = errorMensaje
                    )
                } else if (errorMensaje != null) {
                    EstadoError(
                        mensaje = errorMensaje,
                        alReintentar = alReintentar
                    )
                }
            }
            TipoEstadoVisual.VACIO -> {
                if (vistaVacia != null) {
                    vistaVacia()
                } else {
                    EstadoVacio(
                        titulo = tituloVacio ?: stringResource(R.string.estado_vacio_titulo),
                        mensaje = mensajeVacio ?: stringResource(R.string.estado_vacio_mensaje),
                        icono = iconoVacio,
                        textoBoton = textoBotonVacio,
                        alHacerClickBoton = alHacerClickBotonVacio
                    )
                }
            }
            TipoEstadoVisual.CONTENIDO -> {
                contenido()
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(showBackground = true, name = "ContenedorEstado - Cargando")
@Composable
private fun ContenedorEstadoCargandoPreview() {
    DonChambitasTema {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Marfil)
        ) {
            ContenedorEstado(
                cargando = true,
                error = null,
                vacio = false,
                alReintentar = {}
            ) {
                Text(text = "Contenido de prueba")
            }
        }
    }
}

@Preview(showBackground = true, name = "ContenedorEstado - Error")
@Composable
private fun ContenedorEstadoErrorPreview() {
    DonChambitasTema {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Marfil)
        ) {
            ContenedorEstado(
                cargando = false,
                error = TipoError.RED,
                vacio = false,
                alReintentar = {}
            ) {
                Text(text = "Contenido de prueba")
            }
        }
    }
}

@Preview(showBackground = true, name = "ContenedorEstado - Vacio")
@Composable
private fun ContenedorEstadoVacioPreview() {
    DonChambitasTema {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Marfil)
        ) {
            ContenedorEstado(
                cargando = false,
                error = null,
                vacio = true,
                alReintentar = {}
            ) {
                Text(text = "Contenido de prueba")
            }
        }
    }
}

@Preview(showBackground = true, name = "ContenedorEstado - Contenido")
@Composable
private fun ContenedorEstadoContenidoPreview() {
    DonChambitasTema {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Marfil)
        ) {
            ContenedorEstado(
                cargando = false,
                error = null,
                vacio = false,
                alReintentar = {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Espaciado.margenPantalla)
                ) {
                    Text(
                        text = "Lista cargada correctamente",
                        style = DonChambitasTema.tipografia.titulo,
                        color = Tinta
                    )
                    Text(
                        text = "El contenido normal se muestra cuando no hay carga, error ni vacio.",
                        style = DonChambitasTema.tipografia.cuerpo,
                        color = Tinta
                    )
                }
            }
        }
    }
}
