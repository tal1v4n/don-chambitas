package mx.donchambitas.app.datos.falso

import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.modelo.PerfilHabilidad
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.Postulacion
import mx.donchambitas.app.dominio.modelo.Resena
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.modelo.Usuario

/**
 * Fuente de datos falsa en memoria para sostener la UI de la aplicacion
 * hasta que las implementaciones reales con Supabase entren en su respectivo turno.
 *
 * Contiene semillero coherente con 04_datos_semilla.sql:
 * - 16 categorias, 32 estados y 26 municipios.
 * - 8 trabajadores con perfil y servicios completos.
 * - 2 clientes de prueba.
 * - 5 solicitudes en estados abierta, asignada, cerrada y cancelada.
 * - 3 hilos de conversacion con mensajes.
 * - Resenas coherentes que solo aplican a solicitudes cerradas.
 */
@Singleton
class FuenteDatosFalsa @Inject constructor() {

    private val ahora = Instant.parse("2026-09-20T12:00:00Z")

    // Catalogos
    val categorias: MutableList<Categoria> = mutableListOf()
    val estados: MutableList<Estado> = mutableListOf()
    val municipios: MutableList<Municipio> = mutableListOf()

    // Entidades en memoria
    val usuarios: MutableList<Usuario> = mutableListOf()
    val perfilesTrabajador: MutableList<PerfilTrabajador> = mutableListOf()
    val habilidades: MutableList<PerfilHabilidad> = mutableListOf()
    val servicios: MutableList<Servicio> = mutableListOf()
    val fotosServicio: MutableList<ServicioFoto> = mutableListOf()
    val solicitudes: MutableList<Solicitud> = mutableListOf()
    val postulaciones: MutableList<Postulacion> = mutableListOf()
    val conversaciones: MutableList<Conversacion> = mutableListOf()
    val mensajes: MutableList<Mensaje> = mutableListOf()
    val resenas: MutableList<Resena> = mutableListOf()

    // Sesion activa en memoria
    private val _sesionActiva = MutableStateFlow<Sesion?>(null)
    val sesionActiva: StateFlow<Sesion?> = _sesionActiva.asStateFlow()

    init {
        inicializarDatos()
    }

    fun fijarSesionActiva(sesion: Sesion?) {
        _sesionActiva.value = sesion
    }

    fun obtenerUsuarioActivoId(): String? = _sesionActiva.value?.usuario?.id

    fun reiniciar() {
        categorias.clear()
        estados.clear()
        municipios.clear()
        usuarios.clear()
        perfilesTrabajador.clear()
        habilidades.clear()
        servicios.clear()
        fotosServicio.clear()
        solicitudes.clear()
        postulaciones.clear()
        conversaciones.clear()
        mensajes.clear()
        resenas.clear()
        inicializarDatos()
    }

    private fun inicializarDatos() {
        // 1. Categorias de 04_datos_semilla.sql
        val listaCategorias = listOf(
            Categoria(1, "Plomeria", "Fugas, instalaciones hidraulicas y drenajes", "plumbing", true, 10),
            Categoria(2, "Electricidad", "Instalaciones electricas, cortos y luminarias", "bolt", true, 20),
            Categoria(3, "Albanileria", "Muros, pisos, castillos y acabados", "construction", true, 30),
            Categoria(4, "Carpinteria", "Muebles a medida, puertas y reparaciones en madera", "carpenter", true, 40),
            Categoria(5, "Pintura", "Interiores, exteriores e impermeabilizacion", "format_paint", true, 50),
            Categoria(6, "Herreria", "Rejas, portones, barandales y soldadura", "hardware", true, 60),
            Categoria(7, "Limpieza", "Limpieza de casas, oficinas y mudanzas", "cleaning_services", true, 70),
            Categoria(8, "Jardineria", "Poda, mantenimiento de jardines y riego", "yard", true, 80),
            Categoria(9, "Mudanzas y carga", "Fletes, cargadores y traslados", "local_shipping", true, 90),
            Categoria(10, "Aire y refrigeracion", "Minisplits, refrigeradores y ventilacion", "ac_unit", true, 100),
            Categoria(11, "Mecanica", "Reparacion automotriz y servicio a domicilio", "car_repair", true, 110),
            Categoria(12, "Computo", "Reparacion de equipos, redes y respaldo de datos", "computer", true, 120),
            Categoria(13, "Cerrajeria", "Aperturas, cambio de chapas y duplicado de llaves", "key", true, 130),
            Categoria(14, "Costura", "Arreglos de ropa, confeccion y tapiceria", "content_cut", true, 140),
            Categoria(15, "Cocina y eventos", "Banquetes, meseros y servicio para eventos", "restaurant", true, 150),
            Categoria(16, "Otros", "Oficios que no encajan en las categorias anteriores", "more_horiz", true, 999)
        )
        categorias.addAll(listaCategorias)

        // 2. Estados de 04_datos_semilla.sql (32 entidades)
        val nombresEstados = listOf(
            "Aguascalientes" to "AGU", "Baja California" to "BCN", "Baja California Sur" to "BCS",
            "Campeche" to "CAM", "Coahuila" to "COA", "Colima" to "COL", "Chiapas" to "CHP",
            "Chihuahua" to "CHH", "Ciudad de Mexico" to "CMX", "Durango" to "DUR", "Guanajuato" to "GUA",
            "Guerrero" to "GRO", "Hidalgo" to "HID", "Jalisco" to "JAL", "Estado de Mexico" to "MEX",
            "Michoacan" to "MIC", "Morelos" to "MOR", "Nayarit" to "NAY", "Nuevo Leon" to "NLE",
            "Oaxaca" to "OAX", "Puebla" to "PUE", "Queretaro" to "QUE", "Quintana Roo" to "ROO",
            "San Luis Potosi" to "SLP", "Sinaloa" to "SIN", "Sonora" to "SON", "Tabasco" to "TAB",
            "Tamaulipas" to "TAM", "Tlaxcala" to "TLA", "Veracruz" to "VER", "Yucatan" to "YUC",
            "Zacatecas" to "ZAC"
        )
        nombresEstados.forEachIndexed { index, (nom, clave) ->
            estados.add(Estado(index + 1, nom, clave))
        }

        val idCmx = estados.first { it.clave == "CMX" }.id
        val idMex = estados.first { it.clave == "MEX" }.id
        val idPue = estados.first { it.clave == "PUE" }.id
        val idJal = estados.first { it.clave == "JAL" }.id
        val idNle = estados.first { it.clave == "NLE" }.id

        // 3. Municipios de 04_datos_semilla.sql (26 municipios)
        var munId = 1
        listOf("Alvaro Obregon", "Azcapotzalco", "Benito Juarez", "Coyoacan", "Cuauhtemoc",
            "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Miguel Hidalgo", "Tlalpan", "Venustiano Carranza")
            .forEach { municipios.add(Municipio(munId++, idCmx, it)) }
        listOf("Ecatepec de Morelos", "Naucalpan de Juarez", "Nezahualcoyotl", "Tlalnepantla de Baz", "Toluca", "Chalco")
            .forEach { municipios.add(Municipio(munId++, idMex, it)) }
        listOf("Puebla", "Cholula", "Atlixco")
            .forEach { municipios.add(Municipio(munId++, idPue, it)) }
        listOf("Guadalajara", "Zapopan", "Tlaquepaque")
            .forEach { municipios.add(Municipio(munId++, idJal, it)) }
        listOf("Monterrey", "San Nicolas de los Garza", "Guadalupe")
            .forEach { municipios.add(Municipio(munId++, idNle, it)) }

        val idMunCholula = municipios.first { it.nombre == "Cholula" }.id
        val idMunCoyoacan = municipios.first { it.nombre == "Coyoacan" }.id
        val idMunZapopan = municipios.first { it.nombre == "Zapopan" }.id
        val idMunMonterrey = municipios.first { it.nombre == "Monterrey" }.id
        val idMunToluca = municipios.first { it.nombre == "Toluca" }.id
        val idMunCuauhtemoc = municipios.first { it.nombre == "Cuauhtemoc" }.id
        val idMunGuadalajara = municipios.first { it.nombre == "Guadalajara" }.id
        val idMunPuebla = municipios.first { it.nombre == "Puebla" }.id

        // 4. Clientes de prueba
        val cliente1 = Usuario(
            id = "usr-cli-1",
            correo = "juan.perez@ejemplo.com",
            nombre = "Juan",
            apellidos = "Perez Hernandez",
            telefono = "2221234567",
            rol = RolUsuario.CLIENTE,
            fotoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            activo = true,
            creadoEn = ahora.minusSeconds(86400 * 30),
            actualizadoEn = ahora.minusSeconds(86400 * 30)
        )
        val cliente2 = Usuario(
            id = "usr-cli-2",
            correo = "maria.garcia@ejemplo.com",
            nombre = "Maria",
            apellidos = "Garcia Lopez",
            telefono = "5559876543",
            rol = RolUsuario.CLIENTE,
            fotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            activo = true,
            creadoEn = ahora.minusSeconds(86400 * 25),
            actualizadoEn = ahora.minusSeconds(86400 * 25)
        )
        usuarios.addAll(listOf(cliente1, cliente2))

        // 5. 8 Trabajadores de prueba con perfil, oficio, habilidades y servicios
        data class TrabajadorDef(
            val id: String,
            val correo: String,
            val nombre: String,
            val apellidos: String,
            val telefono: String,
            val fotoUrl: String,
            val titulo: String,
            val descripcion: String,
            val experiencia: Int,
            val estadoId: Int,
            val municipioId: Int,
            val habilidades: List<String>,
            val catId: Int,
            val servTitulo: String,
            val servDesc: String,
            val pDesde: BigDecimal,
            val pHasta: BigDecimal,
            val unidad: String
        )

        val listaTrabajadores = listOf(
            TrabajadorDef(
                "usr-trab-1", "pedro.plomero@ejemplo.com", "Pedro", "Ramirez Soto", "2225550101",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "Plomero e instalador de gas certificado",
                "Mas de 10 anos atendiendo fugas, cambio de griferia, boilers y calentadores solares.",
                10, idPue, idMunCholula, listOf("Cobre", "Termofusion", "Boilers", "Drenajes"),
                1, "Reparacion de fugas e instalacion de griferia", "Deteccion y reparacion de fugas de agua y gas.",
                BigDecimal("350.00"), BigDecimal("800.00"), "por trabajo"
            ),
            TrabajadorDef(
                "usr-trab-2", "elena.electricista@ejemplo.com", "Elena", "Torres Mendez", "5555550102",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
                "Tecnica electricista residencial e industrial",
                "Especialista en balanceo de cargas, cableado, cortos y cambio de centros de carga.",
                8, idCmx, idMunCoyoacan, listOf("Cortocircuitos", "Tableros 220V", "Iluminacion LED"),
                2, "Instalacion electrica y correccion de cortos", "Diagnostico preciso y cableado seguro bajo norma.",
                BigDecimal("400.00"), BigDecimal("1200.00"), "por trabajo"
            ),
            TrabajadorDef(
                "usr-trab-3", "carlos.carpintero@ejemplo.com", "Carlos", "Mendoza Diaz", "3335550103",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                "Carpinteria fina y restauracion de madera",
                "Fabricacion de closets, cocinas integrales, puertas y reparacion de muebles finos.",
                12, idJal, idMunZapopan, listOf("Muebles a medida", "Barniz", "Closets", "Puertas"),
                4, "Muebles sobre diseno y reparaciones en madera", "Carpinteria a medida con maderas de primera calidad.",
                BigDecimal("500.00"), BigDecimal("2500.00"), "por trabajo"
            ),
            TrabajadorDef(
                "usr-trab-4", "pablo.pintor@ejemplo.com", "Pablo", "Sanchez Trevino", "8185550104",
                "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
                "Pintor profesional e impermeabilizante",
                "Pintura vinilica, esmalte, pasta texturizada e impermeabilizacion de azoteas.",
                7, idNle, idMunMonterrey, listOf("Pintura exterior", "Impermeabilizacion", "Texturizados"),
                5, "Pintura de interiores, fachadas e impermeabilizado", "Acabados limpios sin manchas y garantia por escrito.",
                BigDecimal("300.00"), BigDecimal("1500.00"), "por dia"
            ),
            TrabajadorDef(
                "usr-trab-5", "alberto.albanil@ejemplo.com", "Alberto", "Lopez Fuentes", "7225550105",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150",
                "Maestro albanil y colocador de pisos",
                "Construccion de muros, castillos, aplanados, colocacion de loseta y porcelanato.",
                15, idMex, idMunToluca, listOf("Porcelanato", "Aplanados", "Muros", "Techumbres"),
                3, "Albañileria general y colocacion de pisos", "Construccion solida y nivelacion milimetrica de pisos.",
                BigDecimal("450.00"), BigDecimal("1800.00"), "por m2"
            ),
            TrabajadorDef(
                "usr-trab-6", "jorge.jardinero@ejemplo.com", "Jorge", "Vargas Cruz", "5555550106",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
                "Jardineria y mantenimiento de areas verdes",
                "Poda estetica, derribo de ramas, sistemas de riego, control de plagas y fertilizacion.",
                6, idCmx, idMunCuauhtemoc, listOf("Poda", "Pasto en rollo", "Fumigacion", "Diseno floral"),
                8, "Mantenimiento integral de jardines y podas", "Limpieza profunda y embellecimiento de areas verdes.",
                BigDecimal("250.00"), BigDecimal("700.00"), "por visita"
            ),
            TrabajadorDef(
                "usr-trab-7", "mario.mecanico@ejemplo.com", "Mario", "Ortiz Delgado", "3335550107",
                "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
                "Mecanico automotriz y frenos a domicilio",
                "Afinaciones mayores, frenos, suspension, cambio de bandas y escaneo por computadora.",
                9, idJal, idMunGuadalajara, listOf("Escaneo OBD2", "Frenos ABS", "Suspension", "Afinacion"),
                11, "Afinacion y revision automotriz a domicilio", "Servicio mecanico rapido en la comodidad de tu cochera.",
                BigDecimal("600.00"), BigDecimal("2000.00"), "por servicio"
            ),
            TrabajadorDef(
                "usr-trab-8", "lucia.limpieza@ejemplo.com", "Lucia", "Morales Rios", "2225550108",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
                "Servicios profesionales de limpieza residencial",
                "Limpieza profunda tras mudanzas, oficinas, lavado de tapiceria y sanitizacion.",
                5, idPue, idMunPuebla, listOf("Limpieza profunda", "Desinfeccion", "Tapiceria"),
                7, "Limpieza exhaustiva de casas y departamentos", "Insumos biodegradables y maxima atencion al detalle.",
                BigDecimal("350.00"), BigDecimal("900.00"), "por jornada"
            )
        )

        listaTrabajadores.forEachIndexed { i, t ->
            val usr = Usuario(
                id = t.id,
                correo = t.correo,
                nombre = t.nombre,
                apellidos = t.apellidos,
                telefono = t.telefono,
                rol = RolUsuario.TRABAJADOR,
                fotoUrl = t.fotoUrl,
                activo = true,
                creadoEn = ahora.minusSeconds((86400 * (40 - i)).toLong()),
                actualizadoEn = ahora.minusSeconds((86400 * (40 - i)).toLong())
            )
            usuarios.add(usr)

            val perfil = PerfilTrabajador(
                usuarioId = t.id,
                rol = RolUsuario.TRABAJADOR,
                titulo = t.titulo,
                descripcion = t.descripcion,
                experienciaAnios = t.experiencia,
                telefonoContacto = t.telefono,
                estadoId = t.estadoId,
                municipioId = t.municipioId,
                disponible = true,
                creadoEn = usr.creadoEn,
                actualizadoEn = usr.actualizadoEn
            )
            perfilesTrabajador.add(perfil)

            t.habilidades.forEach { h ->
                habilidades.add(PerfilHabilidad(t.id, h))
            }

            val servId = "serv-${i + 1}"
            val serv = Servicio(
                id = servId,
                perfilId = t.id,
                categoriaId = t.catId,
                titulo = t.servTitulo,
                descripcion = t.servDesc,
                precioDesde = t.pDesde,
                precioHasta = t.pHasta,
                unidadPrecio = t.unidad,
                activo = true,
                creadoEn = usr.creadoEn,
                actualizadoEn = usr.actualizadoEn
            )
            servicios.add(serv)

            fotosServicio.add(
                ServicioFoto(
                    id = "foto-${i + 1}-1",
                    servicioId = servId,
                    url = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400",
                    posicion = 1,
                    creadoEn = usr.creadoEn
                )
            )
        }

        // 6. 5 Solicitudes en distintos estados
        val sol1 = Solicitud(
            id = "sol-1",
            clienteId = cliente1.id,
            categoriaId = 1, // Plomeria
            titulo = "Reparacion de fuga bajo lavabo de cocina",
            descripcion = "Tengo una fuga constante en la tuberia del cespol que gotea dentro del mueble de cocina.",
            presupuesto = BigDecimal("450.00"),
            estadoId = idPue,
            municipioId = idMunCholula,
            estatus = EstadoSolicitud.ABIERTA,
            trabajadorId = null,
            categorizadaPorIa = false,
            creadoEn = ahora.minusSeconds(86400 * 3),
            actualizadoEn = ahora.minusSeconds(86400 * 3)
        )
        val sol2 = Solicitud(
            id = "sol-2",
            clienteId = cliente2.id,
            categoriaId = 10, // Aire y refrigeracion
            titulo = "Mantenimiento preventivo de minisplit",
            descripcion = "Requiere limpieza de filtros y revision de gas para equipo de 1.5 toneladas en recamara.",
            presupuesto = BigDecimal("850.00"),
            estadoId = idCmx,
            municipioId = idMunCoyoacan,
            estatus = EstadoSolicitud.ABIERTA,
            trabajadorId = null,
            categorizadaPorIa = false,
            creadoEn = ahora.minusSeconds(86400 * 2),
            actualizadoEn = ahora.minusSeconds(86400 * 2)
        )
        val sol3 = Solicitud(
            id = "sol-3",
            clienteId = cliente1.id,
            categoriaId = 6, // Herreria
            titulo = "Alineacion y soldadura de porton corredizo",
            descripcion = "El riel inferior se desoldo y el porton se traba a medio cerrar.",
            presupuesto = BigDecimal("1500.00"),
            estadoId = idJal,
            municipioId = idMunZapopan,
            estatus = EstadoSolicitud.ASIGNADA,
            trabajadorId = "usr-trab-3",
            categorizadaPorIa = false,
            creadoEn = ahora.minusSeconds(86400 * 5),
            actualizadoEn = ahora.minusSeconds(86400 * 1)
        )
        val sol4 = Solicitud(
            id = "sol-4",
            clienteId = cliente2.id,
            categoriaId = 5, // Pintura
            titulo = "Pintura exterior para fachada de dos pisos",
            descripcion = "Aplicacion de sellador y dos manos de pintura vinilica lavable en fachada principal.",
            presupuesto = BigDecimal("3200.00"),
            estadoId = idNle,
            municipioId = idMunMonterrey,
            estatus = EstadoSolicitud.CERRADA,
            trabajadorId = "usr-trab-4",
            categorizadaPorIa = false,
            creadoEn = ahora.minusSeconds(86400 * 15),
            actualizadoEn = ahora.minusSeconds(86400 * 4),
            cerradaEn = ahora.minusSeconds(86400 * 4)
        )
        val sol5 = Solicitud(
            id = "sol-5",
            clienteId = cliente1.id,
            categoriaId = 13, // Cerrajeria
            titulo = "Cambio de cilindro y chapa de seguridad",
            descripcion = "Necesito cambiar el cilindro de la puerta principal tras extraviar un juego de llaves.",
            presupuesto = BigDecimal("600.00"),
            estadoId = idMex,
            municipioId = idMunToluca,
            estatus = EstadoSolicitud.CANCELADA,
            trabajadorId = null,
            categorizadaPorIa = false,
            creadoEn = ahora.minusSeconds(86400 * 10),
            actualizadoEn = ahora.minusSeconds(86400 * 8)
        )
        solicitudes.addAll(listOf(sol1, sol2, sol3, sol4, sol5))

        // 7. Postulaciones respetando restricciones:
        // Solicitud 1 (abierta) tiene postulacion enviada
        postulaciones.add(
            Postulacion(
                id = "post-1",
                solicitudId = sol1.id,
                trabajadorId = "usr-trab-1",
                mensaje = "Buenas tardes, tengo herramienta y refacciones para arreglar la fuga hoy mismo.",
                precioPropuesto = BigDecimal("400.00"),
                estatus = EstadoPostulacion.ENVIADA,
                creadoEn = ahora.minusSeconds(86400 * 2),
                actualizadoEn = ahora.minusSeconds(86400 * 2)
            )
        )
        // Solicitud 3 (asignada) tiene la postulacion aceptada
        postulaciones.add(
            Postulacion(
                id = "post-3",
                solicitudId = sol3.id,
                trabajadorId = "usr-trab-3",
                mensaje = "Puedo llevar equipo de soldadura portatil y ajustarlo en un par de horas.",
                precioPropuesto = BigDecimal("1500.00"),
                estatus = EstadoPostulacion.ACEPTADA,
                creadoEn = ahora.minusSeconds(86400 * 4),
                actualizadoEn = ahora.minusSeconds(86400 * 1)
            )
        )
        // Solicitud 4 (cerrada) tiene la postulacion que gano
        postulaciones.add(
            Postulacion(
                id = "post-4",
                solicitudId = sol4.id,
                trabajadorId = "usr-trab-4",
                mensaje = "Cuento con andamios propios y equipo de aplicacion profesional.",
                precioPropuesto = BigDecimal("3200.00"),
                estatus = EstadoPostulacion.ACEPTADA,
                creadoEn = ahora.minusSeconds(86400 * 14),
                actualizadoEn = ahora.minusSeconds(86400 * 10)
            )
        )

        // 8. Resenas coherentes:
        // Solo sobre la solicitud cerrada (sol-4), creada por el cliente que publico (cliente2) sobre el trabajador asignado (usr-trab-4)
        resenas.add(
            Resena(
                id = "res-1",
                solicitudId = sol4.id,
                clienteId = cliente2.id,
                trabajadorId = "usr-trab-4",
                calificacion = 5,
                comentario = "Excelente trabajo de pintura. Muy limpio, puntual y cuido perfectamente las ventanas y el piso.",
                creadoEn = ahora.minusSeconds(86400 * 3)
            )
        )

        // 9. 3 Conversaciones con mensajes
        val conv1 = Conversacion(
            id = "conv-1",
            clienteId = cliente1.id,
            trabajadorId = "usr-trab-1",
            solicitudId = sol1.id,
            creadoEn = ahora.minusSeconds(86400 * 2),
            ultimoMensajeEn = ahora.minusSeconds(86400 * 1)
        )
        val conv2 = Conversacion(
            id = "conv-2",
            clienteId = cliente1.id,
            trabajadorId = "usr-trab-3",
            solicitudId = null,
            creadoEn = ahora.minusSeconds(86400 * 6),
            ultimoMensajeEn = ahora.minusSeconds(86400 * 5)
        )
        val conv3 = Conversacion(
            id = "conv-3",
            clienteId = cliente2.id,
            trabajadorId = "usr-trab-4",
            solicitudId = sol4.id,
            creadoEn = ahora.minusSeconds(86400 * 12),
            ultimoMensajeEn = ahora.minusSeconds(86400 * 4)
        )
        conversaciones.addAll(listOf(conv1, conv2, conv3))

        mensajes.addAll(
            listOf(
                Mensaje("msg-1-1", conv1.id, cliente1.id, "Hola maestro Pedro, vi su postulacion. ¿Podria venir manana a las 10?", ahora.minusSeconds(86400 * 2), ahora.minusSeconds(86400 * 2)),
                Mensaje("msg-1-2", conv1.id, "usr-trab-1", "Buenas tardes Juan, con gusto. Llevo herramienta para desarmar y sustituir empaques.", ahora.minusSeconds(86400 * 1 + 3600), ahora.minusSeconds(86400 * 1 + 3600)),
                Mensaje("msg-1-3", conv1.id, cliente1.id, "Perfecto, aqui lo espero. Muchas gracias.", null, ahora.minusSeconds(86400 * 1)),

                Mensaje("msg-2-1", conv2.id, cliente1.id, "Buenas tardes Carlos, ¿haces muebles a medida para oficina?", ahora.minusSeconds(86400 * 6), ahora.minusSeconds(86400 * 6)),
                Mensaje("msg-2-2", conv2.id, "usr-trab-3", "Hola Juan, si claro. Trabajo sobre diseno o te puedo presentar opciones en madera de pino o encino.", ahora.minusSeconds(86400 * 5), ahora.minusSeconds(86400 * 5)),

                Mensaje("msg-3-1", conv3.id, cliente2.id, "Hola Pablo, muchisimas gracias por la atencion y el detalle en la fachada.", ahora.minusSeconds(86400 * 4), ahora.minusSeconds(86400 * 4)),
                Mensaje("msg-3-2", conv3.id, "usr-trab-4", "Para servirle Sra. Maria, fue un gusto apoyarla con su proyecto.", ahora.minusSeconds(86400 * 4 - 1800), ahora.minusSeconds(86400 * 4 - 1800))
            )
        )

        // Sesion por defecto con cliente1
        fijarSesionActiva(Sesion(cliente1))
    }
}
