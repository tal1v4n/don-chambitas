package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import mx.donchambitas.app.ui.componentes.ArcosCasco
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.IconoAvanzar
import mx.donchambitas.app.ui.componentes.MedallaMarca
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.portada
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoContrasena
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.AvisoError
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.util.TipoError

/** Lado de la medalla del casco en P-02. En P-01 mide 130 dp; aqui comparte espacio con el saludo. */
private val LadoIsotipo = 48.dp

/** Radio de la esquina inferior de la portada amarilla. */
private val RadioPortada = 48.dp

/** Alto minimo tactil de los enlaces de navegacion, por 1.8 de DISENO-AUTENTICACION.md. */
private val AltoMinimoEnlace = 48.dp

/** Campos de P-02, en orden visual: el foco salta al primero que falle. */
enum class CampoIniciarSesion { CORREO, CONTRASENA }

/**
 * Pantalla de inicio de sesion (P-02).
 * Especificada en docs/producto/DISENO-AUTENTICACION.md, seccion 2.
 *
 * El estado, la validacion y la llamada a RepositorioAuth viven en
 * [IniciarSesionViewModel]; aqui solo se conecta con el contenido y se
 * entrega el destino una sola vez.
 *
 * @param alEntrar Se invoca con la pantalla de inicio del rol cuando la sesion queda abierta.
 * @param alIrARegistro Lleva a P-03. No pasa por el ViewModel: no hay nada que decidir.
 * @param alIrARecuperarContrasena Lleva a P-04, por la misma razon.
 */
@Composable
fun IniciarSesionPantalla(
    alEntrar: (Ruta) -> Unit,
    alIrARegistro: () -> Unit,
    alIrARecuperarContrasena: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IniciarSesionViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.destino) {
        val destino = estado.destino ?: return@LaunchedEffect
        alEntrar(destino)
        viewModel.alConsumirDestino()
    }

    IniciarSesionContenido(
        estado = estado,
        alCambiarCorreo = viewModel::alCambiarCorreo,
        alCambiarContrasena = viewModel::alCambiarContrasena,
        alSalirDeCampo = viewModel::alPerderFoco,
        alIniciarSesion = viewModel::alIniciarSesion,
        alReintentar = viewModel::alReintentar,
        alIrARegistro = alIrARegistro,
        alIrARecuperarContrasena = alIrARecuperarContrasena,
        modifier = modifier
    )
}

private fun EstadoIniciarSesion.errorDe(campo: CampoIniciarSesion): Int? = when (campo) {
    CampoIniciarSesion.CORREO -> errorCorreo
    CampoIniciarSesion.CONTRASENA -> errorContrasena
}

/**
 * Contenido visual puro de P-02, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores.
 *
 * Al enviar, le pone el foco al primer campo con error (5.4).
 */
@Composable
fun IniciarSesionContenido(
    estado: EstadoIniciarSesion,
    alCambiarCorreo: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alIniciarSesion: () -> Unit,
    alIrARegistro: () -> Unit,
    alIrARecuperarContrasena: () -> Unit,
    modifier: Modifier = Modifier,
    alSalirDeCampo: (CampoIniciarSesion) -> Unit = {},
    alReintentar: (() -> Unit)? = null
) {
    val administradorFoco = LocalFocusManager.current
    val solicitantesFoco = remember { CampoIniciarSesion.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val iniciarSesion = {
        alIniciarSesion()
        irAlPrimerError = true
    }

    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        CampoIniciarSesion.entries.firstOrNull { estado.errorDe(it) != null }
            ?.let { solicitantesFoco.getValue(it).requestFocus() }
    }

    fun Modifier.campo(campo: CampoIniciarSesion): Modifier = this
        .focusRequester(solicitantesFoco.getValue(campo))
        .alPerderFoco { alSalirDeCampo(campo) }

    Scaffold(
        // Sin barra superior ni flecha de regreso: P-02 es la raiz del subgrafo
        // (P-01 sale de la pila en S1-T15) y la portada amarilla ocupa su lugar.
        containerColor = Marfil,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            PortadaIniciarSesion()

            Column(
                modifier = Modifier.padding(
                    start = Espaciado.margenPantalla,
                    end = Espaciado.margenPantalla,
                    top = Espaciado.dp32,
                    bottom = Espaciado.dp24
                )
            ) {
                CampoTexto(
                    valor = estado.correo,
                    alCambiarValor = alCambiarCorreo,
                    etiqueta = stringResource(R.string.auth_correo),
                    error = estado.errorCorreo?.let { stringResource(it) },
                    habilitado = !estado.cargando,
                    iconoInicio = { Icon(Icons.Outlined.MailOutline, contentDescription = null) },
                    tecladoOpciones = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = KeyboardActions(
                        onNext = { administradorFoco.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .padding(bottom = Espaciado.dp12)
                        .campo(CampoIniciarSesion.CORREO)
                )

                CampoContrasena(
                    valor = estado.contrasena,
                    alCambiarValor = alCambiarContrasena,
                    etiqueta = stringResource(R.string.auth_contrasena),
                    error = estado.errorContrasena?.let { stringResource(it) },
                    habilitado = !estado.cargando,
                    tecladoOpciones = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    tecladoAcciones = KeyboardActions(
                        onDone = {
                            administradorFoco.clearFocus()
                            if (!estado.cargando) iniciarSesion()
                        }
                    ),
                    modifier = Modifier.campo(CampoIniciarSesion.CONTRASENA)
                )

                BotonTexto(
                    texto = stringResource(R.string.iniciar_sesion_olvide),
                    onClick = alIrARecuperarContrasena,
                    habilitado = !estado.cargando,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = Espaciado.dp4)
                        .defaultMinSize(minHeight = AltoMinimoEnlace)
                )

                ErrorDeInicioSesion(
                    tipoError = estado.errorPantalla,
                    alReintentar = alReintentar,
                    modifier = Modifier.padding(vertical = Espaciado.dp12)
                )

                BotonPrincipal(
                    texto = stringResource(R.string.iniciar_sesion_accion),
                    onClick = iniciarSesion,
                    cargando = estado.cargando,
                    icono = IconoAvanzar,
                    modifier = Modifier.padding(top = Espaciado.dp12)
                )

                SeparadorConTexto(modifier = Modifier.padding(vertical = Espaciado.dp24))

                BotonSecundario(
                    texto = stringResource(R.string.iniciar_sesion_ir_registro),
                    onClick = alIrARegistro,
                    habilitado = !estado.cargando
                )
            }
        }
    }
}

/**
 * Portada amarilla de P-02: medalla del casco, nombre de la app, saludo grande
 * y los arcos de la marca asomando por la esquina. La esquina inferior derecha
 * va muy redondeada para que el formulario "entre" a la portada.
 */
@Composable
private fun PortadaIniciarSesion(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomEnd = RadioPortada))
            .background(Amarillo)
    ) {
        ArcosCasco(
            color = Blanco.copy(alpha = 0.35f),
            modifier = Modifier
                .matchParentSize()
        )
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    start = Espaciado.dp24,
                    end = Espaciado.dp24,
                    top = Espaciado.dp24,
                    bottom = Espaciado.dp48
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
            ) {
                MedallaMarca(lado = LadoIsotipo)
                Text(
                    text = stringResource(id = R.string.splash_nombre_app),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    color = Tinta,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.iniciar_sesion_saludo),
                style = DonChambitasTema.tipografia.portada,
                color = Tinta,
                modifier = Modifier.padding(top = Espaciado.dp48)
            )
            Text(
                text = stringResource(R.string.iniciar_sesion_bajada),
                style = DonChambitasTema.tipografia.cuerpo,
                color = Tinta.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(top = Espaciado.dp8)
                    .fillMaxWidth(0.8f)
            )
        }
    }
}

/** Linea fina con un punto Amarillo al centro: separa entrar de registrarse sin mas texto. */
@Composable
private fun SeparadorConTexto(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Borde)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Amarillo)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Borde)
    }
}

/**
 * Error que no pertenece a ningun campo, pintado en linea sobre el boton y sin
 * tapar el formulario. El mapeo de TipoError es el de S1-T11; la unica
 * sustitucion es AUTENTICACION, cuyo mensaje generico ("Inicia sesion de nuevo
 * para continuar") no dice nada util justo en la pantalla de inicio de sesion.
 */
@Composable
private fun ErrorDeInicioSesion(
    tipoError: TipoError?,
    alReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (tipoError == null) return

    // Reintentar solo donde volver a intentar puede servir de algo. En
    // AUTENTICACION y VALIDACION lo que hay que cambiar es lo que esta escrito.
    val reintento = alReintentar.takeIf {
        tipoError in setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)
    }

    AvisoError(
        tipoError = tipoError,
        alReintentar = reintento,
        mensajePersonalizado = if (tipoError == TipoError.AUTENTICACION) {
            stringResource(R.string.error_credenciales_invalidas)
        } else {
            null
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-02 vacío")
@Composable
private fun IniciarSesionVacioPreview() {
    DonChambitasTema {
        IniciarSesionContenido(
            estado = EstadoIniciarSesion(),
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alIniciarSesion = {},
            alIrARegistro = {},
            alIrARecuperarContrasena = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-02 con errores")
@Composable
private fun IniciarSesionConErroresPreview() {
    DonChambitasTema {
        IniciarSesionContenido(
            estado = EstadoIniciarSesion(
                correo = "refugio@",
                errorCorreo = R.string.validacion_correo_formato,
                errorContrasena = R.string.validacion_contrasena_vacia
            ),
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alIniciarSesion = {},
            alIrARegistro = {},
            alIrARecuperarContrasena = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-02 cargando")
@Composable
private fun IniciarSesionCargandoPreview() {
    DonChambitasTema {
        IniciarSesionContenido(
            estado = EstadoIniciarSesion(
                correo = "refugio@ejemplo.mx",
                contrasena = "12345678",
                cargando = true
            ),
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alIniciarSesion = {},
            alIrARegistro = {},
            alIrARecuperarContrasena = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-02 credenciales rechazadas")
@Composable
private fun IniciarSesionCredencialesRechazadasPreview() {
    DonChambitasTema {
        IniciarSesionContenido(
            estado = EstadoIniciarSesion(
                correo = "refugio@ejemplo.mx",
                contrasena = "12345678",
                errorPantalla = TipoError.AUTENTICACION
            ),
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alIniciarSesion = {},
            alIrARegistro = {},
            alIrARecuperarContrasena = {}
        )
    }
}
