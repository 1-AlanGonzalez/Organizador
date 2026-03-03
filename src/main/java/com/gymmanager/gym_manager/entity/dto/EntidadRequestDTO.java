package com.gymmanager.gym_manager.entity.dto;

import java.util.List;


public class EntidadRequestDTO {
    private String nombre; 

    private List<String> atributos;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getAtributos() {
        return atributos;
    }

    public void setAtributos(List<String> atributos) {
        this.atributos = atributos;
    }
}
