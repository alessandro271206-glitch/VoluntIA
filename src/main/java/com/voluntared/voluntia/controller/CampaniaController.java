package com.voluntared.voluntia.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import com.voluntared.voluntia.service.*;
import com.voluntared.voluntia.dto.CampaniaDTO;
@Controller @RequestMapping("/campanias")
public class CampaniaController {
 private final CampaniaService service;private final CuentaService cuentas;private final InscripcionService inscripciones;private final AccesoService acceso;
 public CampaniaController(CampaniaService s,CuentaService c,InscripcionService i,AccesoService a){service=s;cuentas=c;inscripciones=i;acceso=a;}
 @ModelAttribute public void comunes(Model m){m.addAttribute("seccion","campanias");}
 @GetMapping public String listar(@RequestParam(required=false) String q,@RequestParam(required=false) String ubicacion,Model m){
  var campanias=service.listar(q,ubicacion);m.addAttribute("campanias",campanias);m.addAttribute("q",q);m.addAttribute("ubicacion",ubicacion);
  m.addAttribute("totalVoluntarios",cuentas.totalVoluntarios());m.addAttribute("totalOrganizaciones",cuentas.totalOrganizaciones());m.addAttribute("misCampanias",false);
  m.addAttribute("gestionables",campanias.stream().filter(acceso::puedeGestionar).map(c->c.getId()).collect(java.util.stream.Collectors.toSet()));return "campanias";
 }
 private void opciones(Model m){m.addAttribute("organizaciones",acceso.admin()?cuentas.listarOrganizaciones():java.util.List.of(cuentas.organizacion(acceso.organizacionId())));}
 @GetMapping("/nuevo") public String nuevo(Model m){opciones(m);m.addAttribute("campania",new CampaniaDTO());return "campania-form";}
 @GetMapping("/editar/{id}") public String editar(@PathVariable Integer id,Model m){opciones(m);m.addAttribute("campania",service.editar(id));return "campania-form";}
 @PostMapping("/guardar") public String guardar(@Valid @ModelAttribute("campania") CampaniaDTO d,BindingResult result,Model m,RedirectAttributes flash){
  opciones(m);if(result.hasErrors())return "campania-form";
  try{service.guardar(d);}catch(IllegalArgumentException e){result.reject("datos",e.getMessage());return "campania-form";}
  flash.addFlashAttribute("exito","Voluntariado guardado correctamente");return acceso.admin()?"redirect:/campanias":"redirect:/mis-voluntariados";
 }
 @GetMapping("/{id}") public String detalle(@PathVariable Integer id,Model m){
  var c=service.buscar(id);boolean gestionar=acceso.puedeGestionar(c);m.addAttribute("campania",c);m.addAttribute("puedeGestionar",gestionar);
  m.addAttribute("cantidadParticipantes",inscripciones.cantidad(id));m.addAttribute("iniciada",c.getFecha()==null || !c.getFecha().isAfter(java.time.LocalDateTime.now()));
  if(gestionar)m.addAttribute("participantes",inscripciones.participantes(id));
  if(acceso.admin())m.addAttribute("voluntarios",cuentas.listarVoluntarios());
  if(acceso.rol("VOLUNTARIO"))m.addAttribute("miInscripcion",inscripciones.miInscripcion(id).orElse(null));
  return "campania-detalle";
 }
 @PostMapping("/eliminar/{id}") public String eliminar(@PathVariable Integer id,RedirectAttributes flash){service.eliminar(id);flash.addFlashAttribute("exito","Voluntariado y sus inscripciones eliminados");return acceso.admin()?"redirect:/campanias":"redirect:/mis-voluntariados";}
 @PostMapping("/{id}/unirme") public String unirme(@PathVariable Integer id,RedirectAttributes flash){try{inscripciones.unirme(id);flash.addFlashAttribute("exito","¡Ya formas parte de este voluntariado!");}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/campanias/"+id;}
 @PostMapping("/{id}/salir") public String salir(@PathVariable Integer id,RedirectAttributes flash){try{inscripciones.salir(id);flash.addFlashAttribute("exito","Has salido del voluntariado");}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/campanias/"+id;}
 @PostMapping("/{id}/participantes/{voluntarioId}/retirar") public String retirar(@PathVariable Integer id,@PathVariable Integer voluntarioId,RedirectAttributes flash){try{inscripciones.retirar(id,voluntarioId);flash.addFlashAttribute("exito","Participante retirado; su cuenta se conserva");}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/campanias/"+id;}
 @PostMapping("/{id}/inscribir") public String inscribir(@PathVariable Integer id,@RequestParam Integer voluntarioId,RedirectAttributes flash){try{inscripciones.inscribir(id,voluntarioId);flash.addFlashAttribute("exito","Voluntario inscrito");}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/campanias/"+id;}
 @PostMapping("/inscripciones/{id}/cancelar") public String cancelar(@PathVariable Integer id,RedirectAttributes flash){try{inscripciones.cancelar(id);}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/campanias";}
 @PostMapping("/inscripciones/{id}/asistencia") public String asistencia(@PathVariable Integer id){inscripciones.asistencia(id);return "redirect:/campanias";}
}
