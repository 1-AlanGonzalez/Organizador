package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


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

    @OneToMany(
        mappedBy = "actividad",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Dicta> dictados = new HashSet<>();


    public Actividad() {
    }

    
    public Actividad(String nombre, Integer cupoMaximo, BigDecimal precio) {
        this.nombre = nombre;
        this.cupoMaximo = cupoMaximo;
        this.precio = precio;
    }

    /* ================== Getters y Setters ================== */

    public void agregarDictado(Dicta dicta) {
    dictados.add(dicta);
    dicta.setActividad(this);
    }

    public void quitarDictado(Dicta dicta) {
        dictados.remove(dicta);
        dicta.setActividad(null);
    }

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
    public Set<Dicta> getDictados() {
        return dictados;
    }
    public void setDictados(Set<Dicta> dictados) {
        this.dictados = dictados;
    }
    
    /* ================== LÓGICA DE ACTIVIDAD ================== */


    
   
}

