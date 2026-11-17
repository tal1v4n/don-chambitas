package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import mx.donchambitas.app.ui.componentes.ArcosCasco
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.portada
import mx.donchambitas.app.ui.tema.secundario
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import mx.donchambitas.app.ui.componentes.AvisoError
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.util.TipoError

/** Lado del icono de la confirmacion, por 4.2 de DISENO-AUTENTICACION.md. */
private val LadoIconoEnviado = 56.dp

/** Alto minimo tactil de los enlaces de navegacion, por 1.8 de DISENO-AUTENTICACION.md. */
private val AltoMinimoEnlace = 48.dp

/** Lado de la ilustracion con arcos que abre las dos vistas de P-04. */
private val LadoIlustracion = 220.dp

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
            // El titulo va en el cuerpo, bajo la ilustracion; la barra solo regresa.
            BarraSuperior(titulo = "", alRegresar = alRegresar)
        },
        containerColor = Marfil,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = Espaciado.dp24,
                    vertical = Espaciado.dp8
                ),
            horizontalAlignment = Alignment.CenterHorizontally
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

    Ilustracion(
        icono = Icons.Outlined.Key,
        fondo = Amarillo,
        tinte = Tinta,
        descripcion = null
    )
    Text(
        text = stringResource(R.string.recuperar_titulo),
        style = DonChambitasTema.tipografia.portada,
        color = Tinta,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = Espaciado.dp12)
    )
    Text(
        text = stringResource(R.string.recuperar_explicacion),
        style = DonChambitasTema.tipografia.cuerpo,
        color = Gris,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = Espaciado.dp32)
    )

    // Es el unico campo, asi que es tambien el ultimo: Done envia, como la
    // contrasena en P-02 (1.7).
    CampoTexto(
        valor = estado.correo,
        alCambiarValor = alCambiarCorreo,
        etiqueta = stringResource(R.string.auth_correo),
        error = estado.errorCorreo?.let { stringResource(it) },
        habilitado = !estado.cargando,
        iconoInicio = { Icon(Icons.Outlined.MailOutline, contentDescription = null) },
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
        icono = Icons.AutoMirrored.Outlined.Send,
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
        Ilustracion(
            icono = Icons.Outlined.MarkEmailRead,
            fondo = Exito,
            tinte = Blanco,
            descripcion = stringResource(R.string.recuperar_enviado_descripcion)
        )
        // De que trata la pantalla, como antetitulo: el titulo grande es la confirmacion.
        Text(
            text = stringResource(R.string.recuperar_titulo),
            style = DonChambitasTema.tipografia.pie,
            fontWeight = FontWeight.SemiBold,
            color = Gris,
            modifier = Modifier.padding(bottom = Espaciado.dp4)
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_titulo),
            style = DonChambitasTema.tipografia.portada,
            color = Tinta,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Espaciado.dp12)
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_mensaje),
            style = DonChambitasTema.tipografia.cuerpo,
            color = Gris,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Espaciado.dp24)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
            modifier = Modifier
                .clip(CircleShape)
                .background(AmarilloSuave)
                .padding(horizontal = Espaciado.dp16, vertical = Espaciado.dp12)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Tinta,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.recuperar_vigencia),
                style = DonChambitasTema.tipografia.secundario,
                fontWeight = FontWeight.Medium,
                color = Tinta
            )
        }
    }
}

/**
 * Icono grande en un circulo de color, rodeado por los arcos de la marca.
 * Abre P-04 en sus dos vistas: la llave del formulario y el sobre de la
 * confirmacion.
 */
@Composable
private fun Ilustracion(
    icono: ImageVector,
    fondo: Color,
    tinte: Color,
    descripcion: String?
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(top = Espaciado.dp8, bottom = Espaciado.dp24)
            .size(LadoIlustracion)
    ) {
        ArcosCasco(
            color = Amarillo.copy(alpha = 0.45f),
            anillos = 3,
            grosor = 6.dp,
            centro = Offset(0.5f, 0.5f),
            radioInicial = LadoIconoEnviado - 6.dp,
            modifier = Modifier.matchParentSize()
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(LadoIconoEnviado * 2)
                .clip(CircleShape)
                .background(fondo)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                tint = tinte,
                modifier = Modifier.size(LadoIconoEnviado)
            )
        }
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

    AvisoError(
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
