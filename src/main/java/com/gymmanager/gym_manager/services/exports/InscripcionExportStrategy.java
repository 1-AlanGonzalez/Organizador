package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

@Service
public class InscripcionExportStrategy implements ExportStrategy{
    private final ClienteActividadRepository actividadClienteRepository;

    public InscripcionExportStrategy(ClienteActividadRepository actividadClienteRepository) {
        this.actividadClienteRepository = actividadClienteRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "inscripcion";
    }

    @Override
public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {

    List<ActividadCliente> inscripciones = actividadClienteRepository.findAll();

    List<Map<String, Object>> filas = new ArrayList<>();

    for (ActividadCliente inscripcion : inscripciones) {

        List<Map<String, Object>> filasInscripcion =
                ExportMapper.mapearEntidad(inscripcion, request.getAtributos());

        filas.addAll(filasInscripcion);
    }

    return filas;
}

   
}
