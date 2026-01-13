package com.gymmanager.gym_manager.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String nombre;
    private String apellido;
    private String dni;
    private Integer telefono;

    @ManyToAny
    private Set<Actividad> actividades = new HashSet<>();
    private boolean asiste;
    private boolean pago;
    private LocalDate fechaDePago;

    public Cliente() {

    }
    public Cliente(String nombre, String apellido, boolean asiste) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.asiste = asiste;
        this.pago = false;
    }

    public void agregarActividad(Actividad actividad) {
        actividades.add(actividad);
    }

    public void quitarActividad(Actividad actividad) {
        actividades.remove(actividad);
    }

    public void registrarPago(LocalDate fecha) {
        this.pago = true;
        this.fechaDePago = fecha;
    }

    public double calcularCuotaTotal() {
        return actividades.stream().mapToDouble(Actividad::getCostoMensual).sum();
    }

    public boolean estaAlDia() {
        return pago;
    }

    public boolean isAsiste() {
        return asiste;
    }

    public boolean isPago() {
        return pago;
    }
    public Integer getTelefono() {
        return telefono;
    }
    public void setTelefono(Integer telefono) {
        this.telefono = telefono;
    }
    public LocalDate getFechaDePago() {
        return fechaDePago;
    }

    public Set<Actividad> getActividades() {
        return actividades;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}

