package com.gymmanager.gym_manager.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ASISTENCIA")
public class Asistencia {
    @Id
    private Long id;
    private LocalDate fecha;
    private Boolean presente;
    private long Id_actividad_cliente;
    
    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Actividad actividad;

    public Asistencia() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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
    public long getId_actividad_cliente() {
        return Id_actividad_cliente;
    }
    public void setId_actividad_cliente(long id_actividad_cliente) {
        Id_actividad_cliente = id_actividad_cliente;
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
