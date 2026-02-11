package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/* Esta entidad tendra la logica de los pagos
 * por transferencia / tarjeta / efectivo
 */

 // 5/2/26 METODO DE PAGO CAMBIO NO ES ENUM, AHORA ES UNA CLASE

@Entity
@Table(name = "CONFIGURACIONPAGO")
public class ConfiguracionDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONFIGURACION_PAGO")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_METODO", nullable = false)
    private MetodoDePago metodoDePago;

    @Column(name = "PORCENTAJE_RECARGO", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeRecargo;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo;


    public ConfiguracionDePago(){}

    public ConfiguracionDePago(MetodoDePago metodoDePago, BigDecimal porcentajeRecargo, Boolean activo) {
        this.metodoDePago = metodoDePago;
        this.porcentajeRecargo = porcentajeRecargo;
        this.activo = activo;
    }

    
    public MetodoDePago getMetodoDePago() {
        return metodoDePago;
    }

    public void setMetodoDePago(MetodoDePago metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public BigDecimal getPorcentajeRecargo() {
        return porcentajeRecargo;
    }

    public void setPorcentajeRecargo(BigDecimal porcentajeRecargo) {
        this.porcentajeRecargo = porcentajeRecargo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public void cambiarOAgregarRecargo(BigDecimal recargo){
        porcentajeRecargo = recargo;
    }

    public void darDeBajaAlMetodo(){
        activo = Boolean.FALSE;
    }

    public void activarMetodo(){
        activo = Boolean.TRUE;
    }
}
