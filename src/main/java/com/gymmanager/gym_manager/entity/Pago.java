package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PAGO")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "ID_PAGO")
    private String idPago;

    @Column(name = "FECHA_DE_PAGO", nullable = false)
    private LocalDate fechaDePago;

    @Column(name = "ESTADO", nullable = false)
    private EstadoPago estado;

    @Column(name = "MONTO_A_PAGAR", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAPagar;

    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    public Pago(){

    }

    public Pago(LocalDate fechaDePago, EstadoPago estado, BigDecimal montoAPagar ,ActividadCliente actividadCliente) {
        this.fechaDePago = fechaDePago;
        this.estado = estado;
        this.montoAPagar = montoAPagar;
        this.actividadCliente = actividadCliente;
    }


    /* ================== Getters y Setters ================== */


    public String getIdPago() {
        return idPago;
    }

    public void setIdPago(String idPago) {
        this.idPago = idPago;
    }

    public LocalDate getFechaDePago() {
        return fechaDePago;
    }

    public void setFechaDePago(LocalDate fechaDePago) {
        this.fechaDePago = fechaDePago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoAPagar() {
        return montoAPagar;
    }

    public void setMontoAPagar(BigDecimal montoAPagar) {
        this.montoAPagar = montoAPagar;
    }

    // public ActividadCliente getActividadCliente() {
    //     return actividadCliente;
    // }

    // public void setActividadCliente(ActividadCliente actividadCliente) {
    //     this.actividadCliente = actividadCliente;
    // }


    /* ================== LÓGICA DEL PAGO ================== */


    // public void generarPago()


}
