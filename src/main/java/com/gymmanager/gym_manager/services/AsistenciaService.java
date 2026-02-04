package com.gymmanager.gym_manager.services;

import java.time.LocalDate;


import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

import jakarta.transaction.Transactional;

@Service
public class AsistenciaService {

    private final ClienteActividadRepository actividadClienteRepository;
    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaService(
            ClienteActividadRepository actividadClienteRepository,
            AsistenciaRepository asistenciaRepository
    ) {
        this.actividadClienteRepository = actividadClienteRepository;
        this.asistenciaRepository = asistenciaRepository;
    }


    /**
     * Registra una asistencia para una inscripción (ActividadCliente)
     * en una fecha determinada.
     */

    @Transactional
    public void registrarAsistencia(Integer idActividadCliente,LocalDate fecha,boolean presente) {
        // Buscamos la inscripción (ActividadCliente)
        // Si no existe, se corta el proceso
        ActividadCliente ac = actividadClienteRepository.findById(idActividadCliente).orElseThrow();

        // Delegamos la lógica de dominio a la entidad
        // - valida que no esté dada de baja
        // - valida que no exista asistencia para esa fecha
        // - crea y asocia la nueva Asistencia
        ac.tomarAsistencia(fecha, presente);

        // Recuperamos la asistencia recién creada (queda en memoria dentro del Set<Asistencia>)
        // reduce Dame el último elemento del conjunto
        Asistencia asistencia = ac.getAsistencias().stream().reduce((first, second) -> second).orElseThrow();

        
        asistenciaRepository.save(asistencia);
    }
}
