package com.voluntared.voluntia.controller;

import com.voluntared.voluntia.dto.*;
import com.voluntared.voluntia.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** API del panel de gestión. Usa la misma sesión y protección CSRF que los formularios. */
@RestController
@RequestMapping("/api")
public class ApiController {
    private final CuentaService cuentas;
    private final CampaniaService campanias;
    private final InscripcionService inscripciones;
    public ApiController(CuentaService cuentas, CampaniaService campanias, InscripcionService inscripciones) {
        this.cuentas=cuentas; this.campanias=campanias; this.inscripciones=inscripciones;
    }
    @GetMapping("/voluntarios") public List<CuentaDTO> voluntarios() {
        return cuentas.listarVoluntarios().stream().map(v->cuentas.buscar(v.getId(),false)).toList();
    }
    @GetMapping("/organizaciones") public List<CuentaDTO> organizaciones() {
        return cuentas.listarOrganizaciones().stream().map(o->cuentas.buscar(o.getId(),true)).toList();
    }
    @GetMapping("/voluntarios/{id}") public CuentaDTO voluntario(@PathVariable Integer id) {return cuentas.buscar(id,false);}
    @GetMapping("/organizaciones/{id}") public CuentaDTO organizacion(@PathVariable Integer id) {return cuentas.buscar(id,true);}
    @PostMapping("/voluntarios") @ResponseStatus(HttpStatus.CREATED)
    public CuentaDTO crearVoluntario(@Valid @RequestBody CuentaDTO dto) {dto.setId(null);return cuentas.buscar(cuentas.guardar(dto,false),false);}
    @PostMapping("/organizaciones") @ResponseStatus(HttpStatus.CREATED)
    public CuentaDTO crearOrganizacion(@Valid @RequestBody CuentaDTO dto) {dto.setId(null);return cuentas.buscar(cuentas.guardar(dto,true),true);}
    @PutMapping("/voluntarios/{id}") public CuentaDTO actualizarVoluntario(@PathVariable Integer id,@Valid @RequestBody CuentaDTO dto) {
        dto.setId(id);cuentas.guardar(dto,false);return cuentas.buscar(id,false);
    }
    @PutMapping("/organizaciones/{id}") public CuentaDTO actualizarOrganizacion(@PathVariable Integer id,@Valid @RequestBody CuentaDTO dto) {
        dto.setId(id);cuentas.guardar(dto,true);return cuentas.buscar(id,true);
    }
    @DeleteMapping("/voluntarios/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarVoluntario(@PathVariable Integer id) {cuentas.eliminar(id,false);}
    @DeleteMapping("/organizaciones/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarOrganizacion(@PathVariable Integer id) {cuentas.eliminar(id,true);}
    @GetMapping("/campanias") public List<CampaniaDTO> listarCampanias(@RequestParam(required=false) String q,@RequestParam(required=false) String ubicacion) {
        return campanias.listar(q,ubicacion).stream().map(c->campanias.detalle(c.getId())).toList();
    }
    @GetMapping("/campanias/{id}") public CampaniaDTO campania(@PathVariable Integer id) {return campanias.detalle(id);}
    @PostMapping("/campanias") @ResponseStatus(HttpStatus.CREATED)
    public CampaniaDTO crearCampania(@Valid @RequestBody CampaniaDTO dto) {dto.setId(null);return campanias.detalle(campanias.guardar(dto));}
    @PutMapping("/campanias/{id}") public CampaniaDTO actualizarCampania(@PathVariable Integer id,@Valid @RequestBody CampaniaDTO dto) {
        dto.setId(id);campanias.guardar(dto);return campanias.detalle(id);
    }
    @DeleteMapping("/campanias/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCampania(@PathVariable Integer id) {campanias.eliminar(id);}
    @GetMapping("/organizaciones/{id}/campanias") public List<CampaniaDTO> campaniasOrganizacion(@PathVariable Integer id) {
        cuentas.organizacion(id);return campanias.porOrganizacion(id).stream().map(c->campanias.detalle(c.getId())).toList();
    }
    @GetMapping("/voluntarios/{id}/historial") public List<Map<String,Object>> historial(@PathVariable Integer id) {
        cuentas.voluntario(id);return inscripciones.historial(id).stream().map(i->Map.<String,Object>of("id",i.getId(),"campania",campanias.detalle(i.getCampania().getId()),"estado",i.getEstado())).toList();
    }
    @GetMapping("/csrf") public Map<String,String> csrf(org.springframework.security.web.csrf.CsrfToken token) {
        return Map.of("headerName",token.getHeaderName(),"token",token.getToken());
    }
    @PostMapping("/campanias/{id}/mi-inscripcion") @ResponseStatus(HttpStatus.CREATED)
    public void unirme(@PathVariable Integer id){inscripciones.unirme(id);}
    @DeleteMapping("/campanias/{id}/mi-inscripcion") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(@PathVariable Integer id){inscripciones.salir(id);}
    @DeleteMapping("/campanias/{id}/participantes/{voluntarioId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable Integer id,@PathVariable Integer voluntarioId){inscripciones.retirar(id,voluntarioId);}
}
