package com.voluntared.voluntia.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.voluntared.voluntia.dto.CuentaDTO;
import com.voluntared.voluntia.entity.*;
import com.voluntared.voluntia.repository.*;
import java.util.*;
@Service @Transactional
public class CuentaService {
 private final UsuarioRepository usuarios; private final VoluntarioRepository voluntarios;
 private final OrganizacionRepository organizaciones; private final CampaniaRepository campanias;
 private final PasswordEncoder encoder;
 private final AccesoService acceso;
 private final InscripcionRepository inscripciones;
 public CuentaService(UsuarioRepository u,VoluntarioRepository v,OrganizacionRepository o,CampaniaRepository c,PasswordEncoder e,AccesoService a,InscripcionRepository i){usuarios=u;voluntarios=v;organizaciones=o;campanias=c;encoder=e;acceso=a;inscripciones=i;}
 public List<Voluntario> listarVoluntarios(){acceso.exigirAdmin();return voluntarios.findAll();}
 public List<Organizacion> listarOrganizaciones(){acceso.exigirAdmin();return organizaciones.findAll();}
 public long totalVoluntarios(){return voluntarios.count();}
 public long totalOrganizaciones(){return organizaciones.count();}
 public Voluntario voluntario(Integer id){acceso.exigirCuenta(id,false);return voluntarios.findById(id).orElseThrow(()->new IllegalArgumentException("Voluntario no encontrado"));}
 public Organizacion organizacion(Integer id){acceso.exigirCuenta(id,true);return organizaciones.findById(id).orElseThrow(()->new IllegalArgumentException("Organización no encontrada"));}
 public CuentaDTO buscar(Integer id,boolean org){
  Usuario u=org?organizacion(id).getUsuario():voluntario(id).getUsuario(); CuentaDTO d=new CuentaDTO();
  d.setId(id);d.setNombre(u.getNombre());d.setEmail(u.getEmail());d.setUbicacion(u.getUbicacion());
  if(org){d.setRuc(organizacion(id).getRuc());d.setInfoContacto(organizacion(id).getInfoContacto());}
  else{d.setEdad(voluntario(id).getEdad());d.setHabilidades(voluntario(id).getHabilidades());} return d;
 }
 public Integer guardar(CuentaDTO d,boolean org){acceso.exigirAdmin();return guardarInterno(d,org);}
 public Integer registrar(CuentaDTO d,boolean org){d.setId(null);return guardarInterno(d,org);}
 public CuentaDTO miCuenta(){
  boolean org=acceso.rol("ORGANIZACION");
  return buscar(org?acceso.organizacionId():acceso.voluntarioId(),org);
 }
 /** Devuelve true si hay que cerrar la sesión porque cambiaron las credenciales. */
 public boolean actualizarMiCuenta(CuentaDTO d){
  boolean org=acceso.rol("ORGANIZACION");
  Integer id=org?acceso.organizacionId():acceso.voluntarioId();
  Usuario u=org?organizacion(id).getUsuario():voluntario(id).getUsuario();
  boolean nuevasCredenciales=!u.getEmail().equalsIgnoreCase(d.getEmail().trim()) || (d.getContrasena()!=null && !d.getContrasena().isBlank());
  d.setId(id);
  guardarInterno(d,org);
  return nuevasCredenciales;
 }
 private Integer guardarInterno(CuentaDTO d,boolean org){
  Organizacion o=org?(d.getId()==null?new Organizacion():organizacion(d.getId())):null;
  Voluntario v=org?null:(d.getId()==null?new Voluntario():voluntario(d.getId()));
  Usuario u=d.getId()==null?new Usuario():(org?o.getUsuario():v.getUsuario());
  String email=d.getEmail().trim().toLowerCase(Locale.ROOT);
  if(usuarios.existsByEmailIgnoreCaseAndIdNot(email,u.getId()==null?-1:u.getId())) throw new IllegalArgumentException("El correo ya está registrado");
  if(d.getId()==null && (d.getContrasena()==null || d.getContrasena().isBlank())) throw new IllegalArgumentException("Ingresa una contraseña para la nueva cuenta");
  if(d.getContrasena()!=null && !d.getContrasena().isBlank()){
   if(d.getContrasena().length()<6 || d.getContrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres y como máximo 72 bytes");
   u.setContrasena(encoder.encode(d.getContrasena()));
  }
  u.setNombre(d.getNombre().trim());u.setEmail(email);u.setUbicacion(d.getUbicacion().trim());u.setRol(org?"ORGANIZACION":"VOLUNTARIO");
  if(org){
   if(d.getRuc()==null || !d.getRuc().matches("[0-9]{11}") || d.getInfoContacto()==null || d.getInfoContacto().isBlank()) throw new IllegalArgumentException("Ingresa un RUC de 11 dígitos y la información de contacto");
   o.setUsuario(u);o.setRuc(d.getRuc());o.setInfoContacto(d.getInfoContacto());return organizaciones.saveAndFlush(o).getId();
  }
  if(d.getEdad()==null) throw new IllegalArgumentException("Ingresa la edad del voluntario");
  v.setUsuario(u);v.setEdad(d.getEdad());v.setHabilidades(d.getHabilidades());return voluntarios.saveAndFlush(v).getId();
 }
 public void eliminar(Integer id,boolean org){
  acceso.exigirAdmin();
  if(org){if(!campanias.findByOrganizacionId(id).isEmpty())throw new IllegalArgumentException("Primero elimina o reasigna las campañas de la organización");organizaciones.delete(organizacion(id));}
  else {inscripciones.deleteAll(inscripciones.findByVoluntarioId(id));inscripciones.flush();voluntarios.delete(voluntario(id));}
 }
}
