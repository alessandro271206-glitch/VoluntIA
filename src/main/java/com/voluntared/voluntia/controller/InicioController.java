package com.voluntared.voluntia.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import com.voluntared.voluntia.dto.CuentaDTO;
import com.voluntared.voluntia.service.CuentaService;
import org.springframework.dao.DataIntegrityViolationException;
@Controller
public class InicioController {
 private final CuentaService cuentas;
 public InicioController(CuentaService cuentas){this.cuentas=cuentas;}
 @GetMapping("/") public String inicio(){return "inicio";}
 @GetMapping("/login") public String login(){return "login";}
 @GetMapping("/registro") public String registro(@RequestParam(defaultValue="VOLUNTARIO") String perfil,Model m){
  m.addAttribute("cuenta",new CuentaDTO());m.addAttribute("perfil","ORGANIZACION".equals(perfil)?perfil:"VOLUNTARIO");return "registro";
 }
 @PostMapping("/registro") public String registrar(@Valid @ModelAttribute("cuenta") CuentaDTO cuenta,BindingResult result,@RequestParam String perfil,Model m,RedirectAttributes flash){
  m.addAttribute("perfil",perfil);
  if(!java.util.Set.of("VOLUNTARIO","ORGANIZACION").contains(perfil))result.reject("perfil","Selecciona un perfil válido");
  if(result.hasErrors()){cuenta.setContrasena(null);return "registro";}
  try{cuentas.registrar(cuenta,"ORGANIZACION".equals(perfil));}
  catch(IllegalArgumentException e){result.reject("datos",e.getMessage());cuenta.setContrasena(null);return "registro";}
  catch(DataIntegrityViolationException e){result.reject("datos","El correo ya está registrado");cuenta.setContrasena(null);return "registro";}
  flash.addFlashAttribute("exito","Cuenta creada. Ingresa con tu correo y contraseña.");return "redirect:/login";
 }
}
