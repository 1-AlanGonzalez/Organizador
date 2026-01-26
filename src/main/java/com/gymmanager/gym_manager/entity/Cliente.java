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

    
    // Relación con ActividadCliente
    // orphanRemoval=true significa que si se elimina la inscripcion del cliente, se elimina de la base de datos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
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
    public Set<ActividadCliente> getInscripciones() {
        return inscripciones;
    }
    public void setInscripciones(Set<ActividadCliente> inscripciones) {
        this.inscripciones = inscripciones;
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
        inscripcion.activar();
        inscripcion.setCliente(this);
    }

    public void darseDeBajaAInscripcion(ActividadCliente inscripcion){
        if(!inscripciones.contains(inscripcion)){
            throw new RuntimeException("La inscripcion que quiere darse de baja no esta en sus actividades");
        }

        inscripcion.darseDeBaja(); 
    }

    public boolean adeuda() {
        // Agrego un IF para evitar un NullPointerException (Error al intentar usar un stream en una lista nula)
        if (inscripciones == null || inscripciones.isEmpty()) {
        return false;
    }       
        return inscripciones.stream().anyMatch(i -> i.tieneAdeudaVencida());
    }


    public BigDecimal totalAdeudado() {
        return inscripciones.stream().map(ActividadCliente::calcularAdeudado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void pagarTodo(){
        inscripciones.stream().forEach(ActividadCliente::pagarTodo);
    }
}

