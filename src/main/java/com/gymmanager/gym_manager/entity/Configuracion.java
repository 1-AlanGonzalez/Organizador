package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;

    public class Configuracion {
    private String nombreGimnasio;
    private String direccion;
    private String telefono;
    private String mensajeTicket;
    private BigDecimal recargoTarjetaCredito;
    private BigDecimal recargoTarjetaDebito;
    private BigDecimal interesMora;
    private Integer diasGracia;
    private Boolean bloquearMorososAutomaticamente;

    
    public Configuracion() {
    }
    
    public String getNombreGimnasio() {
        return nombreGimnasio;
    }
    public void setNombreGimnasio(String nombreGimnasio) {
        this.nombreGimnasio = nombreGimnasio;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getMensajeTicket() {
        return mensajeTicket;
    }
    public void setMensajeTicket(String mensajeTicket) {
        this.mensajeTicket = mensajeTicket;
    }
    public BigDecimal getRecargoTarjetaCredito() {
        return recargoTarjetaCredito;
    }
    public void setRecargoTarjetaCredito(BigDecimal recargoTarjetaCredito) {
        this.recargoTarjetaCredito = recargoTarjetaCredito;
    }
    public BigDecimal getRecargoTarjetaDebito() {
        return recargoTarjetaDebito;
    }
    public void setRecargoTarjetaDebito(BigDecimal recargoTarjetaDebito) {
        this.recargoTarjetaDebito = recargoTarjetaDebito;
    }
    public BigDecimal getInteresMora() {
        return interesMora;
    }
    public void setInteresMora(BigDecimal interesMora) {
        this.interesMora = interesMora;
    }
    public Integer getDiasGracia() {
        return diasGracia;
    }
    public void setDiasGracia(Integer diasGracia) {
        this.diasGracia = diasGracia;
    }
    public Boolean getBloquearMorososAutomaticamente() {
        return bloquearMorososAutomaticamente;
    }
    public void setBloquearMorososAutomaticamente(Boolean bloquearMorososAutomaticamente) {
        this.bloquearMorososAutomaticamente = bloquearMorososAutomaticamente;
    }


    
}
