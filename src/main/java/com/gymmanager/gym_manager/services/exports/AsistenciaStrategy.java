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

        List<Asistencia> asistencias = (fecha != null)
        ? asistenciaRepository.findByFecha(fecha)
        : asistenciaRepository.findAll();
        
        System.out.println("Atributos asistencia: " + request.getAtributos());
        System.out.println("Total asistencias: " + asistencias.size());
        
        for (java.lang.reflect.Field f : Asistencia.class.getDeclaredFields()) {
            System.out.println("Campo real asistencia: " + f.getName());
        }

        List<Map<String, Object>> filas = new ArrayList<>();

        for (Asistencia asistencia : asistencias) {

            System.out.println("Procesando asistencia: " + asistencia.getIdAsistencia());

            List<Map<String, Object>> filasAsistencia =
                    ExportMapper.mapearEntidad(asistencia, request.getAtributos());

            System.out.println("Filas generadas: " + filasAsistencia);

            filas.addAll(filasAsistencia);
        }

        System.out.println("Total filas asistencia: " + filas.size());

        return filas;
    }
}
