package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table(name = "ACTIVIDAD")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD")
    private Integer idActividad;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "CUPO_MAXIMO", nullable = false)
    private Integer cupoMaximo;

    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "ID_INSTRUCTOR")
    private Instructor instructor;

    private Integer cuposActuales = 0;
    @Transient
    private Set<Dicta> dictados = new HashSet<>(); /* Esta mal la notacion es para cambiar luego */

    public Actividad() {
    }

    
    public Actividad(String nombre, Integer cupoMaximo, BigDecimal precio) {
        this.nombre = nombre;
        this.cupoMaximo = cupoMaximo;
        this.precio = precio;
    }

    /* ================== Getters y Setters ================== */


    public Integer getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    /* ================== LÓGICA DE ACTIVIDAD ================== */

    public void aumentoDeCuposActuales(){
        if(cuposActuales > cupoMaximo){
            throw new RuntimeException("El cupo de la actividad esta lleno");
        }

        cuposActuales+=1;
    }

    public void liberacionDeCuposActuales(){
        cuposActuales-=1;
    }
}

