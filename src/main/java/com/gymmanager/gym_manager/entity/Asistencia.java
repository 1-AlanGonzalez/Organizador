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
    @JoinColumn(name = "ID_CLIENTE")
    private Cliente cliente;


    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD")
    private Actividad actividad;

    public Asistencia() {
    }

    

    /* ================== Getters y Setters ================== */

    public Asistencia(LocalDate fecha, Boolean presente, Cliente cliente,Actividad actividad) {
        this.fecha = fecha;
        this.presente = presente;
        this.cliente = cliente;
        this.actividad = actividad;
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
    public Cliente getCliente() {
        return cliente;
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    public Actividad getActividad() {
        return actividad;
    }
    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    } 

    /* ================== LÓGICA DE ASISTENCIA ================== */

    
}
