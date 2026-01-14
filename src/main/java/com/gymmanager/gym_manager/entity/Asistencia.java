package com.gymmanager.gym_manager.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ASISTENCIA")
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASISTENCIA")
    private Long id;
    @Column(name = "FECHA")
    private LocalDate fecha;
    @Column(name = "PRESENTE")
    private Boolean presente;
    @Column(name = "ID_ACTIVIDAD_CLIENTE")
    private long Id_actividad_cliente;
    @Column(name = "ID_CLIENTE")
    @ManyToOne
    private Cliente cliente;
    @Column(name = "ID_ACTIVIDAD")
    @ManyToOne
    private Actividad actividad;

    public Asistencia() {
    }

    

    /* ================== Getters y Setters ================== */

    public Asistencia(Long id, LocalDate fecha, Boolean presente, long id_actividad_cliente, Cliente cliente,
            Actividad actividad) {
        this.id = id;
        this.fecha = fecha;
        this.presente = presente;
        Id_actividad_cliente = id_actividad_cliente;
        this.cliente = cliente;
        this.actividad = actividad;
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

    /* ================== LÓGICA DE ASISTENCIA ================== */

    public tomarPresente()

}
