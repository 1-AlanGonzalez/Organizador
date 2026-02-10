package com.gymmanager.gym_manager.entity.dto;

import java.time.LocalDate;

public class ReporteAsistenciaDTO {
    private String nombreCliente;
    private String apellidoCliente; // Agregado
    private String nombreActividad;
    private LocalDate fecha;
    private String estado; // "PRESENTE" o "AUSENTE"
    private String avatarLetra; // Para el circulito

    public ReporteAsistenciaDTO(String nombre, String apellido, String actividad, LocalDate fecha, boolean isPresente) {
        this.nombreCliente = nombre;
        this.apellidoCliente = apellido;
        this.nombreActividad = actividad;
        this.fecha = fecha;
        this.estado = isPresente ? "PRESENTE" : "AUSENTE";
        this.avatarLetra = (nombre != null && !nombre.isEmpty()) ? nombre.substring(0, 1) : "A";
    }

    // Getters y Setters necesarios
    public String getNombreCliente() { return nombreCliente; }
    public String getApellidoCliente() { return apellidoCliente; }
    public String getNombreActividad() { return nombreActividad; }
    public LocalDate getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getAvatarLetra() { return avatarLetra; }
}
