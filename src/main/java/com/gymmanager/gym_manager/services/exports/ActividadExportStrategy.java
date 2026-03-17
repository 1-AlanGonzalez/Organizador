package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

@Component
public class ActividadExportStrategy implements ExportStrategy {
    private final ActividadRepository actividadRepository;
    private final ClienteActividadRepository clienteActividadRepository;

    public ActividadExportStrategy(ActividadRepository actividadRepository,
                                   ClienteActividadRepository clienteActividadRepository) {
        this.actividadRepository = actividadRepository;
        this.clienteActividadRepository = clienteActividadRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "actividad";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha, Usuario usuario) {
        List<Actividad> actividades = actividadRepository.findByUsuario(usuario);
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Actividad actividad : actividades) {
            List<String> atributosMapper = request.getAtributos().stream()
                .filter(a -> !a.equals("cuposActuales") && !a.equals("activos"))
                .collect(java.util.stream.Collectors.toList());

            List<Map<String, Object>> filasActividad =
                    ExportMapper.mapearEntidad(actividad, atributosMapper);

            if (request.getAtributos().contains("cuposActuales")) {
                Integer activos = clienteActividadRepository
                    .countByActividadAndEstado(actividad, EstadoInscripcion.ACTIVA);
                if (filasActividad.isEmpty()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("Cupos Actuales", activos);
                    filasActividad.add(fila);
                } else {
                    filasActividad.forEach(fila -> fila.put("Cupos Actuales", activos));
                }
            }
            filas.addAll(filasActividad);
        }
        return filas;
    }
}

