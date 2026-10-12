package mx.donchambitas.app.ui.pantallas

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoContrasena
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.Cargando
import mx.donchambitas.app.ui.componentes.EstadoError
import mx.donchambitas.app.ui.tema.Advertencia
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.FormaChip
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Mostaza
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.titulo
import mx.donchambitas.app.util.TipoError

/** Lado del circulo de iniciales, por la seccion 2 de DISENO-CUENTA.md. */
private val LadoIniciales = 80.dp

/** Grosor del borde de la tarjeta y de los avisos. */
private val GrosorBorde = 1.dp

/** Tipos de error en los que volver a intentar puede servir de algo (seccion 6 de DISENO-AUTENTICACION.md). */
private val ERRORES_CON_REINTENTO = setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)

/**
 * Pantalla Mi cuenta (P-18).
 * Especificada en docs/producto/DISENO-CUENTA.md.
 *
 * El estado, la validacion y las llamadas a los repositorios viven en
 * [MiCuentaViewModel]; aqui solo se conecta con el contenido.
 *
 * @param llegaPorRecuperacion El usuario abrio el enlace del correo de
 *   recuperacion: la pantalla abre directo en la vista de contrasena (DEC-27).
 * @param alAtenderRecuperacion Se invoca una vez que esa llegada se atendio.
 * @param alIrAMiPerfil Lleva a P-11. Solo se ofrece al trabajador.
 * @param alIrAMisPostulaciones Lleva a P-14. Solo se ofrece al trabajador.
 */
@Composable
fun MiCuentaPantalla(
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier,
    llegaPorRecuperacion: Boolean = false,
    alAtenderRecuperacion: () -> Unit = {},
    viewModel: MiCuentaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(llegaPorRecuperacion) {
        if (llegaPorRecuperacion) {
            viewModel.alLlegarPorRecuperacion()
            alAtenderRecuperacion()
        }
    }

    // Desde una vista de edicion, atras regresa al resumen, no sale de P-18.
    BackHandler(enabled = estado.vista != VistaMiCuenta.RESUMEN, onBack = viewModel::alVolverAlResumen)

    MiCuentaContenido(
        estado = estado,
        alReintentarCarga = viewModel::alReintentarCarga,
        alAbrirEditarDatos = viewModel::alAbrirEditarDatos,
        alAbrirCambiarContrasena = viewModel::alAbrirCambiarContrasena,
        alVolverAlResumen = viewModel::alVolverAlResumen,
        alCambiarNombre = viewModel::alCambiarNombre,
        alCambiarApellidos = viewModel::alCambiarApellidos,
        alCambiarTelefono = viewModel::alCambiarTelefono,
        alSalirDeCampo = viewModel::alPerderFoco,
        alGuardarDatos = viewModel::alGuardarDatos,
        alEscribirContrasena = viewModel::alEscribirContrasena,
        alSalirDeContrasena = viewModel::alPerderFocoContrasena,
        alGuardarContrasena = viewModel::alGuardarContrasena,
        alReintentarFormulario = viewModel::alReintentarFormulario,
        alPedirCerrarSesion = viewModel::alPedirCerrarSesion,
        alCancelarCierre = viewModel::alCancelarCierre,
        alConfirmarCierre = viewModel::alConfirmarCierre,
        alIrAMiPerfil = alIrAMiPerfil,
        alIrAMisPostulaciones = alIrAMisPostulaciones,
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-18, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores.
 */
@Composable
fun MiCuentaContenido(
    estado: EstadoMiCuenta,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alAbrirEditarDatos: () -> Unit = {},
    alAbrirCambiarContrasena: () -> Unit = {},
    alVolverAlResumen: () -> Unit = {},
    alCambiarNombre: (String) -> Unit = {},
    alCambiarApellidos: (String) -> Unit = {},
    alCambiarTelefono: (String) -> Unit = {},
    alSalirDeCampo: (CampoMiCuenta) -> Unit = {},
    alGuardarDatos: () -> Unit = {},
    alEscribirContrasena: (String) -> Unit = {},
    alSalirDeContrasena: () -> Unit = {},
    alGuardarContrasena: () -> Unit = {},
    alReintentarFormulario: () -> Unit = {},
    alPedirCerrarSesion: () -> Unit = {},
    alCancelarCierre: () -> Unit = {},
    alConfirmarCierre: () -> Unit = {},
    alIrAMiPerfil: () -> Unit = {},
    alIrAMisPostulaciones: () -> Unit = {}
) {
    val titulo = when (estado.vista) {
        VistaMiCuenta.RESUMEN -> R.string.cuenta_titulo
        VistaMiCuenta.EDITAR_DATOS -> R.string.cuenta_editar_datos
        VistaMiCuenta.CAMBIAR_CONTRASENA -> R.string.cuenta_cambiar_contrasena
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(titulo),
                // El resumen es destino de la barra inferior: no lleva flecha.
                alRegresar = alVolverAlResumen.takeIf { estado.vista != VistaMiCuenta.RESUMEN }
            )
        },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        when {
            estado.vista == VistaMiCuenta.RESUMEN && estado.cargando ->
                Cargando(modifier = Modifier.padding(relleno))

            estado.vista == VistaMiCuenta.RESUMEN && estado.usuario == null ->
                EstadoError(
                    tipoError = estado.errorCarga ?: TipoError.DESCONOCIDO,
                    alReintentar = alReintentarCarga,
                    modifier = Modifier.padding(relleno)
                )

            else -> Column(
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp24),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
            ) {
                when (estado.vista) {
                    VistaMiCuenta.RESUMEN -> estado.usuario?.let { usuario ->
                        VistaResumen(
                            usuario = usuario,
                            estado = estado,
                            alAbrirEditarDatos = alAbrirEditarDatos,
                            alAbrirCambiarContrasena = alAbrirCambiarContrasena,
                            alPedirCerrarSesion = alPedirCerrarSesion,
                            alConfirmarCierre = alConfirmarCierre,
                            alIrAMiPerfil = alIrAMiPerfil,
                            alIrAMisPostulaciones = alIrAMisPostulaciones
                        )
                    }

                    VistaMiCuenta.EDITAR_DATOS -> VistaEditarDatos(
                        estado = estado,
                        alCambiarNombre = alCambiarNombre,
                        alCambiarApellidos = alCambiarApellidos,
                        alCambiarTelefono = alCambiarTelefono,
                        alSalirDeCampo = alSalirDeCampo,
                        alGuardar = alGuardarDatos,
                        alReintentar = alReintentarFormulario
                    )

                    VistaMiCuenta.CAMBIAR_CONTRASENA -> VistaCambiarContrasena(
                        estado = estado,
                        alEscribirContrasena = alEscribirContrasena,
                        alSalirDeContrasena = alSalirDeContrasena,
                        alGuardar = alGuardarContrasena,
                        alReintentar = alReintentarFormulario
                    )
                }
            }
        }
    }

    if (estado.confirmandoCierre) {
        AlertDialog(
            onDismissRequest = alCancelarCierre,
            title = {
                Text(
                    text = stringResource(R.string.cuenta_cerrar_pregunta),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Carbon
                )
            },
            confirmButton = {
                BotonTexto(texto = stringResource(R.string.cuenta_cerrar_sesion), onClick = alConfirmarCierre)
            },
            dismissButton = {
                BotonTexto(texto = stringResource(R.string.cuenta_cancelar), onClick = alCancelarCierre)
            },
            containerColor = Crema
        )
    }
}

/** Seccion 2 de DISENO-CUENTA.md. */
@Composable
private fun VistaResumen(
    usuario: Usuario,
    estado: EstadoMiCuenta,
    alAbrirEditarDatos: () -> Unit,
    alAbrirCambiarContrasena: () -> Unit,
    alPedirCerrarSesion: () -> Unit,
    alConfirmarCierre: () -> Unit,
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit
) {
    val habilitado = !estado.cerrandoSesion

    TarjetaFicha(usuario)

    estado.aviso?.let { Aviso(it) }

    BotonSecundario(
        texto = stringResource(R.string.cuenta_editar_datos),
        onClick = alAbrirEditarDatos,
        habilitado = habilitado
    )
    BotonSecundario(
        texto = stringResource(R.string.cuenta_cambiar_contrasena),
        onClick = alAbrirCambiarContrasena,
        habilitado = habilitado
    )

    if (usuario.rol == RolUsuario.TRABAJADOR) {
        BotonSecundario(
            texto = stringResource(R.string.cuenta_mi_perfil),
            onClick = alIrAMiPerfil,
            habilitado = habilitado
        )
        BotonSecundario(
            texto = stringResource(R.string.cuenta_mis_postulaciones),
            onClick = alIrAMisPostulaciones,
            habilitado = habilitado
        )
    }

    ErrorEnLinea(tipoError = estado.errorCierre, alReintentar = alConfirmarCierre)

    BotonSecundario(
        texto = stringResource(R.string.cuenta_cerrar_sesion),
        onClick = alPedirCerrarSesion,
        cargando = estado.cerrandoSesion,
        modifier = Modifier.padding(top = Espaciado.dp8)
    )
}

@Composable
private fun TarjetaFicha(usuario: Usuario) {
    val nombreCompleto = "${usuario.nombre} ${usuario.apellidos}".trim()
    val descripcionIniciales = stringResource(R.string.cuenta_iniciales_descripcion, nombreCompleto)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = Modifier
            .fillMaxWidth()
            .background(Arena, FormaTarjeta)
            .border(GrosorBorde, Borde, FormaTarjeta)
            .padding(Espaciado.rellenoTarjeta)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(LadoIniciales)
                .background(Mostaza, FormaFotoPerfil)
                .clearAndSetSemantics { contentDescription = descripcionIniciales }
        ) {
            Text(
                text = inicialesDe(usuario.nombre, usuario.apellidos),
                style = DonChambitasTema.tipografia.titulo,
                color = Carbon
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
            Text(
                text = nombreCompleto,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                color = Carbon
            )
            EtiquetaRol(usuario.rol)
            Text(
                text = usuario.correo,
                style = DonChambitasTema.tipografia.secundario,
                color = Cafe
            )
            Text(
                text = usuario.telefono ?: stringResource(R.string.cuenta_sin_telefono),
                style = DonChambitasTema.tipografia.secundario,
                color = Cafe
            )
        }
    }
}

/** Mostaza para el cliente y Advertencia para el trabajador, las dos con texto Carbon (WIREFRAMES.md, 7). */
@Composable
private fun EtiquetaRol(rol: RolUsuario) {
    val (fondo, texto) = when (rol) {
        RolUsuario.CLIENTE -> Mostaza to R.string.cuenta_rol_cliente
        RolUsuario.TRABAJADOR -> Advertencia to R.string.cuenta_rol_trabajador
    }
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.pie,
        color = Carbon,
        modifier = Modifier
            .background(fondo, FormaChip)
            .padding(horizontal = Espaciado.dp8, vertical = Espaciado.dp4)
    )
}

@Composable
private fun Aviso(aviso: AvisoMiCuenta) {
    @StringRes val texto = when (aviso) {
        AvisoMiCuenta.DATOS_GUARDADOS -> R.string.cuenta_aviso_datos
        AvisoMiCuenta.CONTRASENA_CAMBIADA -> R.string.cuenta_aviso_contrasena
    }
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.cuerpo,
        color = Exito,
        modifier = Modifier
            .fillMaxWidth()
            .background(Arena, FormaTarjeta)
            .border(GrosorBorde, Exito, FormaTarjeta)
            .padding(Espaciado.rellenoTarjeta)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

/** Seccion 3 de DISENO-CUENTA.md. Al guardar con errores, el foco va al primero en orden visual. */
@Composable
private fun VistaEditarDatos(
    estado: EstadoMiCuenta,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alSalirDeCampo: (CampoMiCuenta) -> Unit,
    alGuardar: () -> Unit,
    alReintentar: () -> Unit
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(onNext = { administradorFoco.moveFocus(FocusDirection.Down) })
    val solicitantesFoco = remember { CampoMiCuenta.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val guardar = {
        alGuardar()
        irAlPrimerError = true
    }

    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoMiCuenta.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    fun Modifier.campo(campo: CampoMiCuenta): Modifier =
        focusRequester(solicitantesFoco.getValue(campo)).alPerderFoco { alSalirDeCampo(campo) }

    val textoConMayusculas = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next
    )

    CampoTexto(
        valor = estado.nombre,
        alCambiarValor = alCambiarNombre,
        etiqueta = stringResource(R.string.registro_nombre),
        error = estado.errorNombre?.let { stringResource(it) },
        habilitado = !estado.enviando,
        modifier = Modifier.campo(CampoMiCuenta.NOMBRE),
        tecladoOpciones = textoConMayusculas,
        tecladoAcciones = siguienteCampo
    )
    CampoTexto(
        valor = estado.apellidos,
        alCambiarValor = alCambiarApellidos,
        etiqueta = stringResource(R.string.registro_apellidos),
        error = estado.errorApellidos?.let { stringResource(it) },
        habilitado = !estado.enviando,
        modifier = Modifier.campo(CampoMiCuenta.APELLIDOS),
        tecladoOpciones = textoConMayusculas,
        tecladoAcciones = siguienteCampo
    )
    CampoTexto(
        valor = estado.telefono,
        alCambiarValor = alCambiarTelefono,
        etiqueta = stringResource(R.string.registro_telefono),
        error = estado.errorTelefono?.let { stringResource(it) },
        habilitado = !estado.enviando,
        modifier = Modifier.campo(CampoMiCuenta.TELEFONO),
        tecladoOpciones = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        tecladoAcciones = KeyboardActions(
            onDone = {
                administradorFoco.clearFocus()
                if (!estado.enviando) guardar()
            }
        )
    )

    ErrorEnLinea(tipoError = estado.errorFormulario, alReintentar = alReintentar)

    BotonPrincipal(
        texto = stringResource(R.string.cuenta_guardar),
        onClick = guardar,
        cargando = estado.enviando
    )
}

private fun EstadoMiCuenta.errorDe(campo: CampoMiCuenta): Int? = when (campo) {
    CampoMiCuenta.NOMBRE -> errorNombre
    CampoMiCuenta.APELLIDOS -> errorApellidos
    CampoMiCuenta.TELEFONO -> errorTelefono
}

/** Seccion 4 de DISENO-CUENTA.md. Solo la contrasena nueva (DEC-27). */
@Composable
private fun VistaCambiarContrasena(
    estado: EstadoMiCuenta,
    alEscribirContrasena: (String) -> Unit,
    alSalirDeContrasena: () -> Unit,
    alGuardar: () -> Unit,
    alReintentar: () -> Unit
) {
    val administradorFoco = LocalFocusManager.current
    val solicitanteFoco = remember { FocusRequester() }
    var irAlError by remember { mutableStateOf(false) }
    val guardar = {
        alGuardar()
        irAlError = true
    }

    LaunchedEffect(irAlError) {
        if (!irAlError) return@LaunchedEffect
        irAlError = false
        if (estado.errorContrasena != null) solicitanteFoco.requestFocus()
    }

    Text(
        text = stringResource(R.string.cuenta_contrasena_explicacion),
        style = DonChambitasTema.tipografia.cuerpo,
        color = Cafe
    )

    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
        CampoContrasena(
            valor = estado.contrasena,
            alCambiarValor = alEscribirContrasena,
            etiqueta = stringResource(R.string.cuenta_contrasena_nueva),
            error = estado.errorContrasena?.let { stringResource(it) },
            habilitado = !estado.enviando,
            tecladoOpciones = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            tecladoAcciones = KeyboardActions(
                onDone = {
                    administradorFoco.clearFocus()
                    if (!estado.enviando) guardar()
                }
            ),
            modifier = Modifier
                .focusRequester(solicitanteFoco)
                .alPerderFoco(alSalirDeContrasena)
        )
        Text(
            text = stringResource(R.string.registro_ayuda_contrasena),
            style = DonChambitasTema.tipografia.pie,
            color = Cafe
        )
    }

    ErrorEnLinea(tipoError = estado.errorFormulario, alReintentar = alReintentar)

    BotonPrincipal(
        texto = stringResource(R.string.cuenta_contrasena_guardar),
        onClick = guardar,
        cargando = estado.enviando
    )
}

/**
 * Error que no pertenece a un campo, en linea sobre el boton y sin tapar lo
 * escrito. "Reintentar" solo donde volver a intentar puede servir de algo.
 */
@Composable
private fun ErrorEnLinea(tipoError: TipoError?, alReintentar: () -> Unit) {
    if (tipoError == null) return
    EstadoError(
        tipoError = tipoError,
        alReintentar = alReintentar.takeIf { tipoError in ERRORES_CON_REINTENTO }
    )
}

/** Primera letra del nombre y del primer apellido, en mayusculas. */
internal fun inicialesDe(nombre: String, apellidos: String): String =
    listOf(nombre, apellidos)
        .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
        .joinToString("")

private val UsuarioDeMuestra = Usuario(
    id = "muestra",
    correo = "refugio@ejemplo.mx",
    nombre = "Refugio",
    apellidos = "Hernández Luna",
    telefono = "4771234567",
    rol = RolUsuario.TRABAJADOR,
    creadoEn = Instant.EPOCH,
    actualizadoEn = Instant.EPOCH
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 resumen de trabajador")
@Composable
private fun MiCuentaResumenPreview() {
    DonChambitasTema {
        MiCuentaContenido(
            estado = EstadoMiCuenta(
                cargando = false,
                usuario = UsuarioDeMuestra,
                aviso = AvisoMiCuenta.DATOS_GUARDADOS
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 sin conexión al cargar")
@Composable
private fun MiCuentaErrorDeCargaPreview() {
    DonChambitasTema {
        MiCuentaContenido(estado = EstadoMiCuenta(cargando = false, errorCarga = TipoError.RED))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 editar datos con errores")
@Composable
private fun MiCuentaEditarDatosPreview() {
    DonChambitasTema {
        MiCuentaContenido(
            estado = EstadoMiCuenta(
                vista = VistaMiCuenta.EDITAR_DATOS,
                cargando = false,
                usuario = UsuarioDeMuestra,
                nombre = "",
                apellidos = "Hernández Luna",
                telefono = "47712",
                errorNombre = R.string.validacion_nombre_vacio,
                errorTelefono = R.string.validacion_telefono_digitos
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 cambiar contraseña")
@Composable
private fun MiCuentaCambiarContrasenaPreview() {
    DonChambitasTema {
        MiCuentaContenido(
            estado = EstadoMiCuenta(
                vista = VistaMiCuenta.CAMBIAR_CONTRASENA,
                cargando = false,
                usuario = UsuarioDeMuestra,
                contrasena = "secreta123",
                errorFormulario = TipoError.RED
            )
        )
    }
}
