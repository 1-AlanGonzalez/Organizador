package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/* Esta entidad tendra la logica de los pagos
 * por transferencia / tarjeta / efectivo
 */

@Entity
public class ConfiguracionDePago {

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private MetodoDePago metodoDePago;

    private BigDecimal procentajeRecargo;

    public MetodoDePago getMetodoDePago() {
        return metodoDePago;
    }

    public void setMetodoDePago(MetodoDePago metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public BigDecimal getProcentajeRecargo() {
        return procentajeRecargo;
    }

    public void setProcentajeRecargo(BigDecimal procentajeRecargo) {
        this.procentajeRecargo = procentajeRecargo;
    }


    

}
