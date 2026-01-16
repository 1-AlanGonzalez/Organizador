package com.gymmanager.gym_manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "INSTRUCTOR")
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "ID_INSTRUCTOR")
    private Integer idInstructor;
    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;
    @Column(name = "APELLIDO", nullable = false, length = 50)
    private String apellido;
    @Column(name = "DNI", nullable = false)
    private Integer dni;
    @Column(name = "TELEFONO", nullable = false, length = 20)
    private Integer telefono;
    @ManyToMany
    @JoinColumn(name = "ACTIVIDAD")
    
    private Actividad actividad;
}
