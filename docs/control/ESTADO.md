# Estado actual

> Archivo **vivo**. Quien termina una tarea lo actualiza. Es la primera cosa
> que lee el agente y la única fuente confiable sobre qué está pasando hoy.

**Última actualización:** 2026-10-19

---

## Sprint en curso

| Campo | Valor |
|---|---|
| Sprint | 3 |
| Fechas | PENDIENTE |
| Tareas del sprint | 15 |
| Terminadas | 7 |
| En curso | 0 |
| Bloqueadas | 0 |

> Sprint 3 abierto el 2026-10-18 por autorización del líder. Sus tickets se
> redactan uno por uno al tomar cada tarea, como en el Sprint 2.
>
> Sprint 2 cerrado con sus 16 tareas en `hecha` (la última, `S2-T16`, con el
> #19). Sprint 1 cerrado el 2026-09-20 con sus 16 tareas en `hecha`.

## Qué se puede probar hoy en la aplicación

Léelo antes de instalar el APK y reportar que algo "no funciona". Desde que
`S2-T07` se integró en `main`, `RepositorioAuth` está enlazado a
`RepositorioAuthReal`: P-02, P-03 y P-04 hablan con Supabase Auth, y **las
cuentas sembradas de `FuenteDatosFalsa` ya no sirven para entrar**. Las filas
de P-04 describen lo que dejó `S2-T10`, integrada con el #13.

| Si haces esto | Pasa esto hoy | Lo arregla |
|---|---|---|
| Abres la aplicación **sin sesión guardada** | P-01 dura al menos 800 ms y te deja en P-02 | — |
| Abres la aplicación **con sesión guardada** | P-01 y luego P-05 o P-10 según tu rol, sin pasar por P-02. Atrás cierra la aplicación. Probado con `ana.rls@…` y `beto.rls@…` | — |
| Abres la aplicación con sesión guardada y **sin red** | A los 8 segundos, "Sin conexión" con "Reintentar" en P-01. La sesión no se borra, y con la red de vuelta "Reintentar" entra | — |
| Tocas la pestaña **Cuenta** | P-18 real: tu ficha de `public.usuarios`, editar datos, cambiar contraseña y cerrar sesión. El trabajador ve además "Mi perfil profesional" (P-11) y "Mis postulaciones" (P-14) | — |
| Editas nombre, apellidos o teléfono y guardas | La tarjeta cambia en ese momento y sigue así al reabrir. El teléfono es obligatorio, como en P-03 | — |
| Tocas el avatar de P-18 | Hoja con "Elegir de la galería" y "Tomar foto", y luego el recortador. Una imagen de más de 5 MB o que no sea JPG, PNG o WebP se rechaza con el motivo | — |
| Como trabajador, tocas "Mi perfil profesional" en P-18 | P-11 carga y guarda: hojas de estado y municipio con datos (solo los municipios del estado), "Perfil guardado" y el aviso de incompleto recalculado al guardar. **Contra el repositorio falso**: lo guardado vive en memoria hasta cerrar la aplicación, y la fuente falsa lo atribuye a `cliente1`, no a tu cuenta (`DEC-31`). Valida como P-03: cada campo al salir de él y todos al guardar, con el foco en el primer error; una habilidad vacía, larga o repetida no se agrega | `S3-T09` |
| Como trabajador, tocas la pestaña **Servicios** | P-12 carga tus servicios, activos primero. La hoja de acciones edita, pausa o reactiva y elimina con confirmación, cada una con su aviso. Recarga al volver de P-13. Arranca vacía por lo mismo que P-11: el falso no conoce tu cuenta | `S3-T09` |
| Como trabajador, publicas un servicio en P-13 | Las 16 categorías cargan. "Publicar" deja la pantalla en "Editar servicio" con "Servicio publicado. Ya puedes agregarle fotos"; "Guardar cambios" regresa a P-12 con "Cambios guardados". Con campos mal, "Publicar" los marca todos y lleva el foco al primero; el rango de precio se marca en "Hasta". **Al editar, las fotos que ya tenía no salen** (`H-18`); las que agregas se suben y se quitan en el momento. "Redactar con IA" se habilita con título y categoría, pero aún no hace nada | `S3-T08`, `S3-T13` |
| Pulsas "Usar foto" en el recortador | La foto se guarda en Supabase Storage, el avatar la muestra y sigue al reabrir. La anterior se borra de la cubeta. Sin red, "Sin conexión" | — |
| Cambias la contraseña en P-18 | Solo se pide la nueva (mínimo 8). Si repites la actual: "La contraseña nueva es igual a la actual" | — |
| Pulsas "Cerrar sesión" en P-18 y confirmas | Cierra la sesión real: vas a P-02, atrás cierra la aplicación y al reabrir sigues en P-02 | — |
| Pulsas "Cerrar sesión" **sin red** | "Sin conexión. Revisa tu conexión e intenta de nuevo." Sigues en P-18 y la sesión sigue guardada | — |
| Estás en P-02 (iniciar sesión) | La pantalla real: marca, los dos campos y los enlaces a P-03 y P-04 | — |
| Pulsas **Iniciar sesión** o **Crear cuenta** **sin las llaves** en `local.properties` | "Algo salió mal" con "Reintentar". No se cierra la aplicación | Poner `SUPABASE_URL` y `SUPABASE_ANON_KEY` |
| Pulsas **Iniciar sesión** con una cuenta real del proyecto | Entra a P-05 o P-10 según su rol en `public.usuarios`. Probado con las cuentas de `92_usuarios_prueba.sql`: `ana.rls@…` entra a P-05 y `beto.rls@…` a P-10 | — |
| Pulsas **Iniciar sesión** con credenciales malas | "Correo o contraseña incorrectos", sin "Reintentar". Probado contra Supabase | — |
| Pulsas **Iniciar sesión** sin red | "Sin conexión" con "Reintentar". Con la red de vuelta, "Reintentar" entra | — |
| Abres `mx.donchambitas.app://auth#error=…` (enlace vencido) | La aplicación abre en P-02 y no se cierra, en frío y con la aplicación abierta | — |
| Entras a P-03 desde el marcador de P-02 | La pantalla real de registro, con sus cinco campos y el selector de rol | — |
| Confirmas el registro **sin elegir rol** | Te reclama el rol y no hace nada más | — |
| Escribes un correo sin arroba y sales del campo | Sale "Ese correo no se ve bien, revísalo" debajo. Se borra en cuanto vuelves a escribir | — |
| Pulsas el botón de P-02 o P-03 con campos mal | Marca **todos** los que fallan, no avanza y deja el foco en el primero | — |
| Entras en P-02 con una contraseña de un carácter y una cuenta que existe | **Entra.** Al iniciar sesión solo se exige que no esté vacía (5.2); el mínimo de 8 es del registro | — |
| Confirmas el registro **con rol** | Crea la cuenta en Supabase Auth y la ficha en `public.usuarios`, y te manda a P-05 o P-10 | — |
| Te registras con un correo que ya existe | "Correo ya registrado / El correo ya está registrado, inicia sesión", sin "Reintentar" | — |
| Tocas "¿Olvidaste tu contraseña?" en P-02 | P-04 real: explicación, correo, "Enviar enlace" y "Volver a iniciar sesión", con una sola barra | — |
| Pides el enlace con cualquier correo bien escrito | "Revisa tu correo", sin repetir el correo, **exista o no la cuenta** y aunque su correo no pueda recibir | — |
| Pides el enlace sin red | "Sin conexión" con "Reintentar", y el correo sigue escrito | — |
| Esperas el correo en una cuenta que no es del equipo | **No llega.** El SMTP de fábrica de Supabase solo entrega a los miembros del equipo de la organización, y muy pocos por hora | `S6-T13` |
| Abres el enlace del correo | P-18 con la sección de contraseña abierta, con la aplicación abierta o en frío. Atrás cierra la aplicación. Con un correo real se probó el 2026-10-14, con la aplicación abierta. **Necesita `mx.donchambitas.app://auth` en "Redirect URLs"**; sin ella, el correo manda a `localhost:3000` | — |
| Con sesión de cliente, intentas abrir una pantalla de trabajador, o al revés | Regresas al inicio de tu rol. Ya no hay botones "Sin sesión / Cliente / Trabajador": las guardas y las barras leen la sesión real | — |
| Con sesión abierta, intentas abrir P-02, P-03 o P-04 desde el mapa | Regresas al inicio de tu rol | — |

**El alta ya es real** desde `S2-T07`: la cuenta vive en Supabase Auth y en
`public.usuarios`, y sobrevive al reinicio. Por `DEC-25` el registro deja
sesión abierta, con la confirmación por correo desactivada en la consola.

## Tarea en curso

_Ninguna._

| Campo | Valor |
|---|---|
| ID | — |
| Título | — |
| Quién la tomó | — |
| Rama | — |
| Desde | — |

## Última tarea terminada

**`S3-T07` — Validaciones y reglas de negocio de la publicación de
servicios.** 2026-10-19. Rama `feat/S3-T07-validaciones-trabajador`, desde
`main` con `S3-T06` ya integrada (#36), pull request **sin abrir todavía**.
Ticket redactado por el agente al tomar la tarea.

- **`ValidacionesTrabajador.kt`** en el dominio, junto a `ValidacionesAuth.kt`:
  las 8 reglas de §5.2 y las 8 de §5.3 como funciones puras que devuelven
  `FalloTrabajador`, con los topes del esquema en `LimitesTrabajador`. La ui
  las traduce con `mensaje()` a las 15 claves `validacion_*` nuevas, y
  `validacion_telefono_digitos` se reutiliza.
- **La normalización de §1.6** también es dominio: el precio con coma o punto
  se envía como `BigDecimal` de dos decimales, y las habilidades van sin
  repetidas, sin distinguir mayúsculas.
- **P-11 y P-13 validan igual que P-03**: al perder el foco un campo ya tocado;
  todos al guardar, sin llamar al repositorio y con el foco en el primer error.
  El selector de municipio y el bloque "Categoría" reciben el foco aunque no
  abran teclado. La habilidad se valida al agregarla, y el rango de precio se
  marca en "Hasta".
- **Corrección de la bitácora**: el renglón de `S3-T06` había entrado
  duplicado con el #36. Se quitó el sobrante.
- Verificación del proyecto:
  - Compilación exitosa (`./gradlew assembleDebug`).
  - 296 pruebas unitarias (12 en `ValidacionesTrabajadorTest` y 7 más en los
    ViewModels), 0 fallas.
  - 108 instrumentadas, 0 fallas.
  - A mano en el emulador con `beto.rls@…`: "Publicar" vacío marca título,
    categoría y descripción, con el foco en el título; "Desde" 800 y "Hasta"
    300 se marca en "Hasta" y no publica; "Cobre" dos veces da "Ya agregaste
    esa habilidad"; un teléfono de 5 dígitos se marca al salir del campo, y
    guardar sin título lleva el foco al título.

Las anteriores, con su detalle completo, están al final de
`docs/control/BITACORA.md`, en "Detalle de las tareas terminadas".

## Cuentas de prueba en Supabase

Las de `92_usuarios_prueba.sql` (`S1-T03`), la de `S2-T07`
(`s2t07.trabajador@…`) y las dos de `S2-T15` (`cliente.s2t15@donchambitas.test`
y `trabajador.s2t15@donchambitas.test`) siguen en el proyecto. Borrarlas necesita la
`service_role`, que no entra al repositorio:

```sql
delete from auth.users where email like '%@prueba.donchambitas.mx';
```

## Cuatro huecos que esperan al líder

Salen del cruce contra las historias y de revisar el paso 2 del ticket.
**Ninguno se corrigió**: el esquema no se rediseña en `S1-T03`. Están
explicados al final de `MODELO-ER.md`.

- **`H-05` — La búsqueda por texto no ignora los acentos.** HU-17 promete que
  "plomeria" encuentre "plomería". `pg_trgm` acelera el `ilike`, no cambia lo
  que el `ilike` considera igual, y `unaccent` no está instalado; el contrato
  de `buscarTrabajadores` busca justamente con `ilike`. Es el más serio y
  **conviene cerrarlo antes de `S4-T04`**.
- **`H-06` — Nada borra el archivo de Storage cuando desaparece su fila.** La
  cascada se lleva la URL antes de que nadie pueda usarla. Le toca a `S3-T08`
  y `S2-T12` escribir el orden de operaciones.
- **`H-07` — "Postulaciones sin revisar" no existe como dato.** `enviada` es
  "no decidida", no "no vista". Probablemente sea la redacción de HU-13.
- **`H-08` — Uno de los cinco triggers del paso 2 no se puede probar, y eso
  toca un criterio de aceptación de este ticket.** "Postularse a tu propia
  solicitud" exigiría un uuid que fuera cliente y trabajador a la vez, y los
  roles excluyentes lo impiden antes de que el trigger opine. Los otros cuatro
  están demostrados. **Este es el único punto por el que `S1-T03` podría
  devolverse:** o el criterio baja a cuatro triggers, o se anota que al quinto
  lo sostiene el esquema.

## Los seis huecos de S3-T01

Salen de cruzar HU-06, HU-08 a HU-11 y HU-29 a HU-31 contra los repositorios
y el esquema. Están explicados en la sección 10 de
`docs/producto/DISENO-PERFIL-SERVICIOS.md`. **Cerrados dos, `H-19` y `H-21`;
quedan cuatro.**

- **`H-18` — Las fotos de un servicio propio no se pueden leer.** `Servicio`
  no trae fotos y sin el `id` de cada una no se puede llamar a `eliminarFoto`.
  Le toca a `S3-T08`.
- **`H-19` — CERRADO el 2026-10-18 por el líder**: operación nueva,
  `obtenerMisHabilidades()`, que agregó `S3-T03` en la interfaz y en el falso.
  La real es de `S3-T09`.
- **`H-20` — `subirFoto` no informa avance.** El diseño pinta un indicador
  indeterminado; HU-11 dice "veo el progreso".
- **`H-21` — CERRADO el 2026-10-18 por el líder**: `TarjetaServicio` ganó
  `pausado` y ya no pinta el renglón de precio vacío. Lo aplicó `S3-T05`.
- **`H-22` — Al crear un servicio todavía no se le pueden agregar fotos.** El
  diseño pasa P-13 a modo editar al publicar; el líder puede elegir lo
  contrario.
- **`H-23` — No hay forma de saber el tope de IA al abrir la pantalla**, como
  pide HU-31. Le toca a `S3-T12`.

## Siguiente en la cola

**`S3-T08` — Contrato de la API de perfil del trabajador y de servicios**
(prioridad 700, BCJL, sin dependencias). Todavía no tiene ticket: se redacta al
tomarla. Es documentación de `CONTRATOS-API.md`, sin Supabase. Le tocan dos
huecos abiertos: `H-18`, para leer las fotos de un servicio propio con su `id`,
y `H-20`, sobre si `subirFoto` informa avance. También le toca `H-06`: en qué
orden se borran la fila y el archivo de Storage.

## Un hueco nuevo de S3-T05

- **`H-24` — Ninguna tarea de la cola construye P-14 "Mis postulaciones".**
  P-12 y P-18 ya llevan a ella y sigue siendo el andamio de `S1-T12`. Las
  postulaciones son del Sprint 5 (`S5-T02`, `S5-T03`), pero ninguna nombra la
  pantalla del trabajador. **Lo tiene que decidir el líder**: agregar una
  tarea o asignársela a una existente.

## Los dos huecos de S2-T01, ya cerrados

Salían de `S2-T01`, de cruzar HU-01 y HU-04 contra `CONTRATOS-API.md`. Están
explicados al final de `docs/producto/DISENO-AUTENTICACION.md`. **Al 2026-09-22
no queda ninguno abierto.**

- **`H-09` — CERRADO el 2026-09-22 por `DEC-27`.** El enlace de recuperación
  abre la aplicación por *deep link*, canjea el token por sesión y aterriza en
  **P-18**, que ya cambia la contraseña por HU-05. **No se agrega pantalla:
  siguen siendo 19 y `DEC-10` queda intacto.** Al investigarlo se cayó la
  premisa del hallazgo: la "página alojada de Supabase" no existe —Supabase no
  hospeda formulario de contraseña nueva, la pantalla la pone uno— y una web
  propia la descarta `DEC-02`. Lo recoge `S2-T06`, lo implementa `S2-T07` y
  `S2-T11` especifica la llegada a P-18.
- **`H-10` — CERRADO el 2026-09-22 por `DEC-25`.** El registro deja sesión
  abierta: el usuario entra directo a la pantalla de su rol y la sesión vive
  hasta que él la cierre. Dos consecuencias que `S2-T06` y `S2-T07` tienen que
  respetar: la confirmación por correo de Supabase Auth **queda desactivada**,
  porque con ella activa `signUp` no abre sesión; y `registrar` pasa a devolver
  `Sesion` en vez de `Usuario`, así que `CONTRATOS-API.md` y `RepositorioAuth`
  cambian en `S2-T06`.


## Decisiones recientes

**2026-09-27 · Se integra la versión de `S2-T05` a `S2-T07` hecha hoy, no la de
Ricardo5690.** Ricardo5690 implementó las mismas tres tareas entre el 23 y el
24 de septiembre, en `feat/S2-T05-viewmodels-estados-ui-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion` y `feat/S2-T07-autenticacion-supabase`.
Nunca llegaron a `main`, y por eso no aparecían aquí. El líder eligió la
versión de hoy, que se sube con los nombres
`feat/S2-T05-viewmodels-autenticacion`,
`docs/S2-T06-contrato-api-autenticacion-v2` y
`feat/S2-T07-autenticacion-supabase-v2`. Las razones:
- parte del `main` actual, con `S2-T04`;
- la sesión persiste;
- está probada contra Supabase;
- ya aplica `DEC-28` a `DEC-31`.

**Las ramas de Ricardo no se borran.** Conviene revisar con él si algo se
rescata, por ejemplo su prueba del repositorio o la llegada a P-18, que le
sirve a `S2-T11`. Es la segunda vez que una tarea se hace dos veces, después
de `S2-T04` (#10 y #11): una tarea tomada en una rama sin PR no se ve desde
`main`.

**2026-09-27 · Dos decisiones más: `DEC-30` y `DEC-31`.** `DEC-30` cierra
`H-12`: `S2-T08` cifra la sesión que guarda `supabase-kt` y quita
`Sesion.tokenAcceso`. `DEC-31` resuelve el riesgo de P-18: `S2-T12` trae
`RepositorioUsuario` real, `S2-T11` pasa a depender de `S2-T12`, y AGENTS.md
§6 pasa a seis tareas reales. De ahí salió un hallazgo nuevo, `H-13`:
solicitudes, postulaciones y reseñas no tienen tarea real. Está al final de
`CONTRATOS-API.md`.

**2026-09-27 · Dos decisiones nuevas: `DEC-28` y `DEC-29`.** Las tomó el líder
y las registró el agente por instrucción suya. `DEC-28` cierra `H-11`: el correo
duplicado es `TipoError.CORREO_DUPLICADO`, y P-03 deja de pintar mensajes tal
cual. `DEC-29` limita la elección de la siguiente tarea al sprint en curso y
a los anteriores.

**2026-09-16 · La base se volvió a levantar y a verificar.** `01` a `04` sin
error, `90_verificacion.sql` **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**, ya con las pruebas 10, 12 y 13 corriendo con RLS activo. Las tres
pasaron por la razón correcta.

**2026-09-16 · `S1-T03` terminada.** Diagrama, cruce de las 33 historias, las
tres pruebas de `91` corriendo por fin con RLS activo, y el paso 2c cerrado con
la `anon key` contra PostgREST. Salieron cuatro huecos —`H-05` a `H-08`— y
ninguno se corrigió: son del líder. `H-08` toca un criterio de aceptación de
este mismo ticket.

**2026-09-15 · Dos decisiones nuevas: `DEC-23` y `DEC-24`.** Salen de los
hallazgos que dejó `S1-T02`. La primera amplía los filtros de búsqueda de tres
a cinco —se agregan estado y municipio— y con eso cambia `PRODUCTO.md`, el
renglón de P-06 y el título de `S4-T05`. La segunda pone por escrito la regla
de unicidad del hilo de chat que el esquema ya aplicaba, y que HU-24 no
describía para el caso de P-07.

Los otros dos hallazgos no necesitaron número: H-02 era la descripción de P-06,
que había quedado corta frente a HU-18, y H-03 se cerró aceptando que las
notificaciones locales sigan cubiertas por los criterios de HU-22 y HU-26 en
vez de tener historia propia. Los cuatro están explicados al final de
`PANTALLAS.md`.

**2026-09-15 · La base de datos está levantada y verificada.** Reinstalación
desde cero contra el proyecto de Supabase: `00_reinicio.sql` dejó todo en cero
—las once comprobaciones en `OK`, cubetas incluidas— y `01` a `04` corrieron sin
un solo error. `90_verificacion.sql` dio **42 de 42** y `91_prueba_funcional.sql`
**25 de 25**.

Falta lo que ningún script puede hacer solo, y es lo que cierra `S1-T03`:
probar RLS con la `anon key` y dos sesiones reales, mover las pruebas 10, 12 y
13 de `91` al bloque de `set role authenticated` (hoy corren como `postgres` y
pasan siempre), el diagrama ER y el cruce contra las historias de `S1-T01`.

**2026-09-15 · Revisión de `basedatos/` antes de volver a levantar el esquema.**
Se encontraron cinco defectos que `91_prueba_funcional.sql` **no detecta**,
porque corre como `postgres` y se salta RLS: cinco funciones de trigger sin
`security definer` que dejaban sus reglas sin aplicar en silencio, la vista de
búsqueda sin con qué filtrar por categoría, `fn_ia_registrar_llamada`
ejecutable con la `anon key`, permisos de `update` más anchos que el contrato,
y el semillero de categorías sin poder reconciliar los iconos de S1-T08. Los
cinco están corregidos en los archivos; `90_verificacion.sql` pasó de 36 a 42
comprobaciones y las seis nuevas son las que vigilan justo eso.

`00_reinicio.sql` ahora borra también los archivos y las dos cubetas de
Storage: el reinicio es total.

**2026-09-15 · Tres decisiones tomadas: `DEC-19`, `DEC-20` y `DEC-21`.** El
correo del trabajador es visible para cualquier usuario con sesión, el chat lo
abre siempre el cliente, y la atomicidad al aceptar una postulación es regla
del contrato y no de la base. Las tres están explicadas en `MODELO-ER.md`, en
"Lo que las políticas NO impiden, a propósito". `DEC-22` solo pone número a
algo que ya estaba decidido: los roles no se cambian.

**2026-09-14 · PEND-01 resuelto: el backend es Supabase.** Ver `DEC-16` y
`DEC-17`. Las seis tareas que estaban `bloqueada` pasaron a `pendiente` y ya no
queda ninguna tarea bloqueada en toda la cola. El cliente es `supabase-kt`:
Retrofit y OkHttp salieron del stack.

Esto **no** cambia cómo se trabaja hoy. La implementación activa sigue siendo
`FuenteDatosFalsa` hasta que cada tarea real llegue en su turno.

---

## Cómo se llena

Al **tomar** una tarea: llena "Tarea en curso" y pon la tarea en `en curso`
dentro de `docs/tareas/INDICE.md`.

Al **terminarla**: mueve la tarea a "Última tarea terminada", vacía "Tarea en
curso", recalcula el contador del sprint y actualiza "Siguiente en la cola".
La que ocupaba "Última tarea terminada" pasa, tal cual, al principio de
"Detalle de las tareas terminadas" en `BITACORA.md`: aquí solo vive una.

Si dos personas van a trabajar el mismo día, la segunda revisa este archivo
**antes** de pedirle nada al agente. Si dice que hay una tarea en curso, esa
tarea no se toca.
