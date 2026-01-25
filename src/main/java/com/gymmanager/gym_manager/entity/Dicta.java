package com.gymmanager.gym_manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "INSTRUCTOR_ACTIVIDAD",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_actividad", "id_instructor"})
    }
)
public class Dicta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSTRUCTOR_ACTIVIDAD")
    private Integer idInstructorActividad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_ACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_INSTRUCTOR", nullable = false)
    private Instructor instructor;

    @Column(name = "DIAS", nullable = false, length = 50)
    private String dias;

    @Column(name = "HORARIO", nullable = false, length = 50)
    private String horario;



    public Dicta(){}

    public Dicta(Actividad actividad, Instructor instructor, String dias, String horario) {
        this.actividad = actividad;
        this.instructor = instructor;
        this.dias = dias;
        this.horario = horario;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public String getDias() {
        return dias;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
    

    
}


