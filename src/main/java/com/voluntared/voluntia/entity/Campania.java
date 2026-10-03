package com.voluntared.voluntia.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="campania") @Getter @Setter @NoArgsConstructor
public class Campania {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="campania_id") private Integer id;
 @Column(nullable=false,length=150) private String titulo;
 @Column(length=255) private String descripcion;
 @Column(length=255) private String ubicacion;
 private java.time.LocalDateTime fecha;
 @Column(length=255) private String requisitos;
 private Integer cupos;
 @ManyToOne @JoinColumn(name="organizacion_id") private Organizacion organizacion;
}
