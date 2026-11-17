package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Hueso
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.Gris
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Error
import mx.donchambitas.app.ui.tema.FormaCampo
import mx.donchambitas.app.ui.tema.Grafito
import mx.donchambitas.app.ui.tema.cuerpo
import mx.donchambitas.app.ui.tema.pie
import mx.donchambitas.app.ui.tema.secundario

private val EspacioTextoError = 4.dp

/**
 * Campo de texto reutilizable segun DISENO.md.
 * Fondo Blanco, contorno Borde en reposo, Tinta al enfocar y Error cuando hay fallo.
 * Deshabilitado, el fondo baja a Hueso para que se note que no se puede escribir.
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String? = null,
    marcadorPosicion: String? = null,
    error: String? = null,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    iconoInicio: (@Composable () -> Unit)? = null,
    iconoFin: (@Composable () -> Unit)? = null,
    tecladoOpciones: KeyboardOptions = KeyboardOptions.Default,
    tecladoAcciones: KeyboardActions = KeyboardActions.Default,
    lineasMaximas: Int = 1,
    lineasMinimas: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val hayError = !error.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiarValor,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            readOnly = soloLectura,
            textStyle = DonChambitasTema.tipografia.cuerpo,
            label = etiqueta?.let {
                {
                    Text(
                        text = it,
                        style = DonChambitasTema.tipografia.secundario
                    )
                }
            },
            placeholder = marcadorPosicion?.let {
                {
                    Text(
                        text = it,
                        style = DonChambitasTema.tipografia.secundario,
                        color = Gris
                    )
                }
            },
            leadingIcon = iconoInicio,
            trailingIcon = iconoFin,
            isError = hayError,
            visualTransformation = visualTransformation,
            keyboardOptions = tecladoOpciones,
            keyboardActions = tecladoAcciones,
            singleLine = lineasMaximas == 1,
            maxLines = lineasMaximas,
            minLines = lineasMinimas,
            shape = FormaCampo,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                disabledContainerColor = Hueso,
                errorContainerColor = Blanco,
                focusedBorderColor = Tinta,
                unfocusedBorderColor = Borde,
                disabledBorderColor = Borde.copy(alpha = 0.5f),
                errorBorderColor = Error,
                focusedTextColor = Tinta,
                unfocusedTextColor = Tinta,
                disabledTextColor = Gris,
                errorTextColor = Tinta,
                focusedLabelColor = Tinta,
                focusedLeadingIconColor = Tinta,
                unfocusedLeadingIconColor = Gris,
                errorLeadingIconColor = Error,
                unfocusedLabelColor = Gris,
                disabledLabelColor = Gris.copy(alpha = 0.5f),
                errorLabelColor = Error,
                cursorColor = Grafito,
                errorCursorColor = Error
            )
        )

        if (hayError) {
            Text(
                text = error.orEmpty(),
                style = DonChambitasTema.tipografia.pie,
                color = Error,
                modifier = Modifier.padding(start = EspacioTextoError, top = EspacioTextoError)
            )
        }
    }
}

/**
 * Campo de texto especializado para contraseñas: candado al inicio y boton de alternar visibilidad.
 */
@Composable
fun CampoContrasena(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String? = null,
    marcadorPosicion: String? = null,
    error: String? = null,
    habilitado: Boolean = true,
    tecladoOpciones: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    tecladoAcciones: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }

    CampoTexto(
        valor = valor,
        alCambiarValor = alCambiarValor,
        etiqueta = etiqueta,
        marcadorPosicion = marcadorPosicion,
        error = error,
        habilitado = habilitado,
        tecladoOpciones = tecladoOpciones,
        tecladoAcciones = tecladoAcciones,
        iconoInicio = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null) },
        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
        iconoFin = {
            val descripcion = if (contrasenaVisible) {
                stringResource(R.string.ocultar_contrasena)
            } else {
                stringResource(R.string.mostrar_contrasena)
            }
            val icono = if (contrasenaVisible) {
                Icons.Outlined.VisibilityOff
            } else {
                Icons.Outlined.Visibility
            }

            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                Icon(
                    imageVector = icono,
                    contentDescription = descripcion,
                    tint = Gris
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaCampoTextoNormal() {
    DonChambitasTema {
        CampoTexto(
            valor = "Juan Pérez",
            alCambiarValor = {},
            etiqueta = "Nombre completo"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaCampoTextoConError() {
    DonChambitasTema {
        CampoTexto(
            valor = "correo-invalido",
            alCambiarValor = {},
            etiqueta = "Correo electrónico",
            error = "Ingresa un correo válido con formato usuario@dominio.com"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaCampoContrasena() {
    DonChambitasTema {
        CampoContrasena(
            valor = "123456",
            alCambiarValor = {},
            etiqueta = "Contraseña"
        )
    }
}
