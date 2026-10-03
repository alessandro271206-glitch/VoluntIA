package com.voluntared.voluntia.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
import com.voluntared.voluntia.entity.*;
import com.voluntared.voluntia.repository.*;
@Service @Transactional
public class InscripcionService {
 private final InscripcionRepository repository; private final CampaniaService campanias;
 private final VoluntarioRepository voluntarios; private final EntityManager em; private final AccesoService acceso;
 public InscripcionService(InscripcionRepository r,CampaniaService c,VoluntarioRepository v,EntityManager e,AccesoService a){repository=r;campanias=c;voluntarios=v;em=e;acceso=a;}
 public List<Inscripcion> historial(Integer id){acceso.exigirCuenta(id,false);return repository.findByVoluntarioId(id);}
 public List<Inscripcion> participantes(Integer id){acceso.exigirGestion(campanias.buscar(id));return repository.findByCampaniaId(id);}
 public long cantidad(Integer id){return repository.countByCampaniaId(id);}
 public Optional<Inscripcion> miInscripcion(Integer id){return repository.findByVoluntarioIdAndCampaniaId(acceso.voluntarioId(),id);}
 private Campania bloquear(Integer id){Campania c=campanias.buscar(id);em.refresh(c,LockModeType.PESSIMISTIC_WRITE);return c;}
 public void inscribir(Integer campaniaId,Integer voluntarioId){acceso.exigirAdmin();inscribirInterno(campaniaId,voluntarioId);}
 public void unirme(Integer campaniaId){inscribirInterno(campaniaId,acceso.voluntarioId());}
 private void inscribirInterno(Integer campaniaId,Integer voluntarioId){Campania c=bloquear(campaniaId);
  if(c.getFecha()==null || !c.getFecha().isAfter(java.time.LocalDateTime.now()))throw new IllegalArgumentException("El voluntariado ya comenzó");
  if(repository.existsByVoluntarioIdAndCampaniaId(voluntarioId,campaniaId))throw new IllegalArgumentException("El voluntario ya está inscrito");
  if(c.getCupos()==null || repository.countByCampaniaId(campaniaId)>=c.getCupos())throw new IllegalArgumentException("No quedan cupos disponibles");
  Inscripcion i=new Inscripcion();i.setCampania(c);i.setVoluntario(voluntarios.findById(voluntarioId).orElseThrow(()->new IllegalArgumentException("Voluntario no encontrado")));i.setEstado("INSCRITO");repository.saveAndFlush(i);
 }
 public void salir(Integer campaniaId){
  Integer vid=acceso.voluntarioId();bloquear(campaniaId);
  eliminarInscripcion(repository.findByVoluntarioIdAndCampaniaId(vid,campaniaId).orElseThrow(()->new IllegalArgumentException("No estás inscrito en este voluntariado")));
 }
 public void retirar(Integer campaniaId,Integer voluntarioId){
  Campania c=bloquear(campaniaId);acceso.exigirGestion(c);
  Inscripcion i=repository.findByVoluntarioIdAndCampaniaId(voluntarioId,campaniaId).orElseThrow(()->new IllegalArgumentException("Inscripción no encontrada"));
  repository.delete(i);repository.flush();
 }
 private void eliminarInscripcion(Inscripcion i){
  if("ASISTIO".equals(i.getEstado()))throw new IllegalArgumentException("La asistencia registrada se conserva en el historial");
  repository.delete(i);repository.flush();
 }
 private Inscripcion buscar(Integer id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Inscripción no encontrada"));}
 public void cancelar(Integer id){Inscripcion i=buscar(id);acceso.exigirGestion(i.getCampania());Campania c=bloquear(i.getCampania().getId());acceso.exigirGestion(c);eliminarInscripcion(i);}
 public void asistencia(Integer id){Inscripcion i=buscar(id);acceso.exigirGestion(i.getCampania());Campania c=bloquear(i.getCampania().getId());acceso.exigirGestion(c);i.setEstado("ASISTIO");}
}
