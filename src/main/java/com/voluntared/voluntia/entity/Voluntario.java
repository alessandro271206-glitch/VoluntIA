package com.voluntared.voluntia.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="voluntario") @Getter @Setter @NoArgsConstructor
public class Voluntario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="voluntario_id") private Integer id;
 @OneToOne(cascade=CascadeType.ALL) @JoinColumn(name="usuario_id",nullable=false,unique=true) private Usuario usuario;
 private Integer edad;
 @Column(length=255) private String habilidades;
}
