package com.voluntared.voluntia.controller;

import com.voluntared.voluntia.dto.CuentaDTO;
import com.voluntared.voluntia.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mi-cuenta")
public class MiCuentaController {
    private final CuentaService cuentas;
    private final AccesoService acceso;
    public MiCuentaController(CuentaService cuentas,AccesoService acceso){this.cuentas=cuentas;this.acceso=acceso;}

    @ModelAttribute
    public void opciones(Model model){
        model.addAttribute("seccion","mi-cuenta");
        model.addAttribute("org",acceso.rol("ORGANIZACION"));
    }

    @GetMapping
    public String perfil(Model model){model.addAttribute("cuenta",cuentas.miCuenta());return "mi-cuenta";}

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cuenta") CuentaDTO dto,BindingResult result,Model model,
                          HttpServletRequest request,HttpServletResponse response,Authentication authentication,RedirectAttributes flash){
        if(result.hasErrors()){dto.setContrasena(null);return "mi-cuenta";}
        boolean credenciales;
        try{credenciales=cuentas.actualizarMiCuenta(dto);}
        catch(IllegalArgumentException e){result.reject("datos",e.getMessage());dto.setContrasena(null);return "mi-cuenta";}
        catch(DataIntegrityViolationException e){result.reject("datos","El correo ya está registrado");dto.setContrasena(null);return "mi-cuenta";}
        if(credenciales){
            new SecurityContextLogoutHandler().logout(request,response,authentication);
            return "redirect:/login?actualizada";
        }
        flash.addFlashAttribute("exito","Tu perfil fue actualizado");return "redirect:/mi-cuenta";
    }
}
