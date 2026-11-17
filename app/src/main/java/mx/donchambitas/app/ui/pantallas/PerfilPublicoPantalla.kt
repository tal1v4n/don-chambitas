package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import mx.donchambitas.app.ui.componentes.ArcosCasco
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Naranja
import mx.donchambitas.app.ui.tema.portada
import mx.donchambitas.app.ui.tema.secundario
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.ServicioPublico
import mx.donchambitas.app.ui.componentes.BarraSuperior
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.ContenedorEstado
import mx.donchambitas.app.ui.componentes.Estrellas
import mx.donchambitas.app.ui.componentes.TarjetaServicio
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.Marfil
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.FormaChip
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.subtitulo
import mx.donchambitas.app.ui.tema.titulo

/**
 * Perfil publico del trabajador (P-07), HU-08.
 * Especificada en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 11 y en
 * docs/producto/DISENO-SOLICITUDES-BUSQUEDA.md, seccion 6 (H-39).
 *
 * Conecta [PerfilPublicoContenido] con [PerfilPublicoViewModel]. Tiene dos modos: la vista
 * previa del trabajador (sus vacios le hablan a el) y la del cliente (oculta lo vacio que
 * no puede arreglar y trae el boton de contactar).
 *
 * @param alRegresar Regresa a la pantalla de donde vino.
 * @param alContactar Abre la conversacion con el trabajador; hoy no esta enlazado (S5-T06).
 */
@Composable
fun PerfilPublicoPantalla(
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alContactar: () -> Unit = {},
    viewModel: PerfilPublicoViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    PerfilPublicoContenido(
        estado = estado,
        alRegresar = alRegresar,
        alReintentarCarga = viewModel::alReintentarCarga,
        alContactar = alContactar,
        modifier = modifier
    )
}

/** Contenido visual puro de P-07, sin estado propio, para previsualizarlo y probarlo. */
@Composable
fun PerfilPublicoContenido(
    estado: EstadoPerfilPublico,
    alRegresar: () -> Unit,
    modifier: Modifier = Modifier,
    alReintentarCarga: () -> Unit = {},
    alContactar: () -> Unit = {}
) {
    // "Contactar" solo sale cuando ya hay perfil y quien lo mira es el cliente (6 del diseno).
    val mostrarContactar = estado.perfil != null && !estado.esVistaPrevia
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.perfil_publico_titulo),
                alRegresar = alRegresar,
                colorFondo = Amarillo
            )
        },
        bottomBar = {
            if (mostrarContactar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blanco)
                        .drawBehind {
                            drawLine(Borde, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                        }
                        .navigationBarsPadding()
                        .padding(horizontal = Espaciado.margenPantalla, vertical = Espaciado.dp12)
                ) {
                    BotonPrincipal(
                        texto = stringResource(R.string.perfil_publico_contactar),
                        onClick = alContactar,
                        icono = Icons.AutoMirrored.Outlined.Chat
                    )
                }
            }
        },
        containerColor = Marfil,
        modifier = modifier.fillMaxSize()
    ) { relleno ->
        ContenedorEstado(
            cargando = estado.cargando,
            error = estado.errorCarga,
            alReintentar = alReintentarCarga,
            modifier = Modifier.padding(relleno)
        ) {
            estado.perfil?.let { PerfilPublicoLista(it, esVistaPrevia = estado.esVistaPrevia) }
        }
    }
}

@Composable
private fun PerfilPublicoLista(perfil: PerfilPublicoTrabajador, esVistaPrevia: Boolean) {
    val margen = Modifier.padding(horizontal = Espaciado.margenPantalla)
    // Column y no LazyColumn: el perfil es corto y asi todo queda compuesto, listo para
    // desplazarse hasta cualquier parte (lectores de pantalla y pruebas incluidos).
    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp16),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Espaciado.dp24)
    ) {
        Portada(perfil, esVistaPrevia)
        Datos(perfil, modifier = margen.subir(SubidaDatos))

        // Al cliente le sobra una seccion vacia: no puede arreglarla, asi que se oculta.
        if (esVistaPrevia || !perfil.descripcion.isNullOrBlank()) {
            Seccion(R.string.perfil_publico_sobre_mi, Icons.Outlined.Person, margen) {
                if (perfil.descripcion.isNullOrBlank()) {
                    Vacio(R.string.perfil_publico_sin_descripcion)
                } else {
                    Text(text = perfil.descripcion, style = DonChambitasTema.tipografia.cuerpo, color = Tinta)
                }
            }
        }

        if (esVistaPrevia || perfil.habilidades.isNotEmpty()) {
            Seccion(R.string.perfil_publico_habilidades, Icons.Outlined.Bolt, margen) {
                if (perfil.habilidades.isEmpty()) {
                    Vacio(R.string.perfil_publico_sin_habilidades)
                } else {
                    Habilidades(perfil.habilidades)
                }
            }
        }

        TituloSeccion(R.string.perfil_publico_servicios, perfil.servicios.size, margen)
        if (perfil.servicios.isEmpty()) {
            Vacio(
                if (esVistaPrevia) R.string.perfil_publico_sin_servicios
                else R.string.perfil_publico_sin_servicios_cliente,
                margen
            )
        } else {
            // Carrusel: con dos o mas, los servicios se hojean de lado como un portafolio.
            if (perfil.servicios.size == 1) {
                TarjetaDeServicio(perfil.servicios.first(), margen)
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Espaciado.margenPantalla),
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
                ) {
                    items(perfil.servicios.size) { posicion ->
                        TarjetaDeServicio(perfil.servicios[posicion], Modifier.width(AnchoServicio))
                    }
                }
            }
        }

        TituloSeccion(R.string.perfil_publico_resenas, perfil.resenas.size, margen)
        if (perfil.resenas.isEmpty()) {
            Vacio(
                if (esVistaPrevia) R.string.perfil_publico_sin_resenas
                else R.string.perfil_publico_sin_resenas_cliente,
                margen
            )
        } else {
            perfil.resenas.forEach { resena -> TarjetaDeResena(resena, margen) }
        }
    }
}

private val AnchoServicio = 260.dp

/** Cuanto se monta la tarjeta de datos sobre la portada. */
private val SubidaDatos = 40.dp

/**
 * Sube el elemento [distancia] y le resta ese alto a lo que ocupa, para que se
 * monte sobre el anterior sin dejar un hueco debajo (offset solo lo mueve).
 */
private fun Modifier.subir(distancia: Dp): Modifier = layout { medible, restricciones ->
    val colocable = medible.measure(restricciones)
    val dy = distancia.roundToPx()
    layout(colocable.width, colocable.height - dy) { colocable.place(0, -dy) }
}

/**
 * Portada Amarilla con los arcos de la marca: avatar grande con anillo Blanco,
 * nombre, oficio y la pildora de disponibilidad. En la vista previa lleva
 * arriba el aviso de que asi lo ven los clientes (6 del diseno).
 */
@Composable
private fun Portada(perfil: PerfilPublicoTrabajador, esVistaPrevia: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Amarillo)
    ) {
        ArcosCasco(
            color = Blanco.copy(alpha = 0.3f),
            grosor = 16.dp,
            modifier = Modifier.matchParentSize()
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp8),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Espaciado.dp24, end = Espaciado.dp24, top = Espaciado.dp8, bottom = 56.dp)
        ) {
            if (esVistaPrevia) AvisoVistaPrevia()
            Avatar(perfil)
            Text(
                text = "${perfil.nombre} ${perfil.apellidos}".trim(),
                style = DonChambitasTema.tipografia.titulo,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                textAlign = TextAlign.Center
            )
            Text(
                text = perfil.titulo,
                style = DonChambitasTema.tipografia.cuerpo,
                color = Tinta.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            PildoraDisponible(perfil.disponible)
        }
    }
}

/** Le recuerda al trabajador que lo que ve es lo que ven los clientes (6 del diseno). */
@Composable
private fun AvisoVistaPrevia() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
        modifier = Modifier
            .clip(CircleShape)
            .background(Tinta)
            .padding(horizontal = Espaciado.dp12, vertical = 6.dp)
    ) {
        Icon(Icons.Outlined.Visibility, contentDescription = null, tint = Amarillo, modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.perfil_publico_aviso_vista_previa),
            style = DonChambitasTema.tipografia.pie,
            fontWeight = FontWeight.SemiBold,
            color = Blanco
        )
    }
}

@Composable
private fun PildoraDisponible(disponible: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(Blanco)
            .padding(horizontal = Espaciado.dp12, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (disponible) Exito else Gris)
        )
        Text(
            text = stringResource(
                if (disponible) R.string.perfil_publico_disponible else R.string.perfil_publico_no_disponible
            ),
            style = DonChambitasTema.tipografia.pie,
            fontWeight = FontWeight.SemiBold,
            color = Tinta
        )
    }
}

/** La foto, o la inicial del nombre sobre AmarilloSuave si todavia no hay; con anillo Blanco. */
@Composable
private fun Avatar(perfil: PerfilPublicoTrabajador) {
    val tamano = 104.dp
    val forma = Modifier
        .size(tamano)
        .border(4.dp, Blanco, FormaFotoPerfil)
        .padding(4.dp)
        .clip(FormaFotoPerfil)
    if (perfil.fotoUrl != null) {
        AsyncImage(
            model = perfil.fotoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        Box(contentAlignment = Alignment.Center, modifier = forma.background(AmarilloSuave)) {
            Text(
                text = perfil.nombre.take(1).uppercase(),
                style = DonChambitasTema.tipografia.portada,
                color = Tinta
            )
        }
    }
}

/**
 * Tres datos de un vistazo en una tarjeta Blanca que se monta sobre la
 * portada: calificacion, experiencia y zona. Lo que no hay, no se pinta.
 */
@Composable
private fun Datos(perfil: PerfilPublicoTrabajador, modifier: Modifier = Modifier) {
    val ubicacion = when {
        perfil.municipio != null && perfil.estado != null ->
            stringResource(R.string.perfil_publico_ubicacion, perfil.municipio, perfil.estado)
        else -> perfil.municipio ?: perfil.estado
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(8.dp, FormaTarjeta, ambientColor = Tinta.copy(alpha = 0.3f), spotColor = Tinta.copy(alpha = 0.3f))
            .clip(FormaTarjeta)
            .background(Blanco)
            .padding(vertical = Espaciado.dp16)
    ) {
        if (perfil.calificacion.totalResenas == 0) {
            Dato(
                icono = Icons.Outlined.StarBorder,
                valor = null,
                etiqueta = stringResource(R.string.perfil_publico_sin_calificacion),
                modifier = Modifier.weight(1f)
            )
        } else {
            Dato(
                icono = Icons.Filled.Star,
                valor = stringResource(
                    R.string.formato_calificacion,
                    perfil.calificacion.promedio.toFloat(),
                    perfil.calificacion.totalResenas
                ),
                etiqueta = stringResource(R.string.perfil_publico_dato_calificacion),
                modifier = Modifier.weight(1f)
            )
        }
        if (perfil.experienciaAnios > 0) {
            VerticalDivider(color = Borde)
            Dato(
                icono = Icons.Outlined.WorkspacePremium,
                valor = if (perfil.experienciaAnios == 1) {
                    stringResource(R.string.perfil_publico_anio_corto)
                } else {
                    stringResource(R.string.perfil_publico_anios_corto, perfil.experienciaAnios)
                },
                etiqueta = stringResource(R.string.perfil_publico_dato_experiencia),
                descripcion = if (perfil.experienciaAnios == 1) {
                    stringResource(R.string.perfil_publico_experiencia_uno)
                } else {
                    stringResource(R.string.perfil_publico_experiencia_varios, perfil.experienciaAnios)
                },
                modifier = Modifier.weight(1f)
            )
        }
        if (!ubicacion.isNullOrBlank()) {
            VerticalDivider(color = Borde)
            Dato(
                icono = Icons.Outlined.LocationOn,
                valor = perfil.municipio ?: ubicacion,
                etiqueta = stringResource(R.string.perfil_publico_dato_zona),
                descripcion = ubicacion,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Un dato de la tarjeta de [Datos]. Con [descripcion], el lector de pantalla
 * lee la frase completa en vez del valor corto.
 */
@Composable
private fun Dato(
    icono: ImageVector,
    valor: String?,
    etiqueta: String,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .padding(horizontal = Espaciado.dp8)
            .then(
                if (descripcion != null) Modifier.clearAndSetSemantics { contentDescription = descripcion; text = AnnotatedString(descripcion) }
                else Modifier
            )
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Naranja, modifier = Modifier.size(20.dp))
        if (valor != null) {
            Text(
                text = valor,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = etiqueta,
            style = DonChambitasTema.tipografia.pie,
            color = Gris,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TituloSeccion(@StringRes titulo: Int, cuantos: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
        modifier = modifier.padding(top = Espaciado.dp8)
    ) {
        Text(
            text = stringResource(titulo),
            style = DonChambitasTema.tipografia.subtitulo,
            fontWeight = FontWeight.Bold,
            color = Tinta
        )
        if (cuantos > 0) {
            Text(
                text = cuantos.toString(),
                style = DonChambitasTema.tipografia.pie,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Amarillo)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/** Tarjeta Blanca con titulo e icono, para "Sobre mi" y "Habilidades". */
@Composable
private fun Seccion(
    @StringRes titulo: Int,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp12),
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Blanco)
            .border(BorderStroke(1.dp, Borde), FormaTarjeta)
            .padding(Espaciado.rellenoTarjeta)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = Tinta, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(titulo),
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
        }
        contenido()
    }
}

/**
 * Lo que sale en una seccion sin datos: dice que falta y donde se arregla, no una
 * lista en blanco (HU-08).
 */
@Composable
private fun Vacio(@StringRes texto: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(texto),
        style = DonChambitasTema.tipografia.secundario,
        color = Gris,
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Hueso)
            .padding(Espaciado.rellenoTarjeta)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Habilidades(habilidades: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        habilidades.forEach { habilidad ->
            Text(
                text = habilidad,
                style = DonChambitasTema.tipografia.secundario,
                fontWeight = FontWeight.Medium,
                color = Tinta,
                modifier = Modifier
                    .clip(FormaChip)
                    .background(AmarilloSuave)
                    .padding(horizontal = Espaciado.dp12, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TarjetaDeServicio(servicio: ServicioPublico, modifier: Modifier = Modifier) {
    TarjetaServicio(
        titulo = servicio.titulo,
        categoria = servicio.categoria,
        precioTexto = textoPrecio(servicio),
        fotoUrl = servicio.fotos.firstOrNull(),
        modifier = modifier
    )
}

/** Resena con la inicial de quien la escribio, sus estrellas y el comentario entre comillas. */
@Composable
private fun TarjetaDeResena(resena: ResenaPublica, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espaciado.dp8),
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(Blanco)
            .border(BorderStroke(1.dp, Borde), FormaTarjeta)
            .padding(Espaciado.rellenoTarjeta)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AmarilloSuave)
            ) {
                Text(
                    text = resena.clienteNombre.take(1).uppercase(),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = FontWeight.Bold,
                    color = Tinta
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resena.clienteNombre,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = FontWeight.SemiBold,
                    color = Tinta
                )
                Estrellas(calificacion = resena.calificacion.toFloat(), tamano = 14.dp)
            }
        }
        if (!resena.comentario.isNullOrBlank()) {
            Text(text = resena.comentario, style = DonChambitasTema.tipografia.cuerpo, color = Tinta)
        }
    }
}

/** El precio como lo pide la tabla 3.4 de DISENO-PERFIL-SERVICIOS.md; vacio si no hay. */
@Composable
private fun textoPrecio(servicio: ServicioPublico): String {
    val precio = when (val tipo = precioDeServicio(servicio.precioDesde, servicio.precioHasta)) {
        is PrecioServicio.Rango -> stringResource(R.string.precio_rango, tipo.desde, tipo.hasta)
        is PrecioServicio.Desde -> stringResource(R.string.precio_desde, tipo.desde)
        is PrecioServicio.Hasta -> stringResource(R.string.precio_hasta, tipo.hasta)
        null -> return ""
    }
    val unidad = servicio.unidadPrecio?.trim().orEmpty()
    return if (unidad.isEmpty()) precio else stringResource(R.string.precio_con_unidad, precio, unidad)
}
