package com.voluntared.voluntia.controller;

import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;

@Order(0)
@RestControllerAdvice(assignableTypes=ApiController.class)
public class ApiErrorAdvice {
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String,String>> prohibido(){return ResponseEntity.status(403).body(Map.of("mensaje","No tienes permiso para realizar esta acción"));}
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> datos(IllegalArgumentException e) {
        HttpStatus status=e.getMessage().contains("no encontrad")?HttpStatus.NOT_FOUND:HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("mensaje",e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> validacion(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje","Revisa los campos obligatorios y sus formatos"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflicto() {
        return ResponseEntity.status(409).body(Map.of("mensaje","Registro duplicado o relación pendiente"));
    }
}
