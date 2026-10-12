package mx.donchambitas.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import kotlinx.coroutines.launch
import mx.donchambitas.app.datos.remoto.supabase.esEnlaceConSesion
import mx.donchambitas.app.datos.remoto.supabase.esEnlaceDeRecuperacion
import mx.donchambitas.app.datos.remoto.supabase.importarSesionDeEnlace
import mx.donchambitas.app.ui.navegacion.GrafoNavegacion
import mx.donchambitas.app.ui.navegacion.SesionViewModel
import mx.donchambitas.app.ui.tema.DonChambitasTema

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Perezoso: el cliente solo se crea si llega un enlace de Auth, asi que
    // la aplicacion arranca aunque falten las llaves en local.properties.
    @Inject
    lateinit var supabase: Lazy<SupabaseClient>

    // El mismo que recibe GrafoNavegacion: los dos lo piden a la actividad.
    private val sesionViewModel: SesionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Al recrearse la actividad (un giro) el intent sigue siendo el del
        // enlace: sin esto se importaria otra vez una sesion ya usada.
        if (savedInstanceState == null) procesarEnlace(intent)
        setContent {
            DonChambitasTema {
                GrafoNavegacion(sesionViewModel = sesionViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarEnlace(intent)
    }

    /**
     * Pasos 3 a 5 del recorrido de recuperacion (DEC-27): deja la sesion
     * importada y, si el enlace era el de recuperacion, avisa para que la
     * navegacion lleve al usuario a P-18.
     */
    private fun procesarEnlace(intent: Intent?) {
        val datos = intent?.data ?: return
        val fragmento = datos.fragment
        if (!esEnlaceConSesion(datos.scheme, datos.host, fragmento) || fragmento == null) return
        // Sin llaves el cliente no se puede crear; un enlace no debe cerrar la aplicacion por eso.
        val cliente = runCatching { supabase.get() }.getOrNull() ?: return
        lifecycleScope.launch {
            val importada = cliente.importarSesionDeEnlace(fragmento)
            if (importada && esEnlaceDeRecuperacion(fragmento)) sesionViewModel.alLlegarPorRecuperacion()
        }
    }
}
