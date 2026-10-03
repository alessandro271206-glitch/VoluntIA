package com.voluntared.voluntia.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="inscripcion") @Getter @Setter @NoArgsConstructor
public class Inscripcion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="inscripcion_id") private Integer id;
 @ManyToOne @JoinColumn(name="voluntario_id",nullable=false) private Voluntario voluntario;
 @ManyToOne @JoinColumn(name="campania_id",nullable=false) private Campania campania;
 @Column(length=50) private String estado;
}
