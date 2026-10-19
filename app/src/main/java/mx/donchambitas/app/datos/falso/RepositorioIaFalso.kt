package mx.donchambitas.app.datos.falso

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.FuncionIa
import mx.donchambitas.app.dominio.modelo.ResultadoIa
import mx.donchambitas.app.dominio.repositorio.RepositorioIa
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioIaFalso @Inject constructor() : RepositorioIa {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null
    var llamadasRestantes: Int = 8

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioIa: $it") }
    }

    override suspend fun generar(
        funcion: FuncionIa,
        entrada: String
    ): Resultado<ResultadoIa> {
        verificarSimulacion()?.let { return it }

        if (llamadasRestantes <= 0) {
            return Resultado.Error(TipoError.LIMITE_IA, "Alcanzaste el limite de hoy")
        }

        llamadasRestantes--

        val textoGenerado = when (funcion) {
            FuncionIa.REDACTAR_PERFIL ->
                "Profesional comprometido con la calidad y la puntualidad. Con amplia experiencia práctica en $entrada, ofreciendo soluciones garantizadas, atención personalizada y presupuestos claros."

            FuncionIa.REDACTAR_SERVICIO ->
                "Servicio profesional especializado en $entrada. Diagnóstico puntual, uso de materiales de primera calidad, limpieza total al finalizar y garantía por escrito."

            FuncionIa.CATEGORIZAR ->
                "Plomería"

            FuncionIa.SUGERIR ->
                "Sugerencia: describe el problema con el mayor detalle posible e incluye medidas aproximadas para recibir cotizaciones más precisas."
        }

        return Resultado.Exito(
            ResultadoIa(
                resultado = textoGenerado,
                desdeCache = false,
                llamadasRestantesHoy = llamadasRestantes
            )
        )
    }
}
