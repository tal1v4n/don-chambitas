package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.pie
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.secundario

private val FormaBurbujaMia = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
private val FormaBurbujaAjena = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)

/**
 * Fila de la bandeja de chats (P-15), HU-25.
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 4.1 (H-49).
 *
 * @param solicitudTitulo El titulo de la solicitud de la que viene el hilo; nulo en el hilo suelto.
 * @param sinLeer Si es mayor que cero, el nombre va en negrita y la fila lleva un punto.
 */
@Composable
fun TarjetaConversacion(
    nombre: String,
    ultimoMensaje: String?,
    hora: String?,
    sinLeer: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    solicitudTitulo: String? = null
) {
    val tieneSinLeer = sinLeer > 0
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, if (tieneSinLeer) Amarillo else Borde)
    ) {
        Row(
            modifier = Modifier.padding(Espaciado.dp12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (tieneSinLeer) Amarillo else AmarilloSuave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inicialesDe(nombre),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = FontWeight.Bold,
                    color = Tinta
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nombre,
                        style = DonChambitasTema.tipografia.cuerpoFuerte,
                        fontWeight = if (tieneSinLeer) FontWeight.Bold else FontWeight.SemiBold,
                        color = Tinta,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hora != null) {
                        Text(
                            text = hora,
                            style = DonChambitasTema.tipografia.pie,
                            fontWeight = if (tieneSinLeer) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (tieneSinLeer) Tinta else Gris
                        )
                    }
                }
                if (solicitudTitulo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp4)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WorkOutline,
                            contentDescription = null,
                            tint = Gris,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = solicitudTitulo,
                            style = DonChambitasTema.tipografia.pie,
                            color = Gris,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ultimoMensaje.orEmpty(),
                        style = DonChambitasTema.tipografia.secundario,
                        fontWeight = if (tieneSinLeer) FontWeight.Medium else FontWeight.Normal,
                        color = if (tieneSinLeer) Tinta else Gris,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (tieneSinLeer) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(start = Espaciado.dp8)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Tinta)
                        ) {
                            Text(
                                text = if (sinLeer > MAXIMO_CONTADOR) "$MAXIMO_CONTADOR+" else sinLeer.toString(),
                                style = DonChambitasTema.tipografia.pie,
                                fontWeight = FontWeight.Bold,
                                color = Amarillo
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Hasta este numero el contador de sin leer se pinta tal cual; despues, "9+". */
private const val MAXIMO_CONTADOR = 9

/** Primera letra de las dos primeras palabras del nombre, para el avatar sin foto. */
private fun inicialesDe(nombre: String): String =
    nombre.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase()

/**
 * Un mensaje de P-16: los propios a la derecha en Amarillo, los de la otra persona a la izquierda en
 * Blanco con contorno. La esquina de abajo del lado de quien habla queda casi recta, como cola de
 * la burbuja. Texto plano (DEC-04).
 * Especificada en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.1 (H-49).
 *
 * @param estadoEnvio Texto bajo el propio mensaje: "Enviando…", "Leído" o el aviso de reintento.
 * @param fallido Si no se pudo enviar: el mensaje se pinta con borde de error y se toca para reintentar.
 */
@Composable
fun BurbujaMensaje(
    contenido: String,
    hora: String,
    esMio: Boolean,
    modifier: Modifier = Modifier,
    estadoEnvio: String? = null,
    fallido: Boolean = false,
    alReintentar: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(start = if (esMio) 48.dp else 0.dp, end = if (esMio) 0.dp else 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(if (esMio) FormaBurbujaMia else FormaBurbujaAjena)
                    .background(if (esMio) Amarillo else Blanco)
                    .border(1.dp, if (fallido) Error else if (esMio) Amarillo else Borde, if (esMio) FormaBurbujaMia else FormaBurbujaAjena)
                    .let { if (fallido && alReintentar != null) it.clickable(onClick = alReintentar) else it }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text = contenido, style = DonChambitasTema.tipografia.cuerpo, color = Tinta)
            }
            Text(
                text = if (estadoEnvio != null) "$hora · $estadoEnvio" else hora,
                style = DonChambitasTema.tipografia.pie,
                color = if (fallido) Error else Gris,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Campo de mensaje de P-16: varias lineas (hasta 4) y un boton de enviar apagado con el texto en
 * blanco. Sin adjuntar, foto ni llamada: el chat es de texto plano (DEC-04).
 * Especificado en docs/producto/DISENO-POSTULACION-CONTACTO-RESENAS.md, seccion 5.1 (H-49).
 */
@Composable
fun CampoMensaje(
    valor: String,
    alCambiarValor: (String) -> Unit,
    alEnviar: () -> Unit,
    puedeEnviar: Boolean,
    marcadorPosicion: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Blanco)
            .drawBehind {
                drawLine(Borde, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
            }
            .navigationBarsPadding()
            .padding(horizontal = Espaciado.dp12, vertical = Espaciado.dp8),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.dp8)
    ) {
        CampoTexto(
            valor = valor,
            alCambiarValor = alCambiarValor,
            marcadorPosicion = marcadorPosicion,
            lineasMaximas = 4,
            modifier = Modifier.weight(1f)
        )
        // Circulo Tinta con la flecha Amarilla; apagado baja a Hueso.
        IconButton(
            onClick = alEnviar,
            enabled = puedeEnviar,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(if (puedeEnviar) Tinta else Hueso)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_enviar),
                tint = if (puedeEnviar) Amarillo else Gris,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
