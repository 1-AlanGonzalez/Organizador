package com.gymmanager.gym_manager.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ASISTENCIA")
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASISTENCIA")
    private Integer idAsistencia;

    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @Column(name = "PRESENTE", nullable = false)
    private Boolean presente;


    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    public Asistencia() {
    }

    

    /* ================== Getters y Setters ================== */

    public Asistencia(LocalDate fecha, Boolean presente, ActividadCliente actividadCliente) {
        this.fecha = fecha;
        this.presente = presente;
        this.actividadCliente = actividadCliente;
    }


    // public Long getId() {
    //     return id;
    // }
    // public void setId(Long id) {
    //     this.id = id;
    // }
    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    public Boolean getPresente() {
        return presente;
    }
    public void setPresente(Boolean presente) {
        this.presente = presente;
    }

    public ActividadCliente getActividadCliente() {
        return actividadCliente;
    }

    public void setActividadCliente(ActividadCliente actividadCliente) {
        this.actividadCliente = actividadCliente;
    }
    

    /* ================== LÓGICA DE ASISTENCIA ================== */

    
}
