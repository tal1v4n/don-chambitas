package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.EstadoError
import mx.donchambitas.app.ui.tema.Arena
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Cafe
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

/** Lado del icono de la confirmacion, por 4.2 de DISENO-AUTENTICACION.md. */
private val LadoIconoEnviado = 56.dp

/** Alto minimo tactil de los enlaces de navegacion, por 1.8 de DISENO-AUTENTICACION.md. */
private val AltoMinimoEnlace = 48.dp

/** Grosor del borde del aviso de vigencia. */
private val GrosorBordeAviso = 1.dp

/**
 * Pantalla de recuperar contrasena (P-04).
 * Especificada en docs/producto/DISENO-AUTENTICACION.md, seccion 4.
 *
 * El estado, la validacion y la llamada a RepositorioAuth viven en
 * [RecuperarContrasenaViewModel]; aqui solo se conecta con el contenido.
 *
 * @param alRegresar Vuelve a P-02. Lo usan la flecha y "Volver a iniciar
 *   sesion", desde el formulario y desde la confirmacion.
 */
@Composable
fun RecuperarContrasenaPantalla(
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecuperarContrasenaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    RecuperarContrasenaContenido(
        estado = estado,
        alCambiarCorreo = viewModel::alCambiarCorreo,
        alSalirDelCorreo = viewModel::alPerderFocoCorreo,
        alEnviar = viewModel::alEnviar,
        alReintentar = viewModel::alReintentar,
        alRegresar = alRegresar,
        modifier = modifier
    )
}

/**
 * Contenido visual puro de P-04, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores.
 *
 * Pinta el formulario (4.1) o, con `enviado`, la confirmacion (4.2). La barra
 * superior y "Volver a iniciar sesion" se quedan en las dos.
 */
@Composable
fun RecuperarContrasenaContenido(
    estado: EstadoRecuperarContrasena,
    alCambiarCorreo: (String) -> Unit,
    alEnviar: () -> Unit,
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alSalirDelCorreo: () -> Unit = {},
    alReintentar: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.recuperar_titulo),
                alRegresar = alRegresar
            )
        },
        containerColor = Crema,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = Espaciado.margenPantalla,
                    vertical = Espaciado.dp24
                )
        ) {
            if (estado.enviado) {
                VistaConfirmacion()
            } else {
                VistaFormulario(
                    estado = estado,
                    alCambiarCorreo = alCambiarCorreo,
                    alSalirDelCorreo = alSalirDelCorreo,
                    alEnviar = alEnviar,
                    alReintentar = alReintentar
                )
            }

            BotonTexto(
                texto = stringResource(R.string.recuperar_volver),
                onClick = alRegresar,
                habilitado = !estado.cargando,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Espaciado.dp24)
                    .defaultMinSize(minHeight = AltoMinimoEnlace)
            )
        }
    }
}

/** Elementos 2 a 5 de la tabla 4.1. Al enviar con el correo mal, el foco vuelve al campo (5.4). */
@Composable
private fun VistaFormulario(
    estado: EstadoRecuperarContrasena,
    alCambiarCorreo: (String) -> Unit,
    alSalirDelCorreo: () -> Unit,
    alEnviar: () -> Unit,
    alReintentar: (() -> Unit)?
) {
    val administradorFoco = LocalFocusManager.current
    val solicitanteFoco = remember { FocusRequester() }
    var irAlError by remember { mutableStateOf(false) }
    val enviar = {
        alEnviar()
        irAlError = true
    }

    LaunchedEffect(irAlError) {
        if (!irAlError) return@LaunchedEffect
        irAlError = false
        if (estado.errorCorreo != null) solicitanteFoco.requestFocus()
    }

    Text(
        text = stringResource(R.string.recuperar_explicacion),
        style = DonChambitasTema.tipografia.cuerpo,
        color = Cafe,
        modifier = Modifier.padding(bottom = Espaciado.dp24)
    )

    // Es el unico campo, asi que es tambien el ultimo: Done envia, como la
    // contrasena en P-02 (1.7).
    CampoTexto(
        valor = estado.correo,
        alCambiarValor = alCambiarCorreo,
        etiqueta = stringResource(R.string.auth_correo),
        error = estado.errorCorreo?.let { stringResource(it) },
        habilitado = !estado.cargando,
        tecladoOpciones = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        tecladoAcciones = KeyboardActions(
            onDone = {
                administradorFoco.clearFocus()
                if (!estado.cargando) enviar()
            }
        ),
        modifier = Modifier
            .focusRequester(solicitanteFoco)
            .alPerderFoco(alSalirDelCorreo)
    )

    ErrorDeRecuperacion(
        tipoError = estado.errorPantalla,
        alReintentar = alReintentar,
        modifier = Modifier.padding(vertical = Espaciado.dp16)
    )

    BotonPrincipal(
        texto = stringResource(R.string.recuperar_accion),
        onClick = enviar,
        cargando = estado.cargando,
        modifier = Modifier
            .fillMaxWidth()
            // Con error, la separacion ya la pone el bloque de error (16 dp).
            .padding(top = if (estado.errorPantalla == null) Espaciado.dp24 else 0.dp)
    )
}

/**
 * Tabla 4.2. No repite el correo escrito: en una pantalla que no verifico
 * nada, "te enviamos un enlace a juan@..." refuerza justo la lectura de que
 * la cuenta existe.
 */
@Composable
private fun VistaConfirmacion() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.MarkEmailRead,
            contentDescription = stringResource(R.string.recuperar_enviado_descripcion),
            tint = Exito,
            modifier = Modifier
                .padding(top = Espaciado.dp32, bottom = Espaciado.dp16)
                .size(LadoIconoEnviado)
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_titulo),
            style = DonChambitasTema.tipografia.subtitulo,
            color = Carbon,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Espaciado.dp8)
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_mensaje),
            style = DonChambitasTema.tipografia.cuerpo,
            color = Cafe,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Espaciado.dp24)
        )
        Text(
            text = stringResource(R.string.recuperar_vigencia),
            style = DonChambitasTema.tipografia.cuerpo,
            color = Carbon,
            modifier = Modifier
                .fillMaxWidth()
                .background(Arena, FormaTarjeta)
                .border(GrosorBordeAviso, Borde, FormaTarjeta)
                .padding(Espaciado.rellenoTarjeta)
        )
    }
}

/**
 * Error que no pertenece al campo, en linea sobre el boton y sin tapar el
 * formulario. Mensajes de S1-T11 tal cual, sin sustituciones (seccion 6).
 */
@Composable
private fun ErrorDeRecuperacion(
    tipoError: TipoError?,
    alReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (tipoError == null) return

    // Misma regla que P-02: reintentar solo donde volver a intentar puede
    // servir de algo. En VALIDACION lo que hay que cambiar es lo escrito.
    val reintento = alReintentar.takeIf {
        tipoError in setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)
    }

    EstadoError(
        tipoError = tipoError,
        alReintentar = reintento,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-04 vacío")
@Composable
private fun RecuperarContrasenaVacioPreview() {
    DonChambitasTema {
        RecuperarContrasenaContenido(
            estado = EstadoRecuperarContrasena(),
            alCambiarCorreo = {},
            alEnviar = {},
            alRegresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-04 correo mal escrito")
@Composable
private fun RecuperarContrasenaConErrorPreview() {
    DonChambitasTema {
        RecuperarContrasenaContenido(
            estado = EstadoRecuperarContrasena(
                correo = "refugio@",
                errorCorreo = R.string.validacion_correo_formato
            ),
            alCambiarCorreo = {},
            alEnviar = {},
            alRegresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-04 cargando")
@Composable
private fun RecuperarContrasenaCargandoPreview() {
    DonChambitasTema {
        RecuperarContrasenaContenido(
            estado = EstadoRecuperarContrasena(correo = "refugio@ejemplo.mx", cargando = true),
            alCambiarCorreo = {},
            alEnviar = {},
            alRegresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-04 sin red")
@Composable
private fun RecuperarContrasenaSinRedPreview() {
    DonChambitasTema {
        RecuperarContrasenaContenido(
            estado = EstadoRecuperarContrasena(
                correo = "refugio@ejemplo.mx",
                errorPantalla = TipoError.RED
            ),
            alCambiarCorreo = {},
            alEnviar = {},
            alRegresar = {},
            alReintentar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-04 confirmación")
@Composable
private fun RecuperarContrasenaEnviadoPreview() {
    DonChambitasTema {
        RecuperarContrasenaContenido(
            estado = EstadoRecuperarContrasena(correo = "refugio@ejemplo.mx", enviado = true),
            alCambiarCorreo = {},
            alEnviar = {},
            alRegresar = {}
        )
    }
}
