package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Actividad;

import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.ActividadRepository;

@Component
public class ActividadExportStrategy implements ExportStrategy {
    private final ActividadRepository actividadRepository;

    public ActividadExportStrategy(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "actividad";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {

        List<Actividad> actividades = actividadRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Actividad actividad : actividades) {

            List<Map<String, Object>> filasAsistencia =
                    ExportMapper.mapearEntidad(actividad, request.getAtributos());

            filas.addAll(filasAsistencia);
        }

        return filas;
    }
}

