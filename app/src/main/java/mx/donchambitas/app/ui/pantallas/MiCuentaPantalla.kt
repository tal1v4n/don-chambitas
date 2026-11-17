package mx.donchambitas.app.ui.pantallas

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import mx.donchambitas.app.ui.componentes.ArcosCasco
import mx.donchambitas.app.ui.componentes.FilaOpcion
import mx.donchambitas.app.ui.componentes.GrupoOpciones
import mx.donchambitas.app.ui.componentes.SeparadorOpciones
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Blanco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.donchambitas.app.ui.componentes.RecortadorFoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.componentes.CampoContrasena
import mx.donchambitas.app.ui.componentes.CampoTexto
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.componentes.obtenerTituloErrorRes
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo
import java.time.Instant
import kotlinx.coroutines.launch

private val TamanoAvatar = 80.dp
private val GrosorBordeAvatar = 3.dp
private val FormaCredencial = RoundedCornerShape(28.dp)

/**
 * Mi cuenta (P-18), HU-05.
 * Especificada en docs/tareas/S2-T11.md y docs/producto/WIREFRAMES.md, seccion 7.
 *
 * El estado y las llamadas a los repositorios viven en [MiCuentaViewModel];
 * aqui solo se conecta con el contenido y se pintan los avisos una vez. La
 * barra inferior la sigue pintando el andamio de GrafoNavegacion.
 *
 * @param alIrAMiPerfil Lleva a P-11. Solo se ofrece al trabajador.
 * @param alIrAMisPostulaciones Lleva a P-14. Solo se ofrece al trabajador.
 */
@Composable
fun MiCuentaPantalla(
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiCuentaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    // La imagen ya decodificada y derecha, mientras el recortador está abierto.
    var aRecortar by remember { mutableStateOf<Bitmap?>(null) }

    // Valida antes de cargar nada en memoria y decodifica fuera del hilo
    // principal: una foto de cámara puede pesar varios megas (S2-T13, paso 3).
    fun abrirEnRecortador(uri: Uri) {
        alcance.launch {
            val resolvedor = contexto.contentResolver
            val fallo = withContext(Dispatchers.IO) { resolvedor.validarImagen(uri) }
            if (fallo != null) {
                viewModel.alRechazarFoto(fallo.mensaje())
                return@launch
            }
            val bitmap = withContext(Dispatchers.IO) { runCatching { resolvedor.decodificarFoto(uri) }.getOrNull() }
            if (bitmap == null) viewModel.alRechazarFoto(R.string.foto_no_se_abrio) else aRecortar = bitmap
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) abrirEnRecortador(uri)
    }
    val archivoCamara = remember { archivoDeCamara(contexto) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        if (tomada) abrirEnRecortador(archivoCamara)
    }

    MiCuentaContenido(
        estado = estado,
        alReintentar = viewModel::alReintentar,
        alAbrirSeccion = viewModel::alAbrirSeccion,
        alCancelar = viewModel::alCancelar,
        alCambiarNombre = viewModel::alCambiarNombre,
        alCambiarApellidos = viewModel::alCambiarApellidos,
        alCambiarTelefono = viewModel::alCambiarTelefono,
        alGuardarDatos = viewModel::alGuardarDatos,
        alCambiarContrasena = viewModel::alCambiarContrasena,
        alGuardarContrasena = viewModel::alGuardarContrasena,
        alCerrarSesion = viewModel::alCerrarSesion,
        alConsumirMensaje = viewModel::alConsumirMensaje,
        alIrAMiPerfil = alIrAMiPerfil,
        alIrAMisPostulaciones = alIrAMisPostulaciones,
        alElegirDeGaleria = {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        alTomarFoto = {
            try {
                camara.launch(archivoCamara)
            } catch (e: ActivityNotFoundException) {
                viewModel.alRechazarFoto(R.string.foto_sin_camara)
            }
        },
        modifier = modifier
    )

    val rellenoSistema = WindowInsets.systemBars.asPaddingValues()
    aRecortar?.let { bitmap ->
        Dialog(
            onDismissRequest = { aRecortar = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            RecortadorFoto(
                rellenoSistema = rellenoSistema,
                imagen = remember(bitmap) { bitmap.asImageBitmap() },
                alConfirmar = { recorte ->
                    aRecortar = null
                    alcance.launch {
                        val bytes = withContext(Dispatchers.Default) { prepararFoto(bitmap, recorte) }
                        viewModel.alConfirmarFoto(bytes)
                    }
                },
                alCancelar = { aRecortar = null }
            )
        }
    }
}


/**
 * Contenido visual puro de P-18, sin estado propio salvo el dialogo de
 * confirmacion, para previsualizarlo y probarlo con cualquier estado.
 */
@Composable
fun MiCuentaContenido(
    estado: EstadoMiCuenta,
    alReintentar: () -> Unit,
    alAbrirSeccion: (SeccionMiCuenta) -> Unit,
    alCancelar: () -> Unit,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alGuardarDatos: () -> Unit,
    alCambiarContrasena: (String) -> Unit,
    alGuardarContrasena: () -> Unit,
    alCerrarSesion: () -> Unit,
    alConsumirMensaje: () -> Unit,
    alIrAMiPerfil: () -> Unit,
    alIrAMisPostulaciones: () -> Unit,
    modifier: Modifier = Modifier,
    alElegirDeGaleria: () -> Unit = {},
    alTomarFoto: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var confirmandoCierre by rememberSaveable { mutableStateOf(false) }
    var eligiendoFoto by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.mensaje) {
        val mensaje = estado.mensaje ?: return@LaunchedEffect
        val texto = when (mensaje) {
            is MensajeMiCuenta.Texto -> contexto.getString(mensaje.texto)
            is MensajeMiCuenta.Error -> contexto.getString(
                R.string.mi_cuenta_formato_error,
                contexto.getString(obtenerTituloErrorRes(mensaje.tipo)),
                contexto.getString(obtenerMensajeErrorRes(mensaje.tipo))
            )
        }
        // En otro scope: consumir el mensaje cambia la llave de este efecto y
        // cancelaria el Snackbar mientras se muestra.
        alcance.launch { avisos.showSnackbar(texto) }
        alConsumirMensaje()
    }

    // La barra inferior ya la reserva el andamio, y la superior trae su
    // propio inset: el contenido no necesita otro relleno de sistema.
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.mi_cuenta_titulo)) },
        snackbarHost = { SnackbarHost(avisos) },
        containerColor = Marfil,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            alReintentar = alReintentar,
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
        ) {
            val usuario = estado.usuario ?: return@ContenedorEstado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp24),
                verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
            ) {
                if (estado.seccion == SeccionMiCuenta.DATOS) {
                    SeccionDatos(
                        estado = estado,
                        alCambiarNombre = alCambiarNombre,
                        alCambiarApellidos = alCambiarApellidos,
                        alCambiarTelefono = alCambiarTelefono,
                        alGuardar = alGuardarDatos,
                        alCancelar = alCancelar
                    )
                } else {
                    TarjetaCuenta(
                        usuario = usuario,
                        subiendoFoto = estado.subiendoFoto,
                        alTocarAvatar = { eligiendoFoto = true }
                    )
                }

                if (estado.seccion == SeccionMiCuenta.CONTRASENA) {
                    SeccionContrasena(
                        estado = estado,
                        alCambiarContrasena = alCambiarContrasena,
                        alGuardar = alGuardarContrasena,
                        alCancelar = alCancelar
                    )
                }

                if (estado.seccion == SeccionMiCuenta.NINGUNA) {
                    GrupoOpciones(titulo = stringResource(R.string.mi_cuenta_configuracion)) {
                        FilaOpcion(
                            texto = stringResource(R.string.mi_cuenta_editar_datos),
                            detalle = stringResource(R.string.mi_cuenta_editar_datos_detalle),
                            icono = Icons.Outlined.Badge,
                            alPulsar = { alAbrirSeccion(SeccionMiCuenta.DATOS) },
                            habilitado = !estado.guardando
                        )
                        SeparadorOpciones()
                        FilaOpcion(
                            texto = stringResource(R.string.mi_cuenta_cambiar_contrasena),
                            detalle = stringResource(R.string.registro_ayuda_contrasena),
                            icono = Icons.Outlined.Lock,
                            alPulsar = { alAbrirSeccion(SeccionMiCuenta.CONTRASENA) },
                            habilitado = !estado.guardando
                        )
                    }
                }

                // Decision 3 de S2-T11: P-11 y P-14, como dice PANTALLAS.md.
                if (usuario.rol == RolUsuario.TRABAJADOR) {
                    GrupoOpciones(titulo = stringResource(R.string.mi_cuenta_accesos_trabajador)) {
                        FilaOpcion(
                            texto = stringResource(R.string.mi_cuenta_mi_perfil),
                            detalle = stringResource(R.string.mi_cuenta_mi_perfil_detalle),
                            icono = Icons.Outlined.Engineering,
                            alPulsar = alIrAMiPerfil
                        )
                        SeparadorOpciones()
                        FilaOpcion(
                            texto = stringResource(R.string.mi_cuenta_mis_postulaciones),
                            detalle = stringResource(R.string.mi_cuenta_mis_postulaciones_detalle),
                            icono = Icons.Outlined.WorkOutline,
                            alPulsar = alIrAMisPostulaciones
                        )
                    }
                }

                GrupoOpciones {
                    FilaOpcion(
                        texto = stringResource(R.string.mi_cuenta_cerrar_sesion),
                        detalle = stringResource(R.string.mi_cuenta_cerrar_sesion_detalle),
                        icono = Icons.AutoMirrored.Outlined.Logout,
                        alPulsar = { confirmandoCierre = true },
                        cargando = estado.cerrandoSesion,
                        color = Error,
                        fondoIcono = Error.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }

    if (eligiendoFoto) {
        OpcionesFoto(
            titulo = stringResource(R.string.mi_cuenta_cambiar_foto),
            alElegirDeGaleria = {
                eligiendoFoto = false
                alElegirDeGaleria()
            },
            alTomarFoto = {
                eligiendoFoto = false
                alTomarFoto()
            },
            alCerrar = { eligiendoFoto = false }
        )
    }

    if (confirmandoCierre) {
        AlertDialog(
            onDismissRequest = { confirmandoCierre = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = Error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.mi_cuenta_confirmar_cierre),
                    style = DonChambitasTema.tipografia.subtitulo,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.mi_cuenta_cerrar_sesion_detalle),
                    style = DonChambitasTema.tipografia.cuerpo,
                    color = Gris,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                BotonSecundario(
                    texto = stringResource(R.string.mi_cuenta_cerrar_sesion),
                    onClick = {
                        confirmandoCierre = false
                        alCerrarSesion()
                    },
                    color = Error
                )
            },
            dismissButton = {
                BotonTexto(
                    texto = stringResource(R.string.mi_cuenta_cancelar),
                    onClick = { confirmandoCierre = false },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = FormaTarjeta,
            containerColor = Blanco
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = DonChambitasTema.tipografia.subtitulo,
        fontWeight = FontWeight.SemiBold,
        color = Tinta
    )
}

/**
 * Cabecera de WIREFRAMES.md, P-18, como credencial: tarjeta Tinta con los
 * arcos de la marca, avatar con anillo Amarillo y los datos en claro. Sin
 * municipio: public.usuarios no lo tiene (H-15).
 */
@Composable
private fun TarjetaCuenta(usuario: Usuario, subiendoFoto: Boolean, alTocarAvatar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaCredencial)
            .background(Tinta)
    ) {
        ArcosCasco(
            color = Amarillo.copy(alpha = 0.16f),
            grosor = 14.dp,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier.padding(Espaciado.dp24),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp16)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Espaciado.dp16)
            ) {
                Avatar(usuario, subiendoFoto, alTocarAvatar)
                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)) {
                    Text(
                        text = "${usuario.nombre} ${usuario.apellidos}",
                        style = DonChambitasTema.tipografia.subtitulo,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                    PildoraRol(rol = usuario.rol)
                }
            }
            HorizontalDivider(color = Blanco.copy(alpha = 0.12f))
            DatoCredencial(icono = Icons.Outlined.MailOutline, texto = usuario.correo)
            usuario.telefono?.takeIf { it.isNotBlank() }?.let { telefono ->
                DatoCredencial(
                    icono = Icons.Outlined.Phone,
                    texto = stringResource(R.string.mi_cuenta_telefono, telefono)
                )
            }
        }
    }
}

@Composable
private fun PildoraRol(rol: RolUsuario) {
    val trabajador = rol == RolUsuario.TRABAJADOR
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp4),
        modifier = Modifier
            .clip(CircleShape)
            .background(if (trabajador) Amarillo else Blanco)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (trabajador) Icons.Outlined.Construction else Icons.Outlined.Person,
            contentDescription = null,
            tint = Tinta,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(if (trabajador) R.string.mi_cuenta_rol_trabajador else R.string.mi_cuenta_rol_cliente),
            style = DonChambitasTema.tipografia.pie,
            fontWeight = FontWeight.SemiBold,
            color = Tinta
        )
    }
}

@Composable
private fun DatoCredencial(icono: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Amarillo, modifier = Modifier.size(18.dp))
        Text(text = texto, style = DonChambitasTema.tipografia.secundario, color = Borde)
    }
}

/**
 * Foto si la hay; si no, las iniciales (HU-07), nunca un espacio vacio.
 * Tocarlo abre las opciones para cambiarla (S2-T13); mientras sube, no. La
 * camarita Amarilla en la esquina avisa que se puede tocar.
 */
@Composable
private fun Avatar(usuario: Usuario, subiendoFoto: Boolean, alTocar: () -> Unit) {
    val forma = Modifier
        .size(TamanoAvatar)
        .clip(FormaFotoPerfil)
        .border(GrosorBordeAvatar, Amarillo, FormaFotoPerfil)
        .clickable(
            enabled = !subiendoFoto,
            onClickLabel = stringResource(R.string.mi_cuenta_cambiar_foto),
            onClick = alTocar
        )
    Box(contentAlignment = Alignment.Center) {
        AvatarImagen(usuario, forma)
        if (subiendoFoto) {
            CircularProgressIndicator(color = Amarillo, modifier = Modifier.size(TamanoAvatar))
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Amarillo)
                    .border(2.dp, Tinta, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = Tinta,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AvatarImagen(usuario: Usuario, forma: Modifier) {
    if (usuario.fotoUrl != null) {
        AsyncImage(
            model = usuario.fotoUrl,
            contentDescription = stringResource(R.string.foto_perfil),
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = forma.background(AmarilloSuave)
        ) {
            Text(
                text = iniciales(usuario.nombre, usuario.apellidos),
                style = DonChambitasTema.tipografia.titulo,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
        }
    }
}

/** Primera letra del nombre y del primer apellido. */
internal fun iniciales(nombre: String, apellidos: String): String =
    listOfNotNull(nombre.trim().firstOrNull(), apellidos.trim().firstOrNull())
        .joinToString("")
        .uppercase()

/**
 * Edicion de nombre, apellidos y telefono. El correo y el rol no aparecen:
 * ninguno se edita (DEC-22). Al guardar con errores, el foco va al primero (5.4).
 */
@Composable
private fun SeccionDatos(
    estado: EstadoMiCuenta,
    alCambiarNombre: (String) -> Unit,
    alCambiarApellidos: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
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

    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Blanco)
            .border(1.dp, Borde, FormaTarjeta)
            .padding(Espaciado.dp16)
    ) {
        Titulo(stringResource(R.string.mi_cuenta_editar_datos))
        CampoTexto(
            valor = estado.nombre,
            alCambiarValor = alCambiarNombre,
            etiqueta = stringResource(R.string.registro_nombre),
            error = estado.errorNombre?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.NOMBRE)),
            tecladoOpciones = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            tecladoAcciones = siguienteCampo
        )
        CampoTexto(
            valor = estado.apellidos,
            alCambiarValor = alCambiarApellidos,
            etiqueta = stringResource(R.string.registro_apellidos),
            error = estado.errorApellidos?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.APELLIDOS)),
            tecladoOpciones = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            tecladoAcciones = siguienteCampo
        )
        CampoTexto(
            valor = estado.telefono,
            alCambiarValor = alCambiarTelefono,
            etiqueta = stringResource(R.string.registro_telefono),
            error = estado.errorTelefono?.let { stringResource(it) },
            habilitado = !estado.guardando,
            modifier = Modifier.focusRequester(solicitantesFoco.getValue(CampoMiCuenta.TELEFONO)),
            tecladoOpciones = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            tecladoAcciones = KeyboardActions(
                onDone = {
                    administradorFoco.clearFocus()
                    if (!estado.guardando) guardar()
                }
            )
        )
        BotonPrincipal(
            texto = stringResource(R.string.mi_cuenta_guardar),
            onClick = guardar,
            cargando = estado.guardando
        )
        BotonTexto(
            texto = stringResource(R.string.mi_cuenta_cancelar),
            onClick = alCancelar,
            habilitado = !estado.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun EstadoMiCuenta.errorDe(campo: CampoMiCuenta): Int? = when (campo) {
    CampoMiCuenta.NOMBRE -> errorNombre
    CampoMiCuenta.APELLIDOS -> errorApellidos
    CampoMiCuenta.TELEFONO -> errorTelefono
}

/** Solo la contrasena nueva (decision 2 de S2-T11), con el minimo del registro. */
@Composable
private fun SeccionContrasena(
    estado: EstadoMiCuenta,
    alCambiarContrasena: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    val administradorFoco = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Blanco)
            .border(1.dp, Borde, FormaTarjeta)
            .padding(Espaciado.dp16)
    ) {
        Titulo(stringResource(R.string.mi_cuenta_cambiar_contrasena))
        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
            CampoContrasena(
                valor = estado.contrasenaNueva,
                alCambiarValor = alCambiarContrasena,
                etiqueta = stringResource(R.string.mi_cuenta_contrasena_nueva),
                error = estado.errorContrasena?.let { stringResource(it) },
                habilitado = !estado.guardando,
                tecladoOpciones = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                tecladoAcciones = KeyboardActions(
                    onDone = {
                        administradorFoco.clearFocus()
                        if (!estado.guardando) alGuardar()
                    }
                )
            )
            Text(
                text = stringResource(R.string.registro_ayuda_contrasena),
                style = DonChambitasTema.tipografia.pie,
                color = Gris
            )
        }
        BotonPrincipal(
            texto = stringResource(R.string.mi_cuenta_guardar),
            onClick = alGuardar,
            cargando = estado.guardando
        )
        BotonTexto(
            texto = stringResource(R.string.mi_cuenta_cancelar),
            onClick = alCancelar,
            habilitado = !estado.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private val UsuarioPrevia = Usuario(
    id = "usr-previa",
    correo = "juan.perez@correo.com",
    nombre = "Juan",
    apellidos = "Pérez Hernández",
    telefono = "3312345678",
    rol = RolUsuario.TRABAJADOR,
    creadoEn = Instant.EPOCH,
    actualizadoEn = Instant.EPOCH
)

@Composable
private fun MiCuentaPrevia(estado: EstadoMiCuenta) {
    DonChambitasTema {
        MiCuentaContenido(
            estado = estado,
            alReintentar = {},
            alAbrirSeccion = {},
            alCancelar = {},
            alCambiarNombre = {},
            alCambiarApellidos = {},
            alCambiarTelefono = {},
            alGuardarDatos = {},
            alCambiarContrasena = {},
            alGuardarContrasena = {},
            alCerrarSesion = {},
            alConsumirMensaje = {},
            alIrAMiPerfil = {},
            alIrAMisPostulaciones = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 trabajador")
@Composable
private fun MiCuentaTrabajadorPreview() {
    MiCuentaPrevia(EstadoMiCuenta(usuario = UsuarioPrevia, cargando = false))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "P-18 editando datos")
@Composable
private fun MiCuentaEditandoPreview() {
    MiCuentaPrevia(
        EstadoMiCuenta(
            usuario = UsuarioPrevia.copy(rol = RolUsuario.CLIENTE),
            cargando = false,
            seccion = SeccionMiCuenta.DATOS,
            nombre = "Juan",
            apellidos = "",
            telefono = "33123",
            errorApellidos = R.string.validacion_apellidos_vacio,
            errorTelefono = R.string.validacion_telefono_digitos
        )
    )
}
