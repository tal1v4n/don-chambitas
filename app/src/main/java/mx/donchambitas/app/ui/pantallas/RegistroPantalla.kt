package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.ui.componentes.IconoAvanzar
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.portada
import mx.donchambitas.app.ui.tema.secundario
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoContrasena
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.AvisoError
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.util.TipoError

/** Campos de texto de P-03, en orden visual: el foco salta al primero que falle. */
enum class CampoRegistro { NOMBRE, APELLIDOS, CORREO, CONTRASENA, TELEFONO }

/**
 * Pantalla de registro con seleccion de rol (P-03).
 * Especificada en docs/producto/DISENO-AUTENTICACION.md, seccion 3.
 *
 * El estado, la validacion y la llamada a RepositorioAuth viven en
 * [RegistroViewModel]; aqui solo se conecta con el contenido y se entrega el
 * destino una sola vez.
 *
 * @param alEntrar Se invoca con la pantalla de inicio del rol cuando la cuenta queda creada.
 * @param alRegresar Regresa a P-02 descartando lo capturado.
 * @param alIrAIniciarSesion Lleva a P-02 desde el pie de la pantalla.
 */
@Composable
fun RegistroPantalla(
    alEntrar: (Ruta) -> Unit,
    alRegresar: () -> Unit,
    alIrAIniciarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.destino) {
        val destino = estado.destino ?: return@LaunchedEffect
        alEntrar(destino)
        viewModel.alConsumirDestino()
    }

    RegistroContenido(
        estado = estado,
        alElegirRol = viewModel::alElegirRol,
        alCambiarNombre = viewModel::alCambiarNombre,
        alCambiarApellidos = viewModel::alCambiarApellidos,
        alCambiarCorreo = viewModel::alCambiarCorreo,
        alCambiarContrasena = viewModel::alCambiarContrasena,
        alCambiarTelefono = viewModel::alCambiarTelefono,
        alSalirDeCampo = viewModel::alPerderFoco,
        alRegistrar = viewModel::alRegistrar,
        alReintentar = viewModel::alReintentar,
        alRegresar = alRegresar,
        alIrAIniciarSesion = alIrAIniciarSesion,
        modifier = modifier
    )
}

private fun EstadoRegistro.errorDe(campo: CampoRegistro): Int? = when (campo) {
    CampoRegistro.NOMBRE -> errorNombre
    CampoRegistro.APELLIDOS -> errorApellidos
    CampoRegistro.CORREO -> errorCorreo
    CampoRegistro.CONTRASENA -> errorContrasena
    CampoRegistro.TELEFONO -> errorTelefono
}

/**
 * Contenido visual puro de P-03, sin estado propio, para previsualizarlo y
 * probarlo con cualquier combinacion de valores y errores.
 *
 * Al enviar, lleva al usuario al primer error en orden visual (5.4): si es el
 * rol, sube hasta el selector; si es un campo, le pone el foco.
 */
@Composable
fun RegistroContenido(
    estado: EstadoRegistro,
    alElegirRol: (RolUsuario) -> Unit,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarCorreo: (String) -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alRegistrar: () -> Unit,
    alRegresar: () -> Unit,
    alIrAIniciarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    alSalirDeCampo: (CampoRegistro) -> Unit = {},
    alReintentar: (() -> Unit)? = null
) {
    val administradorFoco = LocalFocusManager.current
    val siguienteCampo = KeyboardActions(
        onNext = { administradorFoco.moveFocus(FocusDirection.Down) }
    )
    val desplazamiento = rememberScrollState()
    val solicitantesFoco = remember { CampoRegistro.entries.associateWith { FocusRequester() } }
    var irAlPrimerError by remember { mutableStateOf(false) }
    val registrar = {
        alRegistrar()
        irAlPrimerError = true
    }

    LaunchedEffect(irAlPrimerError) {
        if (!irAlPrimerError) return@LaunchedEffect
        irAlPrimerError = false
        if (estado.errorRol != null) {
            administradorFoco.clearFocus()
            desplazamiento.animateScrollTo(0)
        } else {
            CampoRegistro.entries.firstOrNull { estado.errorDe(it) != null }
                ?.let { solicitantesFoco.getValue(it).requestFocus() }
        }
    }

    fun Modifier.campo(campo: CampoRegistro): Modifier = this
        .focusRequester(solicitantesFoco.getValue(campo))
        .alPerderFoco { alSalirDeCampo(campo) }

    Scaffold(
        topBar = {
            // El titulo vive en la portada; la barra solo lleva la flecha.
            BarraSuperior(titulo = "", alRegresar = alRegresar)
        },
        containerColor = Marfil,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(desplazamiento)
                .imePadding()
                .padding(
                    start = Espaciado.margenPantalla,
                    end = Espaciado.margenPantalla,
                    top = 0.dp,
                    bottom = Espaciado.dp24
                ),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                Text(
                    text = stringResource(R.string.registro_titulo),
                    style = DonChambitasTema.tipografia.portada,
                    color = Tinta
                )
                Text(
                    text = stringResource(R.string.registro_bajada),
                    style = DonChambitasTema.tipografia.secundario,
                    color = Gris
                )
            }

            EncabezadoPaso(numero = 1, texto = stringResource(R.string.registro_pregunta_rol))

            SelectorRol(
                rol = estado.rol,
                errorRol = estado.errorRol,
                habilitado = !estado.cargando,
                alElegirRol = alElegirRol
            )

            EncabezadoPaso(
                numero = 2,
                texto = stringResource(R.string.registro_seccion_datos),
                modifier = Modifier.padding(top = Espaciado.dp4)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)) {
                CampoTexto(
                    valor = estado.nombre,
                    alCambiarValor = alCambiarNombre,
                    etiqueta = stringResource(R.string.registro_nombre),
                    error = estado.errorNombre?.let { stringResource(it) },
                    habilitado = !estado.cargando,
                    modifier = Modifier
                        .weight(1f)
                        .campo(CampoRegistro.NOMBRE),
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo
                )

                CampoTexto(
                    valor = estado.apellidos,
                    alCambiarValor = alCambiarApellidos,
                    etiqueta = stringResource(R.string.registro_apellidos),
                    error = estado.errorApellidos?.let { stringResource(it) },
                    habilitado = !estado.cargando,
                    modifier = Modifier
                        .weight(1f)
                        .campo(CampoRegistro.APELLIDOS),
                    tecladoOpciones = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo
                )
            }

            CampoTexto(
                valor = estado.correo,
                alCambiarValor = alCambiarCorreo,
                etiqueta = stringResource(R.string.auth_correo),
                error = estado.errorCorreo?.let { stringResource(it) },
                habilitado = !estado.cargando,
                iconoInicio = { Icon(Icons.Outlined.MailOutline, contentDescription = null) },
                modifier = Modifier.campo(CampoRegistro.CORREO),
                tecladoOpciones = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                tecladoAcciones = siguienteCampo
            )

            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
                CampoContrasena(
                    valor = estado.contrasena,
                    alCambiarValor = alCambiarContrasena,
                    etiqueta = stringResource(R.string.auth_contrasena),
                    error = estado.errorContrasena?.let { stringResource(it) },
                    habilitado = !estado.cargando,
                    modifier = Modifier.campo(CampoRegistro.CONTRASENA),
                    tecladoOpciones = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    tecladoAcciones = siguienteCampo
                )
                Text(
                    text = stringResource(R.string.registro_ayuda_contrasena),
                    style = DonChambitasTema.tipografia.pie,
                    color = Gris,
                    modifier = Modifier.padding(start = Espaciado.dp4)
                )
            }

            CampoTexto(
                valor = estado.telefono,
                alCambiarValor = alCambiarTelefono,
                etiqueta = stringResource(R.string.registro_telefono),
                error = estado.errorTelefono?.let { stringResource(it) },
                habilitado = !estado.cargando,
                iconoInicio = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                modifier = Modifier.campo(CampoRegistro.TELEFONO),
                tecladoOpciones = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                tecladoAcciones = KeyboardActions(
                    onDone = {
                        administradorFoco.clearFocus()
                        if (!estado.cargando) registrar()
                    }
                )
            )

            ErrorDePantalla(
                tipoError = estado.errorPantalla,
                alReintentar = alReintentar
            )

            BotonPrincipal(
                texto = stringResource(R.string.registro_accion),
                onClick = registrar,
                cargando = estado.cargando,
                icono = IconoAvanzar
            )

            BotonTexto(
                texto = stringResource(R.string.registro_ir_iniciar_sesion),
                onClick = alIrAIniciarSesion,
                habilitado = !estado.cargando,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/** Numero del paso en un circulo Amarillo y su titulo al lado. */
@Composable
private fun EncabezadoPaso(numero: Int, texto: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Amarillo)
        ) {
            Text(
                text = numero.toString(),
                style = DonChambitasTema.tipografia.secundario,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
        }
        Text(
            text = texto,
            style = DonChambitasTema.tipografia.subtitulo,
            fontWeight = FontWeight.SemiBold,
            color = Tinta
        )
    }
}

/**
 * Selector de rol unico, en dos tarjetas altas lado a lado. No trae ninguno
 * preseleccionado a proposito: por DEC-22 el rol es permanente, asi que la
 * aplicacion no lo elige por el usuario.
 */
@Composable
private fun SelectorRol(
    rol: RolUsuario?,
    errorRol: Int?,
    habilitado: Boolean,
    alElegirRol: (RolUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            OpcionRol(
                texto = stringResource(R.string.registro_rol_cliente),
                detalle = stringResource(R.string.registro_rol_cliente_detalle),
                icono = Icons.Outlined.Search,
                seleccionado = rol == RolUsuario.CLIENTE,
                hayError = errorRol != null,
                habilitado = habilitado,
                alSeleccionar = { alElegirRol(RolUsuario.CLIENTE) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            OpcionRol(
                texto = stringResource(R.string.registro_rol_trabajador),
                detalle = stringResource(R.string.registro_rol_trabajador_detalle),
                icono = Icons.Outlined.Construction,
                seleccionado = rol == RolUsuario.TRABAJADOR,
                hayError = errorRol != null,
                habilitado = habilitado,
                alSeleccionar = { alElegirRol(RolUsuario.TRABAJADOR) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        if (errorRol != null) {
            Text(
                text = stringResource(errorRol),
                style = DonChambitasTema.tipografia.pie,
                color = Error
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp4)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Gris,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.registro_rol_definitivo),
                style = DonChambitasTema.tipografia.pie,
                color = Gris
            )
        }
    }
}

/**
 * Una tarjeta de rol. Elegida, se vuelve Tinta con el icono en un circulo
 * Amarillo y una palomita; sin elegir, es Blanca con contorno. Toda la tarjeta
 * es un solo nodo seleccionable con rol de RadioButton, asi el lector de
 * pantalla la anuncia como opcion seleccionada.
 */
@Composable
private fun OpcionRol(
    texto: String,
    detalle: String,
    icono: ImageVector,
    seleccionado: Boolean,
    hayError: Boolean,
    habilitado: Boolean,
    alSeleccionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo by animateColorAsState(if (seleccionado) Tinta else Blanco, label = "fondoRol")
    val contorno = when {
        seleccionado -> Tinta
        hayError -> Error
        else -> Borde
    }
    Box(
        modifier = modifier
            .clip(FormaTarjeta)
            .background(fondo)
            .border(1.dp, contorno, FormaTarjeta)
            .selectable(
                selected = seleccionado,
                enabled = habilitado,
                role = Role.RadioButton,
                onClick = alSeleccionar
            )
            .padding(Espaciado.dp12)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (seleccionado) Amarillo else AmarilloSuave)
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = Tinta)
            }
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold,
                color = if (seleccionado) Blanco else Tinta
            )
            Text(
                text = detalle,
                style = DonChambitasTema.tipografia.pie,
                color = if (seleccionado) Borde else Gris
            )
        }
        if (seleccionado) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Amarillo,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
            )
        }
    }
}

/**
 * Error que no pertenece a ningun campo. Se pinta en linea, encima del boton
 * y sin tapar el formulario: lo que el usuario escribio sigue ahi.
 */
@Composable
private fun ErrorDePantalla(
    tipoError: TipoError?,
    alReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (tipoError == null) return

    // Reintentar solo donde volver a intentar puede servir de algo. En
    // VALIDACION y CORREO_DUPLICADO lo que hay que cambiar es lo que esta escrito.
    val reintento = alReintentar.takeIf {
        tipoError in setOf(TipoError.RED, TipoError.SERVIDOR, TipoError.DESCONOCIDO)
    }

    AvisoError(
        tipoError = tipoError,
        alReintentar = reintento,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-03 vacío")
@Composable
private fun RegistroVacioPreview() {
    DonChambitasTema {
        RegistroContenido(
            estado = EstadoRegistro(),
            alElegirRol = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alCambiarTelefono = {},
            alRegistrar = {},
            alRegresar = {},
            alIrAIniciarSesion = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-03 con errores")
@Composable
private fun RegistroConErroresPreview() {
    DonChambitasTema {
        RegistroContenido(
            estado = EstadoRegistro(
                nombre = "Refugio",
                correo = "refugio@",
                telefono = "477123",
                errorRol = R.string.validacion_rol_sin_elegir,
                errorApellidos = R.string.validacion_apellidos_vacio,
                errorCorreo = R.string.validacion_correo_formato,
                errorContrasena = R.string.validacion_contrasena_corta,
                errorTelefono = R.string.validacion_telefono_digitos
            ),
            alElegirRol = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alCambiarTelefono = {},
            alRegistrar = {},
            alRegresar = {},
            alIrAIniciarSesion = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-03 cargando")
@Composable
private fun RegistroCargandoPreview() {
    DonChambitasTema {
        RegistroContenido(
            estado = EstadoRegistro(
                rol = RolUsuario.TRABAJADOR,
                nombre = "Refugio",
                apellidos = "Martínez Luna",
                correo = "refugio@ejemplo.mx",
                contrasena = "12345678",
                telefono = "4771234567",
                cargando = true
            ),
            alElegirRol = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alCambiarTelefono = {},
            alRegistrar = {},
            alRegresar = {},
            alIrAIniciarSesion = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-03 correo duplicado")
@Composable
private fun RegistroCorreoDuplicadoPreview() {
    DonChambitasTema {
        RegistroContenido(
            estado = EstadoRegistro(
                rol = RolUsuario.CLIENTE,
                nombre = "Refugio",
                apellidos = "Martínez Luna",
                correo = "refugio@ejemplo.mx",
                contrasena = "12345678",
                telefono = "4771234567",
                errorPantalla = TipoError.CORREO_DUPLICADO
            ),
            alElegirRol = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarCorreo = {},
            alCambiarContrasena = {},
            alCambiarTelefono = {},
            alRegistrar = {},
            alRegresar = {},
            alIrAIniciarSesion = {}
        )
    }
}
