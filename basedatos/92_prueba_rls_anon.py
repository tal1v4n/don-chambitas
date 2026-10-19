#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
==============================================================================
 DON CHAMBITAS
 92_prueba_rls_anon.py  ·  Paso 2c de S1-T03: RLS con la anon key, de verdad
==============================================================================
 QUE PRUEBA, Y POR QUE NO LO CUBRE 91

   91_prueba_funcional.sql comprueba RLS con "set role authenticated" dentro
   de una transaccion. Es buena aproximacion, pero NO pasa por el JWT, ni por
   el rol anon sin sesion, ni por PostgREST. Y ahi es donde vive el riesgo
   real: la anon key va dentro del APK y cualquiera la saca.

   Este guion abre sesiones de verdad contra la API y lee como las leeria
   alguien con la llave en la mano.

 COMO SE CORRE

   1. Pega basedatos/92_usuarios_prueba.sql en el SQL Editor y correlo. Crea
      las cuatro cuentas.
   2. cp .env.ejemplo .env   # y pon SUPABASE_URL y SUPABASE_ANON_KEY
   3. python3 basedatos/92_prueba_rls_anon.py

   Sin dependencias: solo la biblioteca estandar.

 POR QUE LAS CUENTAS SE CREAN POR SQL Y NO AQUI

   Porque por la API no se puede: el alta las rechaza con
   "email_address_invalid". Supabase valida que el dominio del correo exista,
   y @prueba.donchambitas.mx es ficticio. El dominio se eligio cuando solo se
   usaba desde SQL, en 91, que inserta directo en auth.users.

   No debilita la prueba. Lo unico que se saltan esas cuentas es el formulario
   de registro; aqui se inicia sesion de verdad y cada lectura va con un JWT
   real contra PostgREST, que es lo que el paso 2c tiene que demostrar.

 QUE ESCRIBE, EXACTAMENTE

   A las cuatro cuentas les crea perfil, solicitud, postulaciones y una
   conversacion. NO las borra al terminar: darse de baja necesita la
   service_role, que no entra al repositorio. Al final imprime el DELETE que
   hay que pegar en el SQL Editor.
==============================================================================
"""
import json
import os
import sys
import urllib.error
import urllib.request

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOMINIO = "@prueba.donchambitas.mx"
CLAVE = "prueba-S1T03-no-usar-en-produccion"

CUENTAS = {
    "ana":   ("ana.rls",   "Ana",   "Cliente",     "cliente"),
    "beto":  ("beto.rls",  "Beto",  "Plomero",     "trabajador"),
    "cesar": ("cesar.rls", "Cesar", "Competidor",  "trabajador"),
    "dora":  ("dora.rls",  "Dora",  "Ajena",       "cliente"),
}


def cargar_env():
    ruta = os.path.join(RAIZ, ".env")
    if not os.path.exists(ruta):
        salir("No existe .env. Copialo de la plantilla:\n\n    cp .env.ejemplo .env\n")
    valores = {}
    with open(ruta, encoding="utf-8") as f:
        for linea in f:
            linea = linea.strip()
            if not linea or linea.startswith("#") or "=" not in linea:
                continue
            k, _, v = linea.partition("=")
            valores[k.strip()] = v.strip().strip('"').strip("'")
    url = valores.get("SUPABASE_URL", "").rstrip("/")
    key = valores.get("SUPABASE_ANON_KEY", "")
    if not url or "xxxxx" in url:
        salir("SUPABASE_URL sigue con el valor de la plantilla en .env")
    if not key or key.startswith("pon-aqui"):
        salir("SUPABASE_ANON_KEY sigue con el valor de la plantilla en .env")
    if "service_role" in key:
        salir("Eso parece una service_role key. Ese guion se corre con la ANON key,\n"
              "que es justo lo que hay que probar. La service_role se salta todas\n"
              "las politicas y no demostraria nada.")
    return url, key


def salir(mensaje):
    print("\n" + mensaje + "\n", file=sys.stderr)
    sys.exit(1)


class Api:
    def __init__(self, url, anon):
        self.url, self.anon = url, anon

    def pide(self, metodo, ruta, token=None, cuerpo=None, cabeceras=None):
        """Devuelve (codigo, objeto). Nunca lanza por un 4xx: el codigo es dato."""
        datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
        req = urllib.request.Request(self.url + ruta, data=datos, method=metodo)
        req.add_header("apikey", self.anon)
        req.add_header("Authorization", "Bearer " + (token or self.anon))
        req.add_header("Content-Type", "application/json")
        for k, v in (cabeceras or {}).items():
            req.add_header(k, v)
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                crudo = r.read().decode() or "null"
                return r.status, json.loads(crudo)
        except urllib.error.HTTPError as e:
            crudo = e.read().decode() or "null"
            try:
                return e.code, json.loads(crudo)
            except json.JSONDecodeError:
                return e.code, {"message": crudo[:200]}
        except urllib.error.URLError as e:
            salir("No se pudo llegar a %s\n%s\nRevisa SUPABASE_URL en .env." % (self.url, e.reason))

    def entra(self, mote):
        usuario = CUENTAS[mote][0]
        correo = usuario + DOMINIO
        cod, res = self.pide("POST", "/auth/v1/token?grant_type=password",
                             cuerpo={"email": correo, "password": CLAVE})
        token = (res or {}).get("access_token")
        if not token:
            salir("No se pudo iniciar sesion como %s (HTTP %s).\n%s\n\n"
                  "Casi siempre es que falta el paso 1: pega\n"
                  "basedatos/92_usuarios_prueba.sql en el SQL Editor y correlo.\n"
                  "Ese archivo crea las cuatro cuentas, ya confirmadas y con\n"
                  "contrasena. Por la API no se pueden crear: Supabase rechaza el\n"
                  "dominio @prueba.donchambitas.mx porque no existe."
                  % (correo, cod, json.dumps(res)[:300]))
        return token, (res.get("user") or {}).get("id")


def main():
    url, anon = cargar_env()
    api = Api(url, anon)
    print("Proyecto: %s\n" % url)

    # ---- preparacion ------------------------------------------------------
    print("Iniciando sesion con las cuatro cuentas de prueba...")
    tok, uid = {}, {}
    for mote in CUENTAS:
        tok[mote], uid[mote] = api.entra(mote)
        print("  %-6s %s" % (mote, uid[mote]))

    cod, cats = api.pide("GET", "/rest/v1/categorias?select=id&orden=eq.10",
                         token=tok["ana"])
    if cod != 200 or not cats:
        salir("No se pudo leer la categoria de orden 10, Plomeria (HTTP %s). Corrio 04_datos_semilla.sql?" % cod)
    categoria = cats[0]["id"]

    print("Creando perfiles, solicitud, postulaciones y conversacion...")
    for mote in ("beto", "cesar"):
        cod, res = api.pide("POST", "/rest/v1/perfiles_trabajador", token=tok[mote],
                            cuerpo={"usuario_id": uid[mote], "titulo": "Plomero de prueba"})
        if cod >= 300:
            salir("No se pudo crear el perfil de %s (HTTP %s): %s\n\n"
                  "Sin perfil, la prueba 2 daria FALLA por una razon que no es RLS."
                  % (mote, cod, json.dumps(res)[:300]))

    cod, sol = api.pide("POST", "/rest/v1/solicitudes", token=tok["ana"],
                        cuerpo={"cliente_id": uid["ana"], "categoria_id": categoria,
                                "titulo": "Fuga de prueba", "descripcion": "Para el paso 2c"},
                        cabeceras={"Prefer": "return=representation"})
    if cod >= 300 or not sol:
        salir("No se pudo crear la solicitud (HTTP %s): %s" % (cod, json.dumps(sol)[:300]))
    solicitud = sol[0]["id"]

    for mote in ("beto", "cesar"):
        cod, res = api.pide("POST", "/rest/v1/postulaciones", token=tok[mote],
                            cuerpo={"solicitud_id": solicitud, "trabajador_id": uid[mote],
                                    "mensaje": "Yo puedo", "precio_propuesto": 400})
        if cod >= 300:
            salir("No se pudo postular %s (HTTP %s): %s\n\n"
                  "Sin las dos postulaciones, las pruebas 3 y 4 no demuestran nada."
                  % (mote, cod, json.dumps(res)[:300]))

    cod, conv = api.pide("POST", "/rest/v1/rpc/fn_abrir_conversacion", token=tok["ana"],
                         cuerpo={"p_trabajador_id": uid["beto"], "p_solicitud_id": solicitud})
    if cod >= 300 or not conv:
        salir("No se pudo abrir la conversacion (HTTP %s): %s" % (cod, json.dumps(conv)[:300]))
    cod, res = api.pide("POST", "/rest/v1/mensajes", token=tok["ana"],
                        cuerpo={"conversacion_id": conv, "emisor_id": uid["ana"],
                                "contenido": "Mensaje privado entre Ana y Beto"})
    if cod >= 300:
        salir("No se pudo mandar el mensaje (HTTP %s): %s\n\n"
              "Sin mensaje, la prueba 6 no demuestra nada." % (cod, json.dumps(res)[:300]))

    # ---- las comprobaciones ----------------------------------------------
    # Cada una dice cuantas filas vio y con que codigo. El ticket pide que lo
    # ajeno vuelva VACIO y no con error: un 200 con cero filas, no un 403.
    filas = []

    def comprueba(orden, prueba, quien, ruta, esperadas, exige_200=True):
        token = tok[quien] if quien else None
        cod, res = api.pide("GET", ruta, token=token)
        vistas = len(res) if isinstance(res, list) else -1
        if vistas == esperadas and (cod == 200 or not exige_200):
            estado = "PASA"
        else:
            estado = ">>> FALLA"
        if vistas == -1:
            detalle = "HTTP %s · %s" % (cod, json.dumps(res)[:60])
        else:
            detalle = "HTTP %s · vio %d fila(s), esperaba %d" % (cod, vistas, esperadas)
        filas.append((orden, prueba, estado, detalle))
        return cod, res

    comprueba(1, "un ajeno NO ve la ficha de otro cliente", "dora",
              "/rest/v1/usuarios?select=id&id=eq." + uid["ana"], 0)
    comprueba(2, "pero SI ve la ficha de un trabajador (DEC-19)", "dora",
              "/rest/v1/usuarios?select=id&id=eq." + uid["beto"], 1)
    comprueba(3, "el trabajador ve su postulacion y NO la del competidor", "beto",
              "/rest/v1/postulaciones?select=id", 1)
    comprueba(4, "un ajeno NO ve ninguna postulacion", "dora",
              "/rest/v1/postulaciones?select=id", 0)
    comprueba(5, "un ajeno NO ve la conversacion", "dora",
              "/rest/v1/conversaciones?select=id", 0)
    comprueba(6, "un ajeno NO ve los mensajes", "dora",
              "/rest/v1/mensajes?select=id", 0)
    comprueba(7, "el cliente SI ve su propia conversacion", "ana",
              "/rest/v1/conversaciones?select=id", 1)

    # ia_cache: el REVOKE hace que postgrest conteste 401/403 en vez de una
    # lista vacia. Es MAS fuerte que lo que pide el ticket, no menos, asi que
    # se acepta cualquiera de las dos y se dice cual fue.
    cod, res = api.pide("GET", "/rest/v1/ia_cache?select=funcion", token=tok["dora"])
    vacio = isinstance(res, list) and len(res) == 0
    negado = cod in (401, 403) or (isinstance(res, dict) and "permission" in json.dumps(res).lower())
    filas.append((8, "ia_cache no suelta nada con sesion", "PASA" if (vacio or negado) else ">>> FALLA",
                  "HTTP %s · %s" % (cod, "permiso denegado" if negado else
                                    ("vacio" if vacio else json.dumps(res)[:60]))))

    # Sin sesion: solo la anon key, que es como llega alguien con el APK.
    cod, res = api.pide("GET", "/rest/v1/usuarios?select=id")
    vacio = isinstance(res, list) and len(res) == 0
    negado = cod in (401, 403)
    filas.append((9, "sin sesion, con la anon key sola, usuarios no suelta nada",
                  "PASA" if (vacio or negado) else ">>> FALLA",
                  "HTTP %s · %s" % (cod, "permiso denegado" if negado else
                                    ("vacio" if vacio else "vio %d fila(s)" %
                                     (len(res) if isinstance(res, list) else -1)))))

    cod, res = api.pide("GET", "/rest/v1/categorias?select=id")
    ok = cod == 200 and isinstance(res, list) and len(res) == 16
    filas.append((10, "sin sesion, los catalogos SI se leen (P-05 los pinta)",
                  "PASA" if ok else ">>> FALLA",
                  "HTTP %s · %s" % (cod, "%d categorias" % len(res) if isinstance(res, list) else res)))

    # ---- reporte ----------------------------------------------------------
    print("\n%-4s %-58s %-10s %s" % ("#", "PRUEBA", "RESULTADO", "DETALLE"))
    print("-" * 120)
    for orden, prueba, estado, detalle in filas:
        print("%-4d %-58s %-10s %s" % (orden, prueba, estado, detalle))
    print("-" * 120)

    fallaron = [f for f in filas if f[2] != "PASA"]
    print("\n%d de %d en PASA.\n" % (len(filas) - len(fallaron), len(filas)))

    print("LIMPIEZA. Estas cuatro cuentas siguen en el proyecto: borrarlas necesita")
    print("la service_role, que no entra al repositorio. Pega esto en el SQL Editor:\n")
    print("    delete from auth.users where email like '%" + DOMINIO + "';\n")

    sys.exit(1 if fallaron else 0)


if __name__ == "__main__":
    main()
