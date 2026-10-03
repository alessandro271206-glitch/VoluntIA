package com.voluntared.voluntia.controller;

import com.voluntared.voluntia.service.AccesoService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class SesionControllerAdvice {
    private final AccesoService acceso;
    public SesionControllerAdvice(AccesoService acceso){this.acceso=acceso;}

    @ModelAttribute
    public void sesion(Model model){
        model.addAttribute("esAdmin",acceso.admin());
        model.addAttribute("esOrganizacion",acceso.rol("ORGANIZACION"));
        model.addAttribute("esVoluntario",acceso.rol("VOLUNTARIO"));
        model.addAttribute("autenticado",acceso.admin() || acceso.rol("ORGANIZACION") || acceso.rol("VOLUNTARIO"));
    }
}
