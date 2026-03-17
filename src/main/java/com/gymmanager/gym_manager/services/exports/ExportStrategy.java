package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;

public interface ExportStrategy {
    //Devuelve el nombre de la entidad que maneja


    //Logica de exportacion por entidad
    List<Map<String,Object>> exportar(EntidadRequestDTO entidad, LocalDate fecha, Usuario usuario);

    String getNombreEntidad();
}
