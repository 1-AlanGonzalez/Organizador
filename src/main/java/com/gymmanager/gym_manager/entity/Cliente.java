package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;



import jakarta.persistence.*;
@Entity
@Table(name = "CLIENTE")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Integer idCliente;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 50)
    private String apellido;

    @Column(name = "DNI", nullable = false, length = 15)
    private String dni;

    @Column(name = "TELEFONO", nullable = false, length = 20)
    private String telefono;

    @OneToMany
    private Set<ActividadCliente> inscripciones = new HashSet<>();

    public Cliente() {}


    public Cliente(String nombre, String apellido, String dni, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
    }

    /* ================== Getters y Setters ================== */

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /* ================== LÓGICA DEL CLIENTE ================== */


    public void agregarInscripcion(ActividadCliente inscripcion){
        inscripciones.add(inscripcion);
        inscripcion.setCliente(this);
    }

    public boolean adeuda() {
        return inscripciones.stream().anyMatch(inscripcion -> inscripcion.calcularAdeudado().compareTo(BigDecimal.ZERO) > 0);
    }

    public boolean adeudaMes(int mes, int anio) {
        return inscripciones.stream().anyMatch(inscripcion -> inscripcion.adeudaMes(mes, anio));
    }

    public BigDecimal totalAdeudado() {
        return inscripciones.stream().map(ActividadCliente::calcularAdeudado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void pagarTodo(){
        inscripciones.stream().forEach(ActividadCliente::pagarTodo);
    }

    
}

