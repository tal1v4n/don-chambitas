package mx.donchambitas.app.ui.tema

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formas de componentes segun DISENO.md
val FormaBoton = RoundedCornerShape(16.dp)
val FormaCampo = RoundedCornerShape(14.dp)
val FormaTarjeta = RoundedCornerShape(20.dp)
val FormaChip = CircleShape
val FormaHojaInferior = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FormaFotoPerfil = CircleShape

// Mapeo a Shapes de Material 3
val Formas = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
