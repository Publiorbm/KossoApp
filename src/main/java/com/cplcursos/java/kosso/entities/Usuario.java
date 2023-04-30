package com.cplcursos.java.kosso.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String email;
    private String nombre;
    private String clave;
    private String rol;
    private String foto;
    private Boolean activo = false;
    private Date creadoEl;
    private String acercaDe;
    private Integer puntosEjercicios;
    private Integer puntosRespuestas;

    @OneToMany(mappedBy = "usuario")
    private List<Pregunta> preguntas;

    @OneToMany(mappedBy = "usuario")
    private List<Respuesta> respuestas;

    @OneToMany(mappedBy = "usuario")
    private List<Comentario> comentarios;

}
