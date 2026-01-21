package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
// import java.util.HashSet;
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

    // @OneToMany
    // private Set<ActividadCliente> inscripciones = new HashSet<>();
    // MappedBy: El atributo en la clase ActividadCliente que posee la referencia al cliente
    // CascadeType.ALL: Si se elimina un cliente, se eliminan todas sus inscripciones
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Set<ActividadCliente> inscripciones;

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

    public void darseDeBajaAInscripcion(ActividadCliente inscripcion){
        if(!inscripciones.contains(inscripcion)){
            throw new RuntimeException("La inscripcion que quiere darse de baja no esta en sus actividades");
        }

        inscripciones.remove(inscripcion);    
    }

    public boolean adeuda() {
        return inscripciones.stream().anyMatch(i -> i.tieneAdeudaVencida());
    }


    public BigDecimal totalAdeudado() {
        return inscripciones.stream().map(ActividadCliente::calcularAdeudado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void pagarTodo(){
        inscripciones.stream().forEach(ActividadCliente::pagarTodo);
    }

    
}

