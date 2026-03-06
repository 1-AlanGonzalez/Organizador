package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PAGO")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PAGO")
    private Integer idPago;

    @Column(name = "FECHA_GENERACION", nullable = false)
    @ColumnLabel("Fecha de generacion")
    private LocalDate fechaGeneracion;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    @ColumnLabel("Fecha de Vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_PAGO", nullable = false)
    @ColumnLabel("Estado de Pago")
    private EstadoPago estado;

    @Column(name = "MONTO_A_PAGAR", nullable = false)
    @ColumnLabel("Monto a Pagar")
    private BigDecimal montoAPagar;

    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD_CLIENTE", nullable = false)
    private ActividadCliente actividadCliente;

    @ManyToOne
    @JoinColumn(name = "ID_METODO", nullable = true)
    private MetodoDePago metodoPago;
    // AÑADO HOY 28/1

    // @Column(name = "MONTO_ABONADO")
    // private BigDecimal montoAbonado;

    // @Column(name = "DEUDA")
    // private BigDecimal deuda;

    // @Column(name = "FECHA_PAGO")
    // private LocalDate fechaPago;

    @Column(name = "OBSERVACIONES", nullable = true, columnDefinition = "TEXT")
    private String observaciones;

    // -------------------
    public Pago(BigDecimal montoAPagar, LocalDate fechaGeneracion,LocalDate fechaVencimiento, 
        ActividadCliente actividadCliente, MetodoDePago metodoDePago) {
        this.fechaGeneracion = fechaGeneracion;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoPago.ADEUDA;
        this.montoAPagar = montoAPagar;
        this.actividadCliente = actividadCliente;
        this.metodoPago = metodoDePago;
        
    }

    /* ================== LÓGICA DEL PAGO ================== */

    /* Aca se pregunta si la fecha de hoy es igual a la fecha de vencimiento o un dia despues */

    public Boolean estaVencido(){
        //LocalDate hoy = LocalDate.now();
        return estado == EstadoPago.ADEUDA;
        //  && ( hoy.isEqual(fechaVencimiento) || hoy.isAfter(fechaVencimiento)) ;  
    }
    

    public void pagar(){
        if(estado == EstadoPago.PAGADO){
            throw new RuntimeException("El pago ya esta realizado");
        }

        estado = EstadoPago.PAGADO; 
    
    }
 
    public void aplicarRecargo(BigDecimal recargo){
        if (recargo == null || recargo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Recargo invalido");
        }
        this.montoAPagar = this.montoAPagar.add(recargo);
    }

    public void ajusteDeFechas(LocalDate fechaNueva){
        fechaGeneracion = fechaNueva;
        fechaVencimiento = fechaNueva.plusMonths(1);
    }
}
