package com.gymmanager.gym_manager.entity.dto;
import java.math.BigDecimal;

/**
 * DTO de sólo lectura para mostrar en la vista de configuración
 * cada método de pago junto con su recargo activo.
 *
 * No es una entidad: se construye en el service y se pasa al modelo.
 */
public class MetodoPagoConfigDTO {

    /** ID de MetodoDePago — se usa para el endpoint DELETE */
    private final Integer idMetodo;

    /** ID de ConfiguracionDePago activa — se usa para actualizar el recargo en el POST */
    private final Integer idConfiguracion;

    private final String nombre;
    private final BigDecimal recargo;

    public MetodoPagoConfigDTO(Integer idMetodo, Integer idConfiguracion,
                               String nombre, BigDecimal recargo) {
        this.idMetodo        = idMetodo;
        this.idConfiguracion = idConfiguracion;
        this.nombre          = nombre;
        this.recargo         = recargo != null ? recargo : BigDecimal.ZERO;
    }

    public Integer getIdMetodo()        { return idMetodo; }
    public Integer getIdConfiguracion() { return idConfiguracion; }
    public String  getNombre()          { return nombre; }
    public BigDecimal getRecargo()      { return recargo; }
}