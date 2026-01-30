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
    @Column(name = "ID_PAGO")
    private Integer idPago;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_PAGO", nullable = false)
    private EstadoPago estado;

    @Column(name = "MONTO_A_PAGAR", nullable = false)
    private BigDecimal montoAPagar;

    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "METODO_PAGO", nullable = true)
    private MetodoDePago metodoPago = MetodoDePago.NO_ESPECIFICADO;
    // AÑADO HOY 28/1

    @Column(name = "MONTO_ABONADO")
    private BigDecimal montoAbonado;

    @Column(name = "DEUDA")
    private BigDecimal deuda;

    @Column(name = "FECHA_PAGO")
    private LocalDate fechaPago;

    // @Column(name = "OBSERVACIONES", nullable = false, columnDefinition = "TEXT")
    // private String observaciones;

    // -------------------

    public Pago() {}

    public Pago(BigDecimal montoAPagar, LocalDate fechaGeneracion,LocalDate fechaVencimiento, 
        ActividadCliente actividadCliente) {
        this.fechaGeneracion = fechaGeneracion;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoPago.ADEUDA;
        this.montoAPagar = montoAPagar;
        this.actividadCliente = actividadCliente;
        this.metodoPago = MetodoDePago.NO_ESPECIFICADO;
        
    }



    /* ================== Getters y Setters ================== */
   
    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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

    public ActividadCliente getActividadCliente() {
        return actividadCliente;
    }

    public void setActividadCliente(ActividadCliente actividadCliente) {
        this.actividadCliente = actividadCliente;
    }

    public MetodoDePago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoDePago metodoPago) {
        this.metodoPago = metodoPago;
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
    // PAGO AGREGADO HOY 28/1


    // ===================

    /* Pagos de mas de un mes o año */


    public void aplicarRecargo(BigDecimal recargo){
        if (recargo == null || recargo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Recargo invalido");
        }
        this.montoAPagar = this.montoAPagar.add(recargo);
    }
}
