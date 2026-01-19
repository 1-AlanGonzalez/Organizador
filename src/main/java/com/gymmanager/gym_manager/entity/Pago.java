package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Integer idPago;

    @Column(name = "MES", nullable = false)
    private int mes;

    @Column(name = "ANIO", nullable = false)
    private int anio;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    @Column(name = "MONTO_A_PAGAR", nullable = false)
    private BigDecimal montoAPagar;

    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    public Pago() {}

    public Pago(int mes, int anio, BigDecimal monto, ActividadCliente actividadCliente) {
        this.mes = mes;
        this.anio = anio;
        this.montoAPagar = monto;
        this.estado = EstadoPago.ADEUDA;
        this.fechaGeneracion = LocalDate.now();
        this.actividadCliente = actividadCliente;
    }

    /* ================== Getters y Setters ================== */

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
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

    
    public int getMes() {
            return mes;
        }

    public void setMes(int mes) {
            this.mes = mes;
        }

    public int getAnio() {
            return anio;
        }

    public void setAnio(int anio) {
            this.anio = anio;
        }

    /* ================== LÓGICA DEL PAGO ================== */


    

    public void pagar(){
        if(estado == EstadoPago.PAGADO){
            throw new RuntimeException("El pago ya esta realizado");
        }

        estado = EstadoPago.PAGADO; 
    
    }


}
