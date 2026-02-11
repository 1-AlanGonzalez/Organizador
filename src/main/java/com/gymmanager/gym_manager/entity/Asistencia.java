package com.gymmanager.gym_manager.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ASISTENCIA")
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASISTENCIA")
    private Integer idAsistencia;

    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @Column(name = "PRESENTE", nullable = false)
    private Boolean presente;


    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

  
     public Asistencia(LocalDate fecha, Boolean presente, ActividadCliente actividadCliente) {
        this.fecha = fecha;
        this.presente = presente;
        this.actividadCliente = actividadCliente;
    }

    /* ================== LÓGICA DE ASISTENCIA ================== */

    
}
