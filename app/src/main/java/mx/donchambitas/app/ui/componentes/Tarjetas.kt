package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Exito
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.subtitulo
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaFotoPerfil
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.Naranja
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario

private val TamanoFotoTrabajador = 56.dp
private val FormaFotoServicio = RoundedCornerShape(16.dp)
private const val PROPORCION_FOTO_SERVICIO = 16f / 10f
private const val OPACIDAD_SERVICIO_PAUSADO = 0.6f
private val TamanoIconoUbicacion = 14.dp

/**
 * Tarjeta para mostrar el resumen del perfil de un trabajador segun DISENO.md.
 * Incluye foto, nombre, titulo/oficio, estrellas, municipio, el precio inicial
 * y, si no toma trabajos, la etiqueta "No disponible" (H-43, DEC-36).
 *
 * @param municipio Nulo o vacio cuando el trabajador no puso su ubicacion: el renglon no se pinta (H-48).
 * @param precioDesdeTexto Nulo cuando no tiene servicios con precio: el renglon no se pinta.
 * @param disponible Falso lo marca con la etiqueta; no se oculta ni se atenua la tarjeta.
 */
@Composable
fun TarjetaTrabajador(
    nombre: String,
    oficio: String,
    calificacion: Float,
    municipio: String?,
    fotoUrl: String? = null,
    totalResenas: Int? = null,
    precioDesdeTexto: String? = null,
    disponible: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Blanco,
            contentColor = Tinta
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Espaciado.rellenoTarjeta)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    if (!fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = stringResource(R.string.foto_perfil),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(TamanoFotoTrabajador)
                                .clip(FormaFotoPerfil)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(TamanoFotoTrabajador)
                                .clip(FormaFotoPerfil)
                                .background(AmarilloSuave)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(R.string.foto_perfil),
                                tint = Tinta,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    // Punto de disponibilidad sobre la foto: verde si toma trabajos.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Blanco)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(if (disponible) Exito else Borde)
                    )
                }

                Spacer(modifier = Modifier.width(Espaciado.dp12))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = nombre,
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        fontWeight = FontWeight.SemiBold,
                        color = Tinta,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = oficio,
                        style = DonChambitasTema.tipografia.secundario,
                        color = Gris,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CalificacionCompacta(calificacion = calificacion, totalResenas = totalResenas)
            }

            val hayPie = !municipio.isNullOrBlank() || precioDesdeTexto != null || !disponible
            if (hayPie) {
                HorizontalDivider(color = Borde, modifier = Modifier.padding(vertical = Espaciado.dp12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
                ) {
                    if (!municipio.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Gris,
                            modifier = Modifier.size(TamanoIconoUbicacion)
                        )
                        Text(
                            text = municipio,
                            style = DonChambitasTema.tipografia.pie,
                            color = Gris,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (!disponible) {
                        Text(
                            text = stringResource(R.string.perfil_publico_no_disponible),
                            style = DonChambitasTema.tipografia.pie,
                            fontWeight = FontWeight.Medium,
                            color = Gris,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Hueso)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (precioDesdeTexto != null) {
                        Text(
                            text = precioDesdeTexto,
                            style = DonChambitasTema.tipografia.secundario,
                            fontWeight = FontWeight.Bold,
                            color = Tinta
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar un servicio publicado segun DISENO.md, en formato de
 * portafolio: la foto ocupa la parte de arriba con la categoria encima, y
 * abajo van el titulo y el precio. Sin foto, un bloque AmarilloSuave con la
 * herramienta guarda el mismo alto para que la lista no brinque.
 *
 * @param precioTexto Vacio cuando el servicio no tiene precio: el renglon no se pinta (HU-09).
 * @param pausado El servicio no sale en la busqueda: la tarjeta se atenua y lo dice (H-21).
 */
@Composable
fun TarjetaServicio(
    titulo: String,
    categoria: String,
    precioTexto: String,
    fotoUrl: String? = null,
    pausado: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Blanco,
            contentColor = Tinta
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (pausado) OPACIDAD_SERVICIO_PAUSADO else 1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PROPORCION_FOTO_SERVICIO)
                .padding(6.dp)
                .clip(FormaFotoServicio)
                .background(AmarilloSuave)
        ) {
            if (!fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = stringResource(R.string.foto_servicio),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = stringResource(R.string.foto_servicio),
                    tint = Tinta.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                )
            }
            Text(
                text = if (pausado) {
                    stringResource(R.string.tarjeta_servicio_categoria_pausado, categoria)
                } else {
                    categoria
                },
                style = DonChambitasTema.tipografia.pie,
                fontWeight = FontWeight.SemiBold,
                color = Tinta,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Espaciado.dp8)
                    .clip(CircleShape)
                    .background(Blanco.copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Espaciado.dp16, end = Espaciado.dp16, top = Espaciado.dp8, bottom = Espaciado.dp16),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)
        ) {
            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold,
                color = Tinta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (precioTexto.isNotBlank()) {
                Text(
                    text = precioTexto,
                    style = DonChambitasTema.tipografia.secundario,
                    fontWeight = FontWeight.SemiBold,
                    color = Naranja
                )
            }
        }
    }
}

/**
 * Tarjeta para mostrar una solicitud de trabajo segun DISENO.md.
 * Incluye titulo, categoria, presupuesto, estado y fecha.
 *
 * @param postulacionesSinRevisar Cuantas postulaciones siguen sin decidir; con 0 no se pinta
 * el renglon (DEC-37, H-42).
 */
@Composable
fun TarjetaSolicitud(
    titulo: String,
    categoria: String,
    presupuesto: String,
    estado: EstadoSolicitud,
    fecha: String,
    postulacionesSinRevisar: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = Blanco,
            contentColor = Tinta
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.rellenoTarjeta),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoria.uppercase(),
                    style = DonChambitasTema.tipografia.pie,
                    fontWeight = FontWeight.SemiBold,
                    color = Gris
                )
                EtiquetaEstado(estado = estado)
            }

            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.subtitulo,
                fontWeight = FontWeight.SemiBold,
                color = Tinta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Espaciado.dp4),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presupuesto,
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = FontWeight.Bold,
                    color = Tinta,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmarilloSuave)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = Gris,
                    modifier = Modifier.size(TamanoIconoUbicacion)
                )
                Text(
                    text = fecha,
                    style = DonChambitasTema.tipografia.pie,
                    color = Gris
                )
            }

            if (postulacionesSinRevisar > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .padding(top = Espaciado.dp4)
                        .clip(CircleShape)
                        .background(Naranja.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Naranja)
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.tarjeta_solicitud_sin_revisar,
                            postulacionesSinRevisar,
                            postulacionesSinRevisar
                        ),
                        style = DonChambitasTema.tipografia.pie,
                        fontWeight = FontWeight.SemiBold,
                        color = Naranja
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaTarjetaTrabajador() {
    DonChambitasTema {
        TarjetaTrabajador(
            nombre = "Mario González",
            oficio = "Plomero y electricista",
            calificacion = 4.8f,
            totalResenas = 34,
            municipio = "Monterrey, N.L.",
            precioDesdeTexto = "Desde $350"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaTarjetaTrabajadorSinUbicacionNiDisponibilidad() {
    DonChambitasTema {
        TarjetaTrabajador(
            nombre = "Mario González",
            oficio = "Plomero y electricista",
            calificacion = 0f,
            municipio = null,
            disponible = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaTarjetaServicio() {
    DonChambitasTema {
        TarjetaServicio(
            titulo = "Instalación y reparación de tuberías de cobre",
            categoria = "Plomería",
            precioTexto = "$350 - $800 MXN"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaTarjetaServicioPausado() {
    DonChambitasTema {
        TarjetaServicio(
            titulo = "Destape de drenaje",
            categoria = "Plomería",
            precioTexto = "",
            pausado = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaTarjetaSolicitud() {
    DonChambitasTema {
        TarjetaSolicitud(
            titulo = "Reparación de fuga en baño principal",
            categoria = "Plomería",
            presupuesto = "$500 MXN",
            estado = EstadoSolicitud.ABIERTA,
            fecha = "Hoy, 14:30"
        )
    }
}
