package com.voluntared.voluntia.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
@ControllerAdvice
public class ErrorControllerAdvice {
 @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
 public String prohibido(Model m){m.addAttribute("mensaje","No tienes permiso para realizar esta acción.");return "error";}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
 public String noEncontrado(IllegalArgumentException e,Model m){m.addAttribute("mensaje",e.getMessage());return "error";}
 @ExceptionHandler(DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT)
 public String conflicto(Model m){m.addAttribute("mensaje","No se pudo guardar: existe un registro duplicado o una relación pendiente. Vuelve a la lista y revisa los datos.");return "error";}
}
