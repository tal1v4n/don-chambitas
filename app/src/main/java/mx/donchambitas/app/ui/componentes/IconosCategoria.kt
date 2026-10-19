package mx.donchambitas.app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CarRepair
import androidx.compose.material.icons.outlined.Carpenter
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Hardware
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Traduce la columna `categorias.icono` al icono de Material Icons Outlined.
 * Los nombres son los de la tabla de iconos de DISENO.md y de 04_datos_semilla.sql.
 * Un nombre nulo o desconocido cae en el icono de "Otros", nunca en un hueco.
 */
fun iconoCategoria(nombre: String?): ImageVector = when (nombre) {
    "plumbing" -> Icons.Outlined.Plumbing
    "bolt" -> Icons.Outlined.Bolt
    "construction" -> Icons.Outlined.Construction
    "carpenter" -> Icons.Outlined.Carpenter
    "format_paint" -> Icons.Outlined.FormatPaint
    "hardware" -> Icons.Outlined.Hardware
    "cleaning_services" -> Icons.Outlined.CleaningServices
    "yard" -> Icons.Outlined.Yard
    "local_shipping" -> Icons.Outlined.LocalShipping
    "ac_unit" -> Icons.Outlined.AcUnit
    "car_repair" -> Icons.Outlined.CarRepair
    "computer" -> Icons.Outlined.Computer
    "key" -> Icons.Outlined.Key
    "content_cut" -> Icons.Outlined.ContentCut
    "restaurant" -> Icons.Outlined.Restaurant
    else -> Icons.Outlined.MoreHoriz
}
