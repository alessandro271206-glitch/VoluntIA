package com.voluntared.voluntia.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="organizacion") @Getter @Setter @NoArgsConstructor
public class Organizacion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="organizacion_id") private Integer id;
 @OneToOne(cascade=CascadeType.ALL) @JoinColumn(name="usuario_id",nullable=false,unique=true) private Usuario usuario;
 @Column(length=100) private String ruc;
 @Column(name="info_contacto",length=200) private String infoContacto;
}
