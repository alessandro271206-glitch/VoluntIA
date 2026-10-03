package com.voluntared.voluntia.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="usuario") @Getter @Setter @NoArgsConstructor
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="usuario_id") private Integer id;
 @Column(nullable=false,length=100) private String nombre;
 @Column(nullable=false,unique=true,length=100) private String email;
 @Column(nullable=false,length=100) private String contrasena;
 @Column(nullable=false,length=50) private String rol;
 @Column(length=255) private String ubicacion;
}
