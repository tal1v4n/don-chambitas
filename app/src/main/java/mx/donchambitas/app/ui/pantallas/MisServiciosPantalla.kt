package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonDestacado
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.TarjetaServicio
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaHojaInferior
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.subtitulo

/** Aire bajo la lista para que el boton flotante no tape la ultima tarjeta (1.1). */
private val RellenoInferiorLista = 88.dp

/**
 * Pantalla Mis servicios del trabajador (P-12).
 * Especificada en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 3.
 *
 * Mientras no exista su ViewModel (S3-T06) no carga servicios, asi que se ve
 * vacia; aqui solo se abren y cierran la hoja de acciones y la confirmacion.
 * Pausar, reactivar y eliminar todavia no hacen nada.
 *
 * @param alPublicar Abre P-13 en modo crear.
 * @param alEditar Abre P-13 con el servicio.
 */
@Composable
fun MisServiciosPantalla(
    alPublicar: () -> Unit,
    alEditar: (String) -> Unit,
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    var estado by remember { mutableStateOf(EstadoMisServicios(cargando = false)) }

    MisServiciosContenido(
        estado = estado,
        alAbrirAcciones = { estado = estado.copy(servicioEnAcciones = it) },
        alCerrarAcciones = { estado = estado.copy(servicioEnAcciones = null) },
        alEditar = {
            estado = estado.copy(servicioEnAcciones = null)
            alEditar(it.id)
        },
        alCambiarActivo = { estado = estado.copy(servicioEnAcciones = null) },
        alPedirEliminar = { estado = estado.copy(servicioEnAcciones = null, servicioPorEliminar = it) },
        alConfirmarEliminar = { estado = estado.copy(servicioPorEliminar = null) },
        alCancelarEliminar = { estado = estado.copy(servicioPorEliminar = null) },
        alPublicar = alPublicar,
        alIrAMiPerfil = alIrAMiPerfil,
        alIrAMisPostulaciones = alIrAMisPostulaciones,
        alConsumirAviso = { estado = estado.copy(aviso = null) },
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-12, sin estado propio. Un callback por evento de
 * la seccion 3.3.
 */
@Composable
fun MisServiciosContenido(
    estado: EstadoMisServicios,
    alAbrirAcciones: (Servicio) -> Unit,
    alCerrarAcciones: () -> Unit,
    alEditar: (Servicio) -> Unit,
    alCambiarActivo: (Servicio) -> Unit,
    alPedirEliminar: (Servicio) -> Unit,
    alConfirmarEliminar: () -> Unit,
    alCancelarEliminar: () -> Unit,
    alPublicar: () -> Unit,
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier,
    alReintentar: () -> Unit = {},
    alConsumirAviso: () -> Unit = {}
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = estado.aviso?.let { stringResource(it) }
    val vacia = estado.servicios.isEmpty()

    LaunchedEffect(textoAviso) {
        if (textoAviso == null) return@LaunchedEffect
        alConsumirAviso()
        avisos.showSnackbar(textoAviso)
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.mis_servicios_titulo)) },
        floatingActionButton = {
            // En el vacio no: el estado vacio ya ofrece la misma accion (3.5).
            if (!estado.cargando && estado.error == null && !vacia) {
                BotonDestacado(
                    texto = stringResource(R.string.mis_servicios_publicar),
                    onClick = alPublicar
                )
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        Column(modifier = Modifier.padding(relleno)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Espaciado.dp8, vertical = Espaciado.dp4),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BotonTexto(texto = stringResource(R.string.mi_cuenta_mi_perfil), onClick = alIrAMiPerfil)
                BotonTexto(texto = stringResource(R.string.mi_cuenta_mis_postulaciones), onClick = alIrAMisPostulaciones)
            }

            ContenedorEstado(
                cargando = estado.cargando,
                error = estado.error,
                vacio = vacia,
                alReintentar = alReintentar,
                tituloVacio = stringResource(R.string.mis_servicios_vacio_titulo),
                mensajeVacio = stringResource(R.string.mis_servicios_vacio_mensaje),
                iconoVacio = Icons.Outlined.Handyman,
                textoBotonVacio = stringResource(R.string.mis_servicios_vacio_accion),
                alHacerClickBotonVacio = alPublicar
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Espaciado.margenPantalla,
                        end = Espaciado.margenPantalla,
                        bottom = RellenoInferiorLista
                    ),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.dp12)
                ) {
                    item { Resumen(total = estado.servicios.size, pausados = estado.pausados) }
                    items(estado.servicios, key = { it.id }) { servicio ->
                        TarjetaServicio(
                            titulo = servicio.titulo,
                            categoria = estado.categorias[servicio.categoriaId].orEmpty(),
                            precioTexto = textoPrecio(servicio),
                            pausado = !servicio.activo,
                            onClick = { if (estado.procesandoId != servicio.id) alAbrirAcciones(servicio) }
                        )
                    }
                }
            }
        }
    }

    estado.servicioEnAcciones?.let { servicio ->
        HojaAcciones(
            servicio = servicio,
            alEditar = { alEditar(servicio) },
            alCambiarActivo = { alCambiarActivo(servicio) },
            alEliminar = { alPedirEliminar(servicio) },
            alCerrar = alCerrarAcciones
        )
    }

    estado.servicioPorEliminar?.let { servicio ->
        AlertDialog(
            onDismissRequest = alCancelarEliminar,
            title = { Text(stringResource(R.string.mis_servicios_eliminar_titulo)) },
            text = { Text(stringResource(R.string.mis_servicios_eliminar_texto, servicio.titulo)) },
            confirmButton = {
                BotonTexto(texto = stringResource(R.string.mis_servicios_eliminar), onClick = alConfirmarEliminar)
            },
            dismissButton = {
                BotonTexto(texto = stringResource(R.string.trabajador_cancelar), onClick = alCancelarEliminar)
            },
            containerColor = Crema
        )
    }
}

/** "3 servicios · 1 pausado", o solo "3 servicios" si no hay pausados. */
@Composable
private fun Resumen(total: Int, pausados: Int) {
    val servicios = pluralStringResource(R.plurals.mis_servicios_resumen, total, total)
    val texto = if (pausados == 0) {
        servicios
    } else {
        stringResource(
            R.string.mis_servicios_resumen_con_pausados,
            servicios,
            pluralStringResource(R.plurals.mis_servicios_pausados, pausados, pausados)
        )
    }
    Text(text = texto, style = DonChambitasTema.tipografia.pie, color = Cafe)
}

/** El precio como lo pide la tabla 3.4; vacio si el servicio no tiene. */
@Composable
private fun textoPrecio(servicio: Servicio): String {
    val precio = when (val tipo = precioDeServicio(servicio.precioDesde, servicio.precioHasta)) {
        is PrecioServicio.Rango -> stringResource(R.string.precio_rango, tipo.desde, tipo.hasta)
        is PrecioServicio.Desde -> stringResource(R.string.precio_desde, tipo.desde)
        is PrecioServicio.Hasta -> stringResource(R.string.precio_hasta, tipo.hasta)
        null -> return ""
    }
    val unidad = servicio.unidadPrecio?.trim().orEmpty()
    return if (unidad.isEmpty()) precio else stringResource(R.string.precio_con_unidad, precio, unidad)
}

/** Hoja con las tres acciones de un servicio (3.3). Eliminar va en color Error. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaAcciones(
    servicio: Servicio,
    alEditar: () -> Unit,
    alCambiarActivo: () -> Unit,
    alEliminar: () -> Unit,
    alCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        shape = FormaHojaInferior,
        containerColor = Crema
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Espaciado.margenPantalla)
                .padding(bottom = Espaciado.dp24),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Text(text = servicio.titulo, style = DonChambitasTema.tipografia.subtitulo, color = Carbon)
            BotonSecundario(texto = stringResource(R.string.mis_servicios_editar), onClick = alEditar)
            BotonSecundario(
                texto = stringResource(
                    if (servicio.activo) R.string.mis_servicios_pausar else R.string.mis_servicios_reactivar
                ),
                onClick = alCambiarActivo
            )
            BotonSecundario(
                texto = stringResource(R.string.mis_servicios_eliminar),
                onClick = alEliminar,
                color = Error
            )
        }
    }
}

private fun servicioDeMuestra(
    id: String,
    titulo: String,
    desde: Int?,
    hasta: Int?,
    unidad: String? = null,
    activo: Boolean = true
) = Servicio(
    id = id,
    perfilId = "usr-trab-1",
    categoriaId = 1,
    titulo = titulo,
    descripcion = "",
    precioDesde = desde?.let(::BigDecimal),
    precioHasta = hasta?.let(::BigDecimal),
    unidadPrecio = unidad,
    activo = activo,
    creadoEn = Instant.EPOCH,
    actualizadoEn = Instant.EPOCH
)

private val ServiciosDeMuestra = EstadoMisServicios(
    cargando = false,
    servicios = listOf(
        servicioDeMuestra("s1", "Reparación de fugas", 300, 800, "por trabajo"),
        servicioDeMuestra("s2", "Instalación de boiler", 1200, null),
        servicioDeMuestra("s3", "Destape de drenaje", null, null, activo = false)
    ),
    categorias = mapOf(1 to "Plomería")
)

@Composable
private fun MisServiciosPreview(estado: EstadoMisServicios) {
    DonChambitasTema {
        MisServiciosContenido(
            estado = estado,
            alAbrirAcciones = {},
            alCerrarAcciones = {},
            alEditar = {},
            alCambiarActivo = {},
            alPedirEliminar = {},
            alConfirmarEliminar = {},
            alCancelarEliminar = {},
            alPublicar = {},
            alIrAMiPerfil = {},
            alIrAMisPostulaciones = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-12 con servicios")
@Composable
private fun MisServiciosConDatosPreview() {
    MisServiciosPreview(ServiciosDeMuestra)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-12 vacía")
@Composable
private fun MisServiciosVaciaPreview() {
    MisServiciosPreview(EstadoMisServicios(cargando = false))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-12 acciones de un pausado")
@Composable
private fun MisServiciosAccionesPreview() {
    MisServiciosPreview(ServiciosDeMuestra.copy(servicioEnAcciones = ServiciosDeMuestra.servicios.last()))
}
