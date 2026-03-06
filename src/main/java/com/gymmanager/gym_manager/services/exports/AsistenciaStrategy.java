package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;

@Component
public class AsistenciaStrategy implements ExportStrategy {

    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaStrategy(AsistenciaRepository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "asistencia";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {

        List<Asistencia> asistencias = asistenciaRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Asistencia asistencia : asistencias) {

            List<Map<String, Object>> filasAsistencia =
                    ExportMapper.mapearEntidad(asistencia, request.getAtributos());

            filas.addAll(filasAsistencia);
        }

        return filas;
    }
}
