package mx.donchambitas.app.ui.pantallas

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import java.io.File
import mx.donchambitas.app.R
import mx.donchambitas.app.ui.componentes.BotonSecundario
import mx.donchambitas.app.ui.tema.Carbon
import mx.donchambitas.app.ui.tema.Crema
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.ui.tema.Espaciado
import mx.donchambitas.app.ui.tema.FormaHojaInferior
import mx.donchambitas.app.ui.tema.subtitulo

/**
 * Hoja inferior para elegir de donde sale una foto: galeria o camara.
 * La comparten P-18 (decision 2 de S2-T13) y P-13 (S3-T04).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpcionesFoto(
    titulo: String,
    alElegirDeGaleria: () -> Unit,
    alTomarFoto: () -> Unit,
    alCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        shape = FormaHojaInferior,
        containerColor = Crema
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Espaciado.margenPantalla)
                .padding(bottom = Espaciado.dp24),
            verticalArrangement = Arrangement.spacedBy(Espaciado.dp12)
        ) {
            Text(
                text = titulo,
                style = DonChambitasTema.tipografia.subtitulo,
                color = Carbon
            )
            BotonSecundario(
                texto = stringResource(R.string.foto_elegir_galeria),
                onClick = alElegirDeGaleria
            )
            BotonSecundario(
                texto = stringResource(R.string.foto_tomar),
                onClick = alTomarFoto
            )
        }
    }
}

/**
 * Archivo donde la camara del sistema deja la foto, expuesto con el
 * FileProvider de AndroidManifest.xml. Se reescribe en cada toma.
 */
internal fun archivoDeCamara(contexto: Context): Uri {
    val archivo = File(contexto.cacheDir, "fotos/camara.jpg").apply { parentFile?.mkdirs() }
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
}
