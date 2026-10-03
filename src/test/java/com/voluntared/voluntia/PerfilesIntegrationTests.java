package com.voluntared.voluntia;

import com.voluntared.voluntia.dto.*;
import com.voluntared.voluntia.service.*;
import com.voluntared.voluntia.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest @AutoConfigureMockMvc
class PerfilesIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired CuentaService cuentas;
    @Autowired CampaniaService campanias;
    @Autowired InscripcionService inscripciones;
    @Autowired UsuarioRepository usuarios;
    @Autowired VoluntarioRepository voluntarios;
    @Autowired OrganizacionRepository organizaciones;
    @Autowired InscripcionRepository inscripcionRepo;
    @Autowired CampaniaRepository campaniaRepo;

    void contexto(String nombre,String rol){
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(nombre,"",List.of(new SimpleGrantedAuthority("ROLE_"+rol))));
    }
    CuentaDTO cuenta(boolean org){
        CuentaDTO d=new CuentaDTO();d.setNombre(org?"Organización de prueba":"Voluntario de prueba");d.setEmail(UUID.randomUUID()+"@example.org");
        d.setContrasena("prueba123");d.setUbicacion("Lima");d.setEdad(23);d.setHabilidades("Reciclaje");
        if(org){d.setRuc("20123456789");d.setInfoContacto("Contacto de prueba");}return d;
    }
    CampaniaDTO campania(Integer organizacionId,int cupos){
        CampaniaDTO d=new CampaniaDTO();d.setTitulo("Iniciativa "+UUID.randomUUID());d.setDescripcion("Plantaremos árboles para recuperar los espacios públicos.");
        d.setUbicacion("Lima");d.setFecha(LocalDateTime.now().plusDays(3));d.setCupos(cupos);d.setOrganizacionId(organizacionId);return d;
    }
    MockHttpServletRequestBuilder perfil(CuentaDTO d){
        return post("/mi-cuenta/guardar").with(csrf()).param("nombre",d.getNombre()).param("email",d.getEmail())
            .param("ubicacion",d.getUbicacion()).param("edad","23").param("habilidades","Trabajo en equipo")
            .param("ruc","20123456789").param("infoContacto","Contacto actualizado");
    }
    MockHttpSession login(String email,String password) throws Exception {
        return (MockHttpSession)mvc.perform(post("/login").with(csrf()).param("username",email).param("password",password))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/campanias")).andReturn().getRequest().getSession(false);
    }

    @Test @Transactional
    void crearCuentaDeAmbosPerfilesEIngresarConCorreo() throws Exception {
        for(String rol:List.of("VOLUNTARIO","ORGANIZACION")){
            String email=UUID.randomUUID()+"@example.org";
            mvc.perform(get("/registro").param("perfil",rol)).andExpect(status().isOk()).andExpect(content().string(containsString("Crear cuenta")));
            mvc.perform(post("/registro").with(csrf()).param("perfil",rol).param("id","99999").param("rol","ADMIN")
                .param("nombre","Cuenta nueva").param("email",email).param("contrasena","prueba123").param("ubicacion","Lima")
                .param("edad","23").param("ruc","20123456789").param("infoContacto","Contacto"))
                .andExpect(redirectedUrl("/login"));
            var u=usuarios.findByEmailIgnoreCase(email).orElseThrow();assertEquals(rol,u.getRol());assertTrue(u.getContrasena().startsWith("$2"));
            MockHttpSession session=login(email.toUpperCase(Locale.ROOT),"prueba123");
            mvc.perform(get("/mi-cuenta").session(session)).andExpect(status().isOk()).andExpect(content().string(containsString(email)));
            mvc.perform(get(rol.equals("ORGANIZACION")?"/mis-voluntariados":"/mis-participaciones").session(session)).andExpect(status().isOk());
            mvc.perform(get("/voluntarios").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/organizaciones").session(session)).andExpect(status().isForbidden());
            mvc.perform(post("/registro").with(csrf()).param("perfil",rol).param("nombre","Duplicado").param("email",email)
                .param("contrasena","prueba123").param("ubicacion","Lima").param("edad","23").param("ruc","20123456789").param("infoContacto","Contacto"))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("cuenta"));
        }
        mvc.perform(post("/registro").with(csrf()).param("perfil","ADMIN").param("nombre","Intruso").param("email","intruso@example.org")
            .param("contrasena","prueba123").param("ubicacion","Lima")).andExpect(status().isOk()).andExpect(model().attributeHasErrors("cuenta"));
        mvc.perform(post("/registro").param("perfil","VOLUNTARIO")).andExpect(status().isForbidden());
    }

    @Test @Transactional
    void miCuentaSoloModificaLaCuentaPropiaSinCambiarSuRol() throws Exception {
        for(boolean org:List.of(false,true)){
            CuentaDTO d=cuenta(org),ajena=cuenta(org);
            Integer id=cuentas.registrar(d,org),otroId=cuentas.registrar(ajena,org);
            Integer uid=usuarios.findByEmailIgnoreCase(d.getEmail()).orElseThrow().getId();
            String hash=usuarios.findById(uid).orElseThrow().getContrasena();
            MockHttpSession session=login(d.getEmail(),"prueba123");d.setNombre("Mi nombre actualizado");
            mvc.perform(perfil(d).session(session).param("id",otroId.toString()).param("rol","ADMIN"))
                .andExpect(redirectedUrl("/mi-cuenta"));
            var actualizado=usuarios.findById(uid).orElseThrow();assertEquals("Mi nombre actualizado",actualizado.getNombre());
            assertEquals(org?"ORGANIZACION":"VOLUNTARIO",actualizado.getRol());assertEquals(hash,actualizado.getContrasena());
            assertEquals(ajena.getNombre(),usuarios.findByEmailIgnoreCase(ajena.getEmail()).orElseThrow().getNombre());
            mvc.perform(get("/mi-cuenta").session(session)).andExpect(status().isOk());
            String propio=d.getEmail();d.setEmail(ajena.getEmail());
            mvc.perform(perfil(d).session(session)).andExpect(status().isOk()).andExpect(model().attributeHasErrors("cuenta"));
            d.setEmail(propio);
            mvc.perform(get("/"+(org?"organizaciones":"voluntarios")+"/editar/"+otroId).session(session)).andExpect(status().isForbidden());
            mvc.perform(delete("/api/"+(org?"organizaciones":"voluntarios")+"/"+id).session(session).with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Test @Transactional
    void cambiarCorreoOContrasenaCierraLaSesionYAdmiteLasNuevasCredenciales() throws Exception {
        for(boolean org:List.of(false,true)){
            CuentaDTO d=cuenta(org);cuentas.registrar(d,org);
            Integer uid=usuarios.findByEmailIgnoreCase(d.getEmail()).orElseThrow().getId();
            MockHttpSession session=login(d.getEmail(),"prueba123");
            String oldEmail=d.getEmail();d.setEmail(UUID.randomUUID()+"@example.org");
            mvc.perform(perfil(d).session(session)).andExpect(redirectedUrl("/login?actualizada"));assertTrue(session.isInvalid());
            assertEquals(d.getEmail(),usuarios.findById(uid).orElseThrow().getEmail());
            mvc.perform(post("/login").with(csrf()).param("username",oldEmail).param("password","prueba123")).andExpect(redirectedUrl("/login?error"));
            session=login(d.getEmail(),"prueba123");
            mvc.perform(perfil(d).session(session).param("contrasena","nueva123"))
                .andExpect(redirectedUrl("/login?actualizada"));assertTrue(session.isInvalid());
            mvc.perform(post("/login").with(csrf()).param("username",d.getEmail()).param("password","prueba123")).andExpect(redirectedUrl("/login?error"));
            session=login(d.getEmail(),"nueva123");
            mvc.perform(get("/mi-cuenta").session(session)).andExpect(status().isOk());
        }
    }

    @Test @Transactional @WithMockUser(roles="ADMIN")
    void organizacionPublicaDescripcionYGestionaUnicamenteSusCampanias() throws Exception {
        CuentaDTO o=cuenta(true),otra=cuenta(true),v=cuenta(false);
        Integer oid=cuentas.guardar(o,true),otroId=cuentas.guardar(otra,true);cuentas.guardar(v,false);
        String titulo="Publicación propia "+UUID.randomUUID();
        mvc.perform(post("/campanias/guardar").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())
            .param("titulo",titulo).param("descripcion","Recogeremos residuos de la playa.").param("ubicacion","Lima")
            .param("fecha",LocalDateTime.now().plusDays(3).withSecond(0).withNano(0).toString()).param("cupos","2").param("organizacionId",otroId.toString()))
            .andExpect(redirectedUrl("/mis-voluntariados"));
        Integer cid=campanias.listar(titulo,null).get(0).getId();assertEquals(oid,campanias.buscar(cid).getOrganizacion().getId());
        assertEquals("Recogeremos residuos de la playa.",campanias.buscar(cid).getDescripcion());
        mvc.perform(get("/campanias/nuevo").with(user(o.getEmail()).roles("ORGANIZACION"))).andExpect(status().isOk());
        mvc.perform(get("/campanias/editar/"+cid).with(user(o.getEmail()).roles("ORGANIZACION"))).andExpect(status().isOk());
        mvc.perform(get("/mis-voluntariados").with(user(o.getEmail()).roles("ORGANIZACION"))).andExpect(content().string(containsString(titulo)));
        mvc.perform(get("/mis-voluntariados").with(user(otra.getEmail()).roles("ORGANIZACION"))).andExpect(content().string(not(containsString(titulo))));
        mvc.perform(get("/campanias/editar/"+cid).with(user(otra.getEmail()).roles("ORGANIZACION"))).andExpect(status().isForbidden());
        mvc.perform(post("/campanias/eliminar/"+cid).with(user(otra.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isForbidden());
        String json="{\"titulo\":\"Edición API\",\"descripcion\":\"Cuidamos la comunidad\",\"ubicacion\":\"Lima\",\"fecha\":\"2030-01-01T10:00:00\",\"cupos\":2}";
        mvc.perform(put("/api/campanias/"+cid).with(user(otra.getEmail()).roles("ORGANIZACION")).with(csrf()).contentType("application/json").content(json)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/campanias/"+cid).with(user(otra.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/campanias").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf()).contentType("application/json").content(json)).andExpect(status().isForbidden());
        mvc.perform(put("/api/campanias/"+cid).with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf()).contentType("application/json").content(json)).andExpect(status().isOk()).andExpect(jsonPath("$.organizacionId").value(oid));
        mvc.perform(post("/api/campanias").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf()).contentType("application/json").content(json)).andExpect(status().isCreated());
        mvc.perform(delete("/api/campanias/"+cid).with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/campanias/"+cid).with(anonymous())).andExpect(status().isNotFound());
    }

    @Test @Transactional @WithMockUser(roles="ADMIN")
    void voluntarioSeUneSaleYLaOrganizacionRetiraSinEliminarLaCuenta() throws Exception {
        CuentaDTO o=cuenta(true),otra=cuenta(true),v=cuenta(false),otro=cuenta(false);
        Integer oid=cuentas.guardar(o,true);cuentas.guardar(otra,true);Integer vid=cuentas.guardar(v,false),otroVid=cuentas.guardar(otro,false);
        Integer cid=campanias.guardar(campania(oid,1)),otroCid=campanias.guardar(campania(oid,2));
        mvc.perform(get("/campanias/"+cid).with(anonymous())).andExpect(status().isOk()).andExpect(content().string(not(containsString("Participantes</h2>"))));
        mvc.perform(post("/campanias/"+cid+"/unirme").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf()).param("voluntarioId",otroVid.toString())).andExpect(status().is3xxRedirection());
        assertTrue(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(vid,cid));assertFalse(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(otroVid,cid));
        mvc.perform(get("/campanias/"+cid).with(user(v.getEmail()).roles("VOLUNTARIO"))).andExpect(content().string(containsString("Salir del voluntariado"))).andExpect(content().string(not(containsString("Participantes</h2>"))));
        mvc.perform(get("/mis-participaciones").with(user(v.getEmail()).roles("VOLUNTARIO"))).andExpect(status().isOk());
        mvc.perform(get("/campanias/"+cid).with(user(o.getEmail()).roles("ORGANIZACION"))).andExpect(content().string(containsString("Retirar participante")));
        mvc.perform(get("/campanias/"+cid).with(user(otra.getEmail()).roles("ORGANIZACION"))).andExpect(content().string(not(containsString("Retirar participante"))));
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isBadRequest());
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(user(otro.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isBadRequest());
        mvc.perform(post("/api/campanias/"+otroCid+"/mi-inscripcion").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isCreated());
        mvc.perform(delete("/api/campanias/"+cid+"/participantes/"+vid).with(user(otra.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/campanias/"+cid+"/participantes/"+vid+"/retirar").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().is3xxRedirection());
        assertTrue(voluntarios.existsById(vid));assertTrue(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(vid,otroCid));assertEquals(0,inscripcionRepo.countByCampaniaId(cid));
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isCreated());
        mvc.perform(post("/campanias/"+cid+"/salir").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(user(otro.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isCreated());
        mvc.perform(delete("/api/campanias/"+cid+"/mi-inscripcion").with(user(otro.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/campanias/"+otroCid+"/participantes/"+vid).with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(post("/campanias/"+cid+"/inscribir").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf()).param("voluntarioId",vid.toString())).andExpect(status().isForbidden());
        mvc.perform(get("/api/voluntarios/"+otroVid).with(user(v.getEmail()).roles("VOLUNTARIO"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(anonymous())).andExpect(status().isForbidden());
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(anonymous()).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test @Transactional @WithMockUser(roles="ADMIN")
    void asistenciaPuedeRetirarsePorGestionYCampaniaIniciadaNoAdmiteInscripciones() throws Exception {
        CuentaDTO o=cuenta(true),v=cuenta(false);Integer oid=cuentas.guardar(o,true),vid=cuentas.guardar(v,false),cid=campanias.guardar(campania(oid,2));
        inscripciones.inscribir(cid,vid);Integer iid=inscripcionRepo.findByVoluntarioIdAndCampaniaId(vid,cid).orElseThrow().getId();
        mvc.perform(post("/campanias/inscripciones/"+iid+"/asistencia").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(delete("/api/campanias/"+cid+"/mi-inscripcion").with(user(v.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isBadRequest());
        mvc.perform(get("/mis-participaciones").with(user(v.getEmail()).roles("VOLUNTARIO"))).andExpect(content().string(containsString("Asistencia registrada")));
        contexto("admin","ADMIN");
        CuentaDTO otraOrganizacion=cuenta(true);cuentas.guardar(otraOrganizacion,true);
        Integer otraCampania=campanias.guardar(campania(oid,2));inscripciones.inscribir(otraCampania,vid);
        mvc.perform(delete("/api/campanias/"+cid+"/participantes/"+vid).with(user(otraOrganizacion.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/campanias/"+cid).with(user(o.getEmail()).roles("ORGANIZACION"))).andExpect(status().isOk())
            .andExpect(content().string(containsString("Retirar participante"))).andExpect(content().string(containsString("También se eliminará su asistencia")));
        mvc.perform(post("/campanias/"+cid+"/participantes/"+vid+"/retirar").with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(vid,cid));
        assertEquals(0,inscripcionRepo.countByCampaniaId(cid));
        assertTrue(voluntarios.existsById(vid));
        assertTrue(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(vid,otraCampania));
        contexto("admin","ADMIN");inscripciones.inscribir(cid,vid);
        iid=inscripcionRepo.findByVoluntarioIdAndCampaniaId(vid,cid).orElseThrow().getId();inscripciones.asistencia(iid);
        mvc.perform(delete("/api/campanias/"+cid+"/participantes/"+vid).with(user(o.getEmail()).roles("ORGANIZACION")).with(csrf())).andExpect(status().isNoContent());
        assertFalse(inscripcionRepo.existsByVoluntarioIdAndCampaniaId(vid,cid));
        campanias.buscar(cid).setFecha(LocalDateTime.now().minusDays(1));campaniaRepo.flush();
        contexto("admin","ADMIN");CuentaDTO otro=cuenta(false);cuentas.guardar(otro,false);
        mvc.perform(post("/api/campanias/"+cid+"/mi-inscripcion").with(user(otro.getEmail()).roles("VOLUNTARIO")).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles="ADMIN")
    void solicitudesSimultaneasNoSobrepasanCuposNiDuplicanInscripciones() throws Exception {
        CuentaDTO o=cuenta(true),v1=cuenta(false),v2=cuenta(false);
        Integer oid=cuentas.guardar(o,true),vid1=cuentas.guardar(v1,false),vid2=cuentas.guardar(v2,false);
        List<Integer> ids=new ArrayList<>();ExecutorService pool=Executors.newFixedThreadPool(2);
        try{
            for(boolean duplicado:List.of(false,true)){
                Integer cid=campanias.guardar(campania(oid,duplicado?2:1));ids.add(cid);
                CountDownLatch preparados=new CountDownLatch(2),inicio=new CountDownLatch(1);List<Future<Boolean>> resultados=new ArrayList<>();
                for(String email:List.of(v1.getEmail(),duplicado?v1.getEmail():v2.getEmail()))resultados.add(pool.submit(()->{
                    contexto(email,"VOLUNTARIO");preparados.countDown();inicio.await(10,TimeUnit.SECONDS);
                    try{inscripciones.unirme(cid);return true;}catch(IllegalArgumentException e){return false;}finally{SecurityContextHolder.clearContext();}
                }));
                assertTrue(preparados.await(10,TimeUnit.SECONDS));inicio.countDown();int exitos=0;
                for(var resultado:resultados)if(resultado.get(20,TimeUnit.SECONDS))exitos++;
                assertEquals(1,exitos);assertEquals(1,inscripcionRepo.countByCampaniaId(cid));
            }
        }finally{
            pool.shutdownNow();contexto("admin","ADMIN");for(Integer id:ids)campanias.eliminar(id);
            cuentas.eliminar(vid1,false);cuentas.eliminar(vid2,false);cuentas.eliminar(oid,true);
        }
    }
}
