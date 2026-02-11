package com.gymmanager.gym_manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class MetodoDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_METODO")
    private Integer idMetodoDePago;

    @Column(unique = true, nullable = false)
    private String nombre;

    public MetodoDePago(){}

    public MetodoDePago(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdMetodoDePago() {
        return idMetodoDePago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
    
}
