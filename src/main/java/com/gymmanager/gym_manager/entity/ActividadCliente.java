package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "ACTIVIDAD_CLIENTE")
public class ActividadCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD_CLIENTE")
    private Integer idActividadCliente;
        
    @Column(name = "FECHA_DE_INSCRIPCION", nullable = false)
    private LocalDate fechaDeInscripcion;
    
    @Column(name = "COSTO", nullable = false)
    private BigDecimal costo;
    
    @JoinColumn(name = "ID_CLIENTE")
    private Cliente cliente;
    
    @JoinColumn(name = "ID_ACTIVIDAD")
    private Actividad actividad;

    public ActividadCliente() { }

    public ActividadCliente(LocalDate fechaDeInscripcion, BigDecimal costo, Cliente cliente, Actividad actividad) {
        this.fechaDeInscripcion = fechaDeInscripcion;
        this.costo = costo;
        this.cliente = cliente;
        this.actividad = actividad;
    }

    /* ================== Getters y Setters ================== */

    public Integer getIdActividadCliente() {
        return idActividadCliente;
    }

    public void setIdActividadCliente(Integer idActividadCliente) {
        this.idActividadCliente = idActividadCliente;
    }

    public LocalDate getFechaDeInscripcion() {
        return fechaDeInscripcion;
    }

    public void setFechaDeInscripcion(LocalDate fechaDeInscripcion) {
        this.fechaDeInscripcion = fechaDeInscripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
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

}
