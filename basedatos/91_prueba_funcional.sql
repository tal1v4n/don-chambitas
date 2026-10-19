-- ============================================================================
--  DON CHAMBITAS
--  91_prueba_funcional.sql  ·  Recorrido completo contra la base real
-- ============================================================================
--  Pegalo COMPLETO en el SQL Editor de Supabase y corre. Devuelve 25
--  renglones. Todo tiene que decir PASA.
--
--  QUE HACE
--    Recorre los 7 pasos de PRODUCTO.md con cuatro usuarios de prueba:
--    registro, perfil, servicio, solicitud, postulacion, aceptacion, chat,
--    cierre, resena y baja de cuenta. Y prueba que lo que DEBE fallar falla.
--
--  SE LIMPIA SOLO. Al final borra los cuatro usuarios de prueba y todo lo que
--  cuelga de ellos. Si una corrida se interrumpe, volver a correrlo limpia lo
--  que haya quedado: el primer DELETE de abajo es justo para eso.
--
--  QUE BORRA, EXACTAMENTE. Supabase va a advertir que el script es
--  destructivo, y tiene razon. Lo unico que borra son filas de auth.users
--  cuyo correo termina en @prueba.donchambitas.mx, mas lo que cuelgue de
--  ellas por CASCADE. Ninguna cuenta real puede caer ahi.
--
--  Supabase tambien va a advertir que crea una tabla sin RLS. Es
--  resultado_prueba, que es TEMPORAL: vive en pg_temp, solo en esta sesion, y
--  PostgREST no la ve nunca. Ahi la respuesta es "Run without RLS".
--
--  Los usuarios se crean directo en auth.users porque la aplicacion todavia
--  no existe. Es la unica razon; en cuanto haya pantalla de registro, esto se
--  prueba desde el telefono.
--
--  QUE CORRE CON RLS ACTIVO
--    El grueso del script corre como postgres, que se SALTA RLS. Las pruebas
--    10, 12, 13 y 21 a 24 no: esas hacen SET ROLE authenticated y ponen
--    request.jwt.claims, porque lo que vigilan solo se rompe cuando RLS esta
--    activo. Son las que detectan que a una funcion de trigger le falte el
--    SECURITY DEFINER, que es una falla silenciosa: sin RLS de por medio, esa
--    misma funcion se comporta bien y la prueba pasa sin demostrar nada.
--
--    Si el usuario que corre el script no puede hacer SET ROLE authenticated,
--    esas pruebas dicen ">>> NO SE PUDO PROBAR" en vez de mentir con un PASA.
-- ============================================================================

delete from auth.users where email like '%@prueba.donchambitas.mx';

-- Acotado a pg_temp a proposito: asi es imposible que este DROP alcance una
-- tabla real de public, aunque alguna vez alguien cree una con este nombre.
drop table if exists pg_temp.resultado_prueba;
create temp table resultado_prueba (orden int, prueba text, resultado text, detalle text);

do $$
declare
    v_cli  uuid := '11111111-1111-1111-1111-111111111111';  -- cliente
    v_tra  uuid := '22222222-2222-2222-2222-222222222222';  -- trabajador
    v_tra2 uuid := '33333333-3333-3333-3333-333333333333';  -- otro trabajador
    v_ter  uuid := '44444444-4444-4444-4444-444444444444';  -- cliente ajeno
    v_sol uuid; v_sol2 uuid; v_pos uuid; v_pos2 uuid; v_srv uuid; v_con uuid; v_con2 uuid;
    v_cat smallint; v_edo smallint; v_mun integer;
    v_n int; v_n2 int; v_txt text; v_uuid uuid; v_ts timestamptz; v_num numeric;
    v_rest int; v_rol_ok boolean; v_uid uuid; v_tabla text;
begin
    -- ---- 0. Se puede hacer SET ROLE authenticated? ------------------------
    -- Se averigua una sola vez, aqui arriba, porque de esto dependen las
    -- pruebas 10, 12, 13 y 21 a 24: todas necesitan que RLS este activo, y
    -- RLS no se aplica al rol postgres.
    begin
        set local role authenticated;
        reset role;
        v_rol_ok := true;
    exception when others then
        v_rol_ok := false;
    end;

    select id into v_cat from public.categorias where orden = 10;  -- Plomería
    select id into v_edo from public.estados where clave = 'PUE';
    select m.id into v_mun from public.municipios m
      join public.estados e on e.id = m.estado_id
     where e.clave = 'PUE' and m.nombre = 'Puebla';

    -- ---- 1. Registro -------------------------------------------------------
    insert into auth.users (id, email, raw_user_meta_data) values
      (v_cli,  'ana@prueba.donchambitas.mx',
       '{"nombre":"Ana","apellidos":"Cliente","telefono":"5550001111","rol":"cliente"}'::jsonb),
      (v_tra,  'beto@prueba.donchambitas.mx',
       '{"nombre":"Beto","apellidos":"Plomero","telefono":"5550002222","rol":"trabajador"}'::jsonb),
      (v_tra2, 'cesar@prueba.donchambitas.mx',
       '{"nombre":"Cesar","apellidos":"Plomero","telefono":"5550003333","rol":"trabajador"}'::jsonb),
      (v_ter,  'dora@prueba.donchambitas.mx',
       '{"nombre":"Dora","apellidos":"Ajena","telefono":"5550004444","rol":"cliente"}'::jsonb);

    select count(*) into v_n from public.usuarios where id in (v_cli, v_tra, v_tra2, v_ter);
    insert into resultado_prueba values (1, 'el trigger crea el perfil al registrarse',
        case when v_n = 4 then 'PASA' else '>>> FALLA' end, v_n || ' de 4 filas en public.usuarios');

    select rol::text into v_txt from public.usuarios where id = v_tra;
    insert into resultado_prueba values (2, 'el rol se lee del metadata del registro',
        case when v_txt = 'trabajador' then 'PASA' else '>>> FALLA' end, 'rol de Beto: ' || v_txt);

    -- ---- 2. Roles excluyentes (S-03) --------------------------------------
    begin
        insert into public.perfiles_trabajador (usuario_id, titulo) values (v_cli, 'Plomero falso');
        insert into resultado_prueba values (3, 'S-03 un cliente NO puede tener perfil de trabajador',
            '>>> FALLA', 'lo permitio');
    exception when others then
        insert into resultado_prueba values (3, 'S-03 un cliente NO puede tener perfil de trabajador',
            'PASA', left(sqlerrm, 70));
    end;

    begin
        insert into public.solicitudes (cliente_id, categoria_id, titulo, descripcion)
        values (v_tra, v_cat, 'Solicitud de un trabajador', 'No deberia poder');
        insert into resultado_prueba values (4, 'S-03 un trabajador NO puede publicar solicitudes',
            '>>> FALLA', 'lo permitio');
    exception when others then
        insert into resultado_prueba values (4, 'S-03 un trabajador NO puede publicar solicitudes',
            'PASA', left(sqlerrm, 70));
    end;

    -- ---- 3. Perfil y servicio ---------------------------------------------
    insert into public.perfiles_trabajador
        (usuario_id, titulo, descripcion, experiencia_anios, telefono_contacto, estado_id, municipio_id)
    values (v_tra, 'Plomero con 10 anios de experiencia', 'Fugas, drenajes e instalaciones',
            10, '5550002222', v_edo, v_mun);
    insert into public.perfiles_trabajador (usuario_id, titulo, estado_id, municipio_id)
    values (v_tra2, 'Plomero y albanil', v_edo, v_mun);

    insert into public.perfil_habilidades (perfil_id, habilidad)
    values (v_tra, 'Deteccion de fugas'), (v_tra, 'Cambio de tuberia');

    insert into public.servicios (perfil_id, categoria_id, titulo, descripcion,
                                  precio_desde, precio_hasta, unidad_precio)
    values (v_tra, v_cat, 'Reparacion de fugas', 'Detecto y reparo fugas el mismo dia',
            300, 800, 'por trabajo')
    returning id into v_srv;

    insert into public.servicio_fotos (servicio_id, url, posicion)
    values (v_srv, 'https://x/1.jpg', 1), (v_srv, 'https://x/2.jpg', 2), (v_srv, 'https://x/3.jpg', 3);

    begin
        insert into public.servicio_fotos (servicio_id, url, posicion)
        values (v_srv, 'https://x/4.jpg', 4);
        insert into resultado_prueba values (5, 'maximo 3 fotos por servicio', '>>> FALLA', 'acepto la cuarta');
    exception when others then
        insert into resultado_prueba values (5, 'maximo 3 fotos por servicio', 'PASA', left(sqlerrm, 70));
    end;

    -- ---- 4. Solicitud y postulaciones -------------------------------------
    insert into public.solicitudes (cliente_id, categoria_id, titulo, descripcion,
                                    presupuesto, estado_id, municipio_id)
    values (v_cli, v_cat, 'Fuga en el bano', 'La regadera gotea desde hace una semana',
            500, v_edo, v_mun)
    returning id into v_sol;

    insert into public.postulaciones (solicitud_id, trabajador_id, mensaje, precio_propuesto)
    values (v_sol, v_tra, 'Puedo ir el jueves', 450) returning id into v_pos;
    insert into public.postulaciones (solicitud_id, trabajador_id, mensaje, precio_propuesto)
    values (v_sol, v_tra2, 'Yo lo hago manana', 400) returning id into v_pos2;

    insert into resultado_prueba values (6, 'dos trabajadores se postulan', 'PASA', '2 postulaciones');

    -- Aceptar sin ser el cliente dueno
    perform set_config('request.jwt.claims',
        json_build_object('sub', v_ter, 'role', 'authenticated')::text, true);
    begin
        perform public.fn_aceptar_postulacion(v_pos);
        insert into resultado_prueba values (7, 'un tercero NO puede aceptar postulaciones ajenas',
            '>>> FALLA', 'lo permitio');
    exception when others then
        insert into resultado_prueba values (7, 'un tercero NO puede aceptar postulaciones ajenas',
            'PASA', left(sqlerrm, 70));
    end;

    -- Aceptar como el cliente dueno
    perform set_config('request.jwt.claims',
        json_build_object('sub', v_cli, 'role', 'authenticated')::text, true);
    perform public.fn_aceptar_postulacion(v_pos);

    select estatus::text, trabajador_id into v_txt, v_uuid from public.solicitudes where id = v_sol;
    insert into resultado_prueba values (8, 'aceptar deja la solicitud asignada al trabajador',
        case when v_txt = 'asignada' and v_uuid = v_tra then 'PASA' else '>>> FALLA' end,
        'estatus=' || v_txt);

    select estatus::text into v_txt from public.postulaciones where id = v_pos2;
    insert into resultado_prueba values (9, 'aceptar rechaza automaticamente las demas',
        case when v_txt = 'rechazada' then 'PASA' else '>>> FALLA' end,
        'la otra quedo en ' || v_txt);

    -- Postularse a una solicitud que ya no esta abierta.
    -- Se usa una solicitud aparte, cancelada, para que lo que rechace sea el
    -- trigger y no el UNIQUE de una postulacion repetida.
    insert into public.solicitudes (cliente_id, categoria_id, titulo, descripcion, estatus)
    values (v_cli, v_cat, 'Solicitud cancelada', 'Ya no la necesito', 'cancelada')
    returning id into v_sol2;

    --
    -- Corre con SET ROLE authenticated, no como postgres. La diferencia no es
    -- cosmetica: fn_validar_postulacion tiene que LEER esa solicitud cancelada
    -- ajena para poder rechazar la postulacion, y con RLS activo solo la ve
    -- por ser SECURITY DEFINER. Como postgres, que se salta RLS, la prueba
    -- pasaba aunque a la funcion le faltara el SECURITY DEFINER.
    --
    -- La politica de INSERT de postulaciones (trabajador_id = auth.uid()) SI
    -- deja pasar este insert, asi que lo que lo rechaza es el trigger y no la
    -- politica. Por eso se exige ademas que el mensaje sea el del trigger: si
    -- alguna vez lo rechazara otra capa, la prueba lo dice en vez de pasar
    -- por la razon equivocada.
    if v_rol_ok then
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_tra2, 'role', 'authenticated')::text, true);
            insert into public.postulaciones (solicitud_id, trabajador_id)
            values (v_sol2, v_tra2);
            reset role;
            insert into resultado_prueba values (10, 'no se puede postular a una solicitud no abierta (RLS activo)',
                '>>> FALLA', 'lo permitio: fn_validar_postulacion no esta viendo la solicitud');
        exception when others then
            reset role;
            insert into resultado_prueba values (10, 'no se puede postular a una solicitud no abierta (RLS activo)',
                case when sqlerrm like '%abierta%' then 'PASA' else '>>> FALLA' end,
                left(sqlerrm, 70));
        end;
    else
        insert into resultado_prueba values (10, 'no se puede postular a una solicitud no abierta (RLS activo)',
            '>>> NO SE PUDO PROBAR', 'este usuario no puede hacer SET ROLE authenticated');
    end if;

    -- Las claims vuelven al cliente dueno: lo que sigue (fn_abrir_conversacion
    -- y fn_cerrar_solicitud) resuelve quien es quien con auth.uid().
    perform set_config('request.jwt.claims',
        json_build_object('sub', v_cli, 'role', 'authenticated')::text, true);

    -- ---- 5. Chat -----------------------------------------------------------
    v_con := public.fn_abrir_conversacion(v_tra, v_sol);
    v_con2 := public.fn_abrir_conversacion(v_tra, v_sol);
    insert into resultado_prueba values (11, 'abrir la conversacion dos veces devuelve la misma',
        case when v_con = v_con2 then 'PASA' else '>>> FALLA' end, 'ids iguales: ' || (v_con = v_con2)::text);

    -- Los dos mensajes se mandan con SET ROLE authenticated, cada uno como su
    -- emisor. Es lo unico que hace util a la prueba 12: conversaciones NO
    -- tiene politica de UPDATE, asi que si fn_tocar_conversacion perdiera el
    -- SECURITY DEFINER, su update afectaria CERO filas sin dar error y
    -- ultimo_mensaje_en se quedaria en nulo. Como postgres eso no se ve,
    -- porque postgres se salta RLS y el update siempre funciona.
    if v_rol_ok then
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_cli, 'role', 'authenticated')::text, true);
            insert into public.mensajes (conversacion_id, emisor_id, contenido)
            values (v_con, v_cli, 'Buenas, sigue disponible?');

            perform set_config('request.jwt.claims',
                json_build_object('sub', v_tra, 'role', 'authenticated')::text, true);
            insert into public.mensajes (conversacion_id, emisor_id, contenido)
            values (v_con, v_tra, 'Claro, paso el jueves a las 10');
            reset role;
            v_txt := '';
        exception when others then
            reset role;
            v_txt := left(sqlerrm, 70);
        end;

        select ultimo_mensaje_en into v_ts from public.conversaciones where id = v_con;
        insert into resultado_prueba values (12, 'el trigger actualiza ultimo_mensaje_en (RLS activo)',
            case when v_txt = '' and v_ts is not null then 'PASA' else '>>> FALLA' end,
            case when v_txt <> '' then 'los mensajes no entraron: ' || v_txt
                 when v_ts is null then 'nulo: fn_tocar_conversacion no esta escribiendo'
                 else v_ts::text end);
    else
        insert into public.mensajes (conversacion_id, emisor_id, contenido)
        values (v_con, v_cli, 'Buenas, sigue disponible?');
        insert into public.mensajes (conversacion_id, emisor_id, contenido)
        values (v_con, v_tra, 'Claro, paso el jueves a las 10');
        insert into resultado_prueba values (12, 'el trigger actualiza ultimo_mensaje_en (RLS activo)',
            '>>> NO SE PUDO PROBAR', 'este usuario no puede hacer SET ROLE authenticated');
    end if;

    -- S-04: un tercero escribiendo en conversacion ajena, tambien con RLS
    -- activo. Aqui hay dos capas que lo rechazan y el orden importa: los
    -- triggers BEFORE corren ANTES de que PostgreSQL evalue el WITH CHECK de
    -- la politica, asi que quien debe rechazarlo es fn_validar_mensaje. Por
    -- eso se exige su mensaje: sin SECURITY DEFINER la funcion no veria la
    -- conversacion, no rechazaria nada, y seria la politica la que salvara la
    -- regla. La prueba pasaria igual y el hueco quedaria tapado.
    if v_rol_ok then
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_ter, 'role', 'authenticated')::text, true);
            insert into public.mensajes (conversacion_id, emisor_id, contenido)
            values (v_con, v_ter, 'Me meto a una conversacion ajena');
            reset role;
            insert into resultado_prueba values (13, 'S-04 nadie escribe en conversacion ajena (RLS activo)',
                '>>> FALLA', 'lo permitio');
        exception when others then
            reset role;
            insert into resultado_prueba values (13, 'S-04 nadie escribe en conversacion ajena (RLS activo)',
                case when sqlerrm like '%no participa%' then 'PASA' else '>>> FALLA' end,
                left(sqlerrm, 70));
        end;
    else
        insert into resultado_prueba values (13, 'S-04 nadie escribe en conversacion ajena (RLS activo)',
            '>>> NO SE PUDO PROBAR', 'este usuario no puede hacer SET ROLE authenticated');
    end if;

    -- Las claims vuelven al cliente dueno: fn_cerrar_solicitud, aqui abajo,
    -- comprueba con auth.uid() que quien cierra sea el dueno de la solicitud.
    perform set_config('request.jwt.claims',
        json_build_object('sub', v_cli, 'role', 'authenticated')::text, true);

    -- ---- 6. Cierre y resena ------------------------------------------------
    begin
        insert into public.resenas (solicitud_id, cliente_id, trabajador_id, calificacion)
        values (v_sol, v_cli, v_tra, 5);
        insert into resultado_prueba values (14, 'no se resena una solicitud sin cerrar',
            '>>> FALLA', 'lo permitio');
    exception when others then
        insert into resultado_prueba values (14, 'no se resena una solicitud sin cerrar',
            'PASA', left(sqlerrm, 70));
    end;

    perform public.fn_cerrar_solicitud(v_sol);
    select estatus::text, cerrada_en into v_txt, v_ts from public.solicitudes where id = v_sol;
    insert into resultado_prueba values (15, 'cerrar sella estatus y cerrada_en',
        case when v_txt = 'cerrada' and v_ts is not null then 'PASA' else '>>> FALLA' end,
        'estatus=' || v_txt);

    begin
        insert into public.resenas (solicitud_id, cliente_id, trabajador_id, calificacion, comentario)
        values (v_sol, v_cli, v_tra2, 5, 'Resena al trabajador equivocado');
        insert into resultado_prueba values (16, 'la resena debe ser del trabajador asignado',
            '>>> FALLA', 'lo permitio');
    exception when others then
        insert into resultado_prueba values (16, 'la resena debe ser del trabajador asignado',
            'PASA', left(sqlerrm, 70));
    end;

    insert into public.resenas (solicitud_id, cliente_id, trabajador_id, calificacion, comentario)
    values (v_sol, v_cli, v_tra, 5, 'Puntual y limpio, lo recomiendo');

    select promedio into v_num from public.vw_trabajador_calificacion where trabajador_id = v_tra;
    insert into resultado_prueba values (17, 'la vista calcula el promedio',
        case when v_num = 5.00 then 'PASA' else '>>> FALLA' end, 'promedio=' || coalesce(v_num::text, 'nulo'));

    select (public.fn_perfil_publico_trabajador(v_tra) -> 'servicios' -> 0 ->> 'titulo') into v_txt;
    insert into resultado_prueba values (18, 'el RPC arma el perfil publico (P-07)',
        case when v_txt = 'Reparacion de fugas' then 'PASA' else '>>> FALLA' end,
        'primer servicio: ' || coalesce(v_txt, 'nulo'));

    select count(*)::int into v_n from public.vw_busqueda_trabajadores where trabajador_id = v_tra;
    insert into resultado_prueba values (19, 'el trabajador aparece en la busqueda (P-06)',
        case when v_n = 1 then 'PASA' else '>>> FALLA' end, v_n || ' renglon(es)');

    -- ---- 7. Tope de IA -----------------------------------------------------
    for i in 1..10 loop
        v_rest := public.fn_ia_registrar_llamada(v_cli, 'redactar_perfil', 120);
    end loop;
    v_rest := public.fn_ia_registrar_llamada(v_cli, 'categorizar', 30);
    insert into resultado_prueba values (20, 'el tope de IA son 10 al dia EN TOTAL (DEC-18)',
        case when v_rest = -1 then 'PASA' else '>>> FALLA' end,
        'la llamada 11 devolvio ' || v_rest);

    -- ---- 8. RLS de verdad -------------------------------------------------
    -- Aqui es donde se cayo la primera corrida: dos politicas se consultaban
    -- entre ellas y PostgreSQL abortaba con "infinite recursion detected in
    -- policy". Estas cuatro pruebas son las que lo vigilan de ahora en
    -- adelante. Si vuelven a fallar, la recursion regreso.
    if not v_rol_ok then
        insert into resultado_prueba values (21, 'RLS: pruebas con rol authenticated',
            '>>> NO SE PUDO PROBAR', 'este usuario no puede hacer SET ROLE authenticated');
    else
        -- 21. El ajeno no ve nada que no sea suyo.
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_ter, 'role', 'authenticated')::text, true);
            select count(*)::int into v_n    from public.mensajes;
            select count(*)::int into v_rest from public.postulaciones;
            select count(*)::int into v_n2   from public.solicitudes;
            reset role;
            insert into resultado_prueba values (21, 'RLS: un ajeno no ve mensajes, postulaciones ni solicitudes',
                case when v_n = 0 and v_rest = 0 and v_n2 = 0 then 'PASA' else '>>> FALLA' end,
                'vio ' || v_n || ' mensajes, ' || v_rest || ' postulaciones, ' || v_n2 || ' solicitudes');
        exception when others then
            reset role;
            insert into resultado_prueba values (21, 'RLS: un ajeno no ve mensajes, postulaciones ni solicitudes',
                '>>> FALLA', left(sqlerrm, 70));
        end;

        -- 22. El cliente ve las postulaciones a su solicitud.
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_cli, 'role', 'authenticated')::text, true);
            select count(*)::int into v_n from public.postulaciones;
            reset role;
            insert into resultado_prueba values (22, 'RLS: el cliente ve las 2 postulaciones a su solicitud',
                case when v_n = 2 then 'PASA' else '>>> FALLA' end, 'vio ' || v_n);
        exception when others then
            reset role;
            insert into resultado_prueba values (22, 'RLS: el cliente ve las 2 postulaciones a su solicitud',
                '>>> FALLA', left(sqlerrm, 70));
        end;

        -- 23. El trabajador ve la suya y NO la de su competidor.
        begin
            set local role authenticated;
            perform set_config('request.jwt.claims',
                json_build_object('sub', v_tra, 'role', 'authenticated')::text, true);
            select count(*)::int into v_n from public.postulaciones;
            reset role;
            insert into resultado_prueba values (23, 'RLS: el trabajador no ve la postulacion de su competidor',
                case when v_n = 1 then 'PASA' else '>>> FALLA' end, 'vio ' || v_n || ' de 2');
        exception when others then
            reset role;
            insert into resultado_prueba values (23, 'RLS: el trabajador no ve la postulacion de su competidor',
                '>>> FALLA', left(sqlerrm, 70));
        end;

        -- 24. Barrido: ninguna tabla revienta con ninguno de los tres roles.
        v_txt := '';
        foreach v_uid in array array[v_cli, v_tra, v_ter] loop
            foreach v_tabla in array array['usuarios', 'perfiles_trabajador',
                    'perfil_habilidades', 'servicios', 'servicio_fotos', 'solicitudes',
                    'postulaciones', 'conversaciones', 'mensajes', 'resenas'] loop
                begin
                    set local role authenticated;
                    perform set_config('request.jwt.claims',
                        json_build_object('sub', v_uid, 'role', 'authenticated')::text, true);
                    execute format('select count(*) from public.%I', v_tabla) into v_n;
                    reset role;
                exception when others then
                    reset role;
                    v_txt := v_txt || v_tabla || ': ' || left(sqlerrm, 40) || ' | ';
                end;
            end loop;
        end loop;
        insert into resultado_prueba values (24, 'RLS: ninguna politica entra en recursion (30 lecturas)',
            case when v_txt = '' then 'PASA' else '>>> FALLA' end,
            case when v_txt = '' then 'las 10 tablas responden con los 3 usuarios' else v_txt end);
    end if;

    -- ---- 9. Baja de cuenta (S-01) ------------------------------------------
    delete from auth.users where id = v_tra;

    select estatus::text, trabajador_id into v_txt, v_uuid from public.solicitudes where id = v_sol;
    insert into resultado_prueba values (25, 'S-01 baja de trabajador con historial',
        case when v_txt = 'cerrada' and v_uuid is null then 'PASA' else '>>> FALLA' end,
        'la solicitud quedo ' || coalesce(v_txt, 'borrada') ||
        ', trabajador_id ' || coalesce(v_uuid::text, 'nulo'));

    -- ---- Limpieza ----------------------------------------------------------
    delete from auth.users where email like '%@prueba.donchambitas.mx';
end
$$;

select orden, prueba, resultado, detalle
  from resultado_prueba
 order by orden;
