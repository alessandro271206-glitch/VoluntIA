package com.voluntared.voluntia.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import com.voluntared.voluntia.service.*;
import com.voluntared.voluntia.dto.CuentaDTO;
@Controller @RequestMapping("/organizaciones")
public class OrganizacionController {
 private final CuentaService service; private final CampaniaService campanias; private final InscripcionService inscripciones;
 public OrganizacionController(CuentaService s,CampaniaService c,InscripcionService i){service=s;campanias=c;inscripciones=i;}
 private void modelo(Model m){m.addAttribute("org",true);m.addAttribute("ruta","organizaciones");m.addAttribute("seccion","organizaciones");}
 @GetMapping public String listar(Model m){modelo(m);m.addAttribute("cuentas",service.listarOrganizaciones());return "cuentas";}
 @GetMapping("/nuevo") public String nuevo(Model m){modelo(m);m.addAttribute("cuenta",new CuentaDTO());return "cuenta-form";}
 @GetMapping("/editar/{id}") public String editar(@PathVariable Integer id,Model m){modelo(m);m.addAttribute("cuenta",service.buscar(id,true));return "cuenta-form";}
 @PostMapping("/guardar") public String guardar(@Valid @ModelAttribute("cuenta") CuentaDTO d,BindingResult result,Model m,RedirectAttributes flash){
  modelo(m);if(result.hasErrors())return "cuenta-form";
  try{service.guardar(d,true);}catch(IllegalArgumentException e){result.reject("datos",e.getMessage());return "cuenta-form";}
  flash.addFlashAttribute("exito","Registro guardado correctamente");return "redirect:/organizaciones";
 }
 @GetMapping("/{id}") public String detalle(@PathVariable Integer id,Model m){modelo(m);m.addAttribute("cuenta",service.buscar(id,true));m.addAttribute("campanias",campanias.porOrganizacion(id));return "cuenta-detalle";}
 @PostMapping("/eliminar/{id}") public String eliminar(@PathVariable Integer id,RedirectAttributes flash){try{service.eliminar(id,true);flash.addFlashAttribute("exito","Cuenta eliminada");}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/organizaciones";}
}
