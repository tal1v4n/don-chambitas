package mx.donchambitas.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import mx.donchambitas.app.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import mx.donchambitas.app.ui.componentes.BotonPrincipal
import mx.donchambitas.app.ui.componentes.BotonTexto
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.FormaCampo
import mx.donchambitas.app.ui.tema.FormaTarjeta
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.subtitulo
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.AmarilloSuave
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.FormaBoton
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.cuerpoFuerte
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.pie

/**
 * Boton "Redactar con IA" de P-11 y P-13 con la linea que explica su estado
 * (DISENO-PERFIL-SERVICIOS.md, 1.9). Cuando esta deshabilitado, el lector de
 * pantalla anuncia el motivo.
 *
 * @param textoFaltanDatos Lo que le falta a cada pantalla para poder pedir: no es lo mismo en las dos.
 */
@Composable
internal fun BotonIa(
    estado: EstadoBotonIa,
    textoFaltanDatos: String,
    habilitado: Boolean,
    alPedir: () -> Unit
) {
    val linea = when (estado) {
        is EstadoBotonIa.FaltanDatos -> textoFaltanDatos
        is EstadoBotonIa.Disponible -> estado.restantesHoy?.let { stringResource(R.string.ia_restantes, it) }
        EstadoBotonIa.Pidiendo -> null
        EstadoBotonIa.LimiteAlcanzado -> stringResource(R.string.ia_limite)
    }
    val disponible = estado is EstadoBotonIa.Disponible

    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.dp4)) {
        // Fondo AmarilloSuave con contorno Amarillo y destellos: se distingue de
        // los botones de guardar y avisa que aqui ayuda la IA.
        OutlinedButton(
            onClick = alPedir,
            enabled = habilitado && disponible,
            shape = FormaBoton,
            border = BorderStroke(1.dp, if (habilitado && disponible) Amarillo else Borde),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AmarilloSuave,
                contentColor = Tinta,
                disabledContainerColor = Hueso,
                disabledContentColor = Gris
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics { if (!disponible && linea != null) stateDescription = linea }
        ) {
            if (estado == EstadoBotonIa.Pidiendo) {
                CircularProgressIndicator(color = Tinta, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Espaciado.dp8))
                Text(
                    text = stringResource(R.string.ia_redactar),
                    style = DonChambitasTema.tipografia.cuerpoFuerte,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (linea != null) {
            Text(
                text = linea,
                style = DonChambitasTema.tipografia.pie,
                color = Gris,
                modifier = Modifier.padding(start = Espaciado.dp4)
            )
        }
    }
}

/**
 * Dialogo de 1.9: con la descripcion ya escrita, la IA propone y la persona
 * decide (HU-30). La propuesta va sobre AmarilloSuave para que se lea como
 * texto ajeno, todavia no aceptado. Lo comparten P-11 y P-13.
 */
@Composable
internal fun DialogoPropuestaIa(
    propuesta: String,
    alAceptar: () -> Unit,
    alDescartar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alDescartar,
        icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Tinta) },
        title = {
            Text(
                text = stringResource(R.string.ia_reemplazar_titulo),
                style = DonChambitasTema.tipografia.subtitulo,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = propuesta,
                style = DonChambitasTema.tipografia.cuerpo,
                color = Tinta,
                modifier = Modifier
                    .clip(FormaCampo)
                    .background(AmarilloSuave)
                    .padding(Espaciado.dp12)
            )
        },
        confirmButton = {
            BotonPrincipal(texto = stringResource(R.string.ia_reemplazar), onClick = alAceptar)
        },
        dismissButton = {
            BotonTexto(
                texto = stringResource(R.string.ia_conservar),
                onClick = alDescartar,
                modifier = Modifier.fillMaxWidth()
            )
        },
        shape = FormaTarjeta,
        containerColor = Blanco
    )
}
