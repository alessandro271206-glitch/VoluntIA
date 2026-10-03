package com.voluntared.voluntia.controller;

import com.voluntared.voluntia.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.stream.Collectors;

@Controller
public class ComunidadController {
    private final AccesoService acceso;
    private final CampaniaService campanias;
    private final CuentaService cuentas;
    private final InscripcionService inscripciones;
    public ComunidadController(AccesoService acceso,CampaniaService campanias,CuentaService cuentas,InscripcionService inscripciones){
        this.acceso=acceso;this.campanias=campanias;this.cuentas=cuentas;this.inscripciones=inscripciones;
    }

    @GetMapping("/mis-voluntariados")
    public String voluntariados(Model model){
        var lista=acceso.admin()?campanias.listar(null,null):campanias.porOrganizacion(acceso.organizacionId());
        model.addAttribute("campanias",lista);
        model.addAttribute("gestionables",lista.stream().map(c->c.getId()).collect(Collectors.toSet()));
        model.addAttribute("totalVoluntarios",cuentas.totalVoluntarios());model.addAttribute("totalOrganizaciones",cuentas.totalOrganizaciones());
        model.addAttribute("misCampanias",true);model.addAttribute("seccion","mis-voluntariados");return "campanias";
    }

    @GetMapping("/mis-participaciones")
    public String participaciones(Model model){
        model.addAttribute("historial",inscripciones.historial(acceso.voluntarioId()));
        model.addAttribute("seccion","participaciones");return "mis-participaciones";
    }
}
