package com.voluntared.voluntia.dto;
import lombok.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
@Getter @Setter
public class CampaniaDTO {
 private Integer id;
 @NotBlank @Size(max=150) private String titulo;
 @NotBlank @Size(max=255) private String descripcion;
 @NotBlank @Size(max=255) private String ubicacion;
 @NotNull @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private java.time.LocalDateTime fecha;
 @Size(max=255) private String requisitos;
 @NotNull @Min(1) @Max(100000) private Integer cupos;
 private Integer organizacionId;
}
