package com.voluntared.voluntia.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voluntared.voluntia.entity.*;
import com.voluntared.voluntia.dto.*;
import com.voluntared.voluntia.repository.*;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.AccessDeniedException;
@Service @Transactional
public class CampaniaService {
 private final CampaniaRepository repository; private final InscripcionRepository inscripciones;
 private final OrganizacionRepository organizaciones; private final AccesoService acceso; private final EntityManager em;
 public CampaniaService(CampaniaRepository r,OrganizacionRepository o,InscripcionRepository i,AccesoService a,EntityManager e){repository=r;organizaciones=o;inscripciones=i;acceso=a;em=e;}
 public List<Campania> listar(String q,String ubicacion){return repository.findAll().stream().filter(c->q==null || (c.getTitulo()+" "+c.getDescripcion()).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))).filter(c->ubicacion==null || (c.getUbicacion()!=null && c.getUbicacion().toLowerCase(Locale.ROOT).contains(ubicacion.toLowerCase(Locale.ROOT)))).toList();}
 public Campania buscar(Integer id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Campaña no encontrada"));}
 public CampaniaDTO detalle(Integer id){Campania c=buscar(id);CampaniaDTO d=new CampaniaDTO();d.setId(id);d.setTitulo(c.getTitulo());d.setDescripcion(c.getDescripcion());d.setUbicacion(c.getUbicacion());d.setFecha(c.getFecha());d.setRequisitos(c.getRequisitos());d.setCupos(c.getCupos());if(c.getOrganizacion()!=null)d.setOrganizacionId(c.getOrganizacion().getId());return d;}
 public CampaniaDTO editar(Integer id){acceso.exigirGestion(buscar(id));return detalle(id);}
 public Integer guardar(CampaniaDTO d){
  if(!acceso.admin() && !acceso.rol("ORGANIZACION"))throw new AccessDeniedException("Solo las organizaciones pueden publicar voluntariados");
  Campania c=d.getId()==null?new Campania():buscar(d.getId());
  if(d.getId()!=null){acceso.exigirGestion(c);em.refresh(c,LockModeType.PESSIMISTIC_WRITE);acceso.exigirGestion(c);}
  Integer oid=acceso.admin()?d.getOrganizacionId():acceso.organizacionId();
  if(oid==null)throw new IllegalArgumentException("Selecciona una organización");
  Organizacion organizacion=organizaciones.findById(oid).orElseThrow(()->new IllegalArgumentException("Organización no encontrada"));
  if(d.getId()==null && (d.getFecha()==null || !d.getFecha().isAfter(java.time.LocalDateTime.now())))throw new IllegalArgumentException("La fecha del nuevo voluntariado debe ser futura");
  if(d.getCupos()<inscripciones.countByCampaniaId(d.getId()==null?-1:d.getId()))throw new IllegalArgumentException("Los cupos no pueden ser menores que los participantes inscritos");
  c.setTitulo(d.getTitulo().trim());c.setDescripcion(d.getDescripcion().trim());c.setUbicacion(d.getUbicacion().trim());c.setFecha(d.getFecha());c.setRequisitos(d.getRequisitos());c.setCupos(d.getCupos());c.setOrganizacion(organizacion);return repository.saveAndFlush(c).getId();
 }
 public void eliminar(Integer id){
  Campania c=buscar(id);acceso.exigirGestion(c);em.refresh(c,LockModeType.PESSIMISTIC_WRITE);acceso.exigirGestion(c);
  inscripciones.deleteAll(inscripciones.findByCampaniaId(id));inscripciones.flush();repository.delete(c);repository.flush();
 }
 public List<Campania> porOrganizacion(Integer id){acceso.exigirCuenta(id,true);return repository.findByOrganizacionId(id);}
}
