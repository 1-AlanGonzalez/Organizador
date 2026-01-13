package com.gymmanager.gym_manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Actividad {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private String nombre;
    private double costoMensual;

    public Actividad(String nombre, double costoMensual) {
        this.nombre = nombre;
        this.costoMensual = costoMensual;
    }

    public double getCostoMensual() {
        return costoMensual;
    }

    public String getNombre() {
        return nombre;
    }
}
