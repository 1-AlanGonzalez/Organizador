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

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoPago estado;

    @Column(name = "MONTO_A_PAGAR", nullable = false)
    private BigDecimal montoAPagar;

    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    public Pago() {}

    public Pago(BigDecimal montoAPagar, ActividadCliente actividadCliente) {
        this.fechaGeneracion = LocalDate.now();
        this.fechaVencimiento = this.fechaGeneracion.plusMonths(1);
        this.estado = EstadoPago.ADEUDA;
        this.montoAPagar = montoAPagar;
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

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /* ================== LÓGICA DEL PAGO ================== */

    /* Aca se pregunta si la fecha de hoy es igual a la fecha de vencimiento o un dia despues */

    

    public Boolean estaVencido(){
        LocalDate hoy = LocalDate.now();
        return estado == EstadoPago.ADEUDA && ( hoy.isEqual(fechaVencimiento) || hoy.isAfter(fechaVencimiento)) ;  
    }
    

    public void pagar(){
        if(estado == EstadoPago.PAGADO){
            throw new RuntimeException("El pago ya esta realizado");
        }

        estado = EstadoPago.PAGADO; 
    
    }


}
