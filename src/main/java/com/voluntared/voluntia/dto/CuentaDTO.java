package com.voluntared.voluntia.dto;
import lombok.*;
import jakarta.validation.constraints.*;
@Getter @Setter
public class CuentaDTO {
 private Integer id;
 @NotBlank @Size(max=100) private String nombre;
 @NotBlank @Email @Size(max=100) private String email;
 @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
 @Size(max=72) private String contrasena;
 @NotBlank @Size(max=255) private String ubicacion;
 @Min(16) @Max(120) private Integer edad;
 @Size(max=255) private String habilidades;
 @Pattern(regexp="^$|[0-9]{11}",message="El RUC debe tener 11 dígitos") private String ruc;
 @Size(max=200) private String infoContacto;
}
