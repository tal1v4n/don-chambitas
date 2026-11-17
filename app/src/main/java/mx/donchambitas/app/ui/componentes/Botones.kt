package mx.donchambitas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.donchambitas.app.ui.tema.Amarillo
import mx.donchambitas.app.ui.tema.Blanco
import mx.donchambitas.app.ui.tema.Borde
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.FormaBoton
import mx.donchambitas.app.ui.tema.Grafito
import mx.donchambitas.app.ui.tema.Naranja
import mx.donchambitas.app.ui.tema.Tinta
import mx.donchambitas.app.ui.tema.cuerpoFuerte

private val AlturaBoton = 54.dp
private val GrosorBorde = 1.dp
private val TamanoIndicador = 20.dp
private val GrosorIndicador = 2.dp
private val TamanoIconoBoton = 20.dp

/** La flecha que acompana a las acciones que avanzan a otra pantalla. */
val IconoAvanzar: ImageVector = Icons.AutoMirrored.Outlined.ArrowForward

/**
 * Texto del boton con su icono opcional a la derecha, o el indicador de carga
 * en su lugar. Compartido por todos los botones para que midan y se lean igual.
 */
@Composable
private fun ContenidoBoton(
    texto: String,
    cargando: Boolean,
    colorIndicador: Color,
    icono: ImageVector?
) {
    if (cargando) {
        CircularProgressIndicator(
            modifier = Modifier.size(TamanoIndicador),
            color = colorIndicador,
            strokeWidth = GrosorIndicador
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = texto,
                style = DonChambitasTema.tipografia.cuerpoFuerte,
                fontWeight = FontWeight.SemiBold
            )
            if (icono != null) {
                Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(TamanoIconoBoton))
            }
        }
    }
}

/**
 * Boton principal de la aplicacion: relleno Amarillo, texto Tinta, 54 dp de alto.
 * Sobre Amarillo va texto Tinta (11.78:1 WCAG AAA).
 *
 * @param icono Opcional, a la derecha del texto; [IconoAvanzar] en las acciones que avanzan.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = Amarillo,
            contentColor = Tinta,
            disabledContainerColor = Amarillo.copy(alpha = 0.45f),
            disabledContentColor = Tinta.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        ContenidoBoton(texto, cargando, Tinta, icono)
    }
}

/**
 * Boton secundario: relleno Blanco con contorno Borde y texto del color dado.
 *
 * @param color Texto y, si no es Grafito, tambien el contorno. Grafito salvo en
 * acciones que cierran algo, como "Cerrar sesion" de P-18, que van en Error
 * (WIREFRAMES.md, P-18).
 */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = Grafito,
    icono: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val colorContorno = if (color == Grafito) Borde else color
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        border = BorderStroke(
            width = GrosorBorde,
            color = if (habilitado && !cargando) colorContorno else colorContorno.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Blanco,
            contentColor = color,
            disabledContainerColor = Blanco,
            disabledContentColor = color.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        ContenidoBoton(texto, cargando, color, icono)
    }
}

/**
 * Boton destacado para acciones de conversion (postularse, publicar): color Naranja, texto Blanco.
 * Sobre Naranja va texto Blanco (4.98:1 WCAG AA).
 */
@Composable
fun BotonDestacado(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = Naranja,
            contentColor = Blanco,
            disabledContainerColor = Naranja.copy(alpha = 0.5f),
            disabledContentColor = Blanco.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(AlturaBoton)
    ) {
        ContenidoBoton(texto, cargando, Blanco, icono)
    }
}

/**
 * Boton plano de texto sin fondo, color Grafito.
 */
@Composable
fun BotonTexto(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaBoton,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = Grafito,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Grafito.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        ContenidoBoton(texto, cargando, Grafito, null)
    }
}

/**
 * Boton flotante de "publicar": pildora Naranja con icono y texto Blanco
 * (4.98:1) y sombra, para la accion principal de una lista. Toda la pildora es
 * un solo nodo con su texto como texto y como descripcion.
 */
@Composable
fun BotonFlotante(
    texto: String,
    icono: ImageVector,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .shadow(10.dp, FormaBoton, ambientColor = Naranja, spotColor = Naranja)
            .clip(FormaBoton)
            .background(Naranja)
            .clickable(role = Role.Button, onClick = alPulsar)
            // Un solo nodo que se anuncia y se encuentra por su texto o por su descripcion.
            .clearAndSetSemantics {
                contentDescription = texto
                text = AnnotatedString(texto)
                role = Role.Button
            }
            .height(AlturaBoton)
            .padding(horizontal = 20.dp)
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Blanco, modifier = Modifier.size(TamanoIconoBoton))
        Text(
            text = texto,
            style = DonChambitasTema.tipografia.cuerpoFuerte,
            fontWeight = FontWeight.SemiBold,
            color = Blanco
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaBotonPrincipal() {
    DonChambitasTema {
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaBotonPrincipalCargando() {
    DonChambitasTema {
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = {},
            cargando = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaBotonSecundario() {
    DonChambitasTema {
        BotonSecundario(
            texto = "Crear cuenta",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaBotonDestacado() {
    DonChambitasTema {
        BotonDestacado(
            texto = "Postularme al trabajo",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFEFA)
@Composable
private fun PreviaBotonTexto() {
    DonChambitasTema {
        BotonTexto(
            texto = "¿Olvidaste tu contraseña?",
            onClick = {}
        )
    }
}
