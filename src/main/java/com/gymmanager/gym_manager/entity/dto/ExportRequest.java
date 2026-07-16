package com.gymmanager.gym_manager.entity.dto;

import java.time.LocalDate;
import java.util.List;



// Cuando el usuario haga click en "Exportar", el backend necesita saber:

// Qué entidades eligió

// Qué atributos de cada entidad eligió

// Si eligió fecha o no

// Qué modo quiere (MULTIHOJA o COMBINADO)

// Entonces necesitamos un objeto que represente eso.


public class ExportRequest {

    private LocalDate fecha; // puede ser null

    private String mes; // yyyy-MM; se aplica exclusivamente a pagos/ingresos

    private List<EntidadRequestDTO> entidades;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public List<EntidadRequestDTO> getEntidades() {
        return entidades;
    }

    public void setEntidades(List<EntidadRequestDTO> entidades) {
        this.entidades = entidades;
    }
}
