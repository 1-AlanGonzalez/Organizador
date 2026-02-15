package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.dto.ReporteAsistenciaDTO;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

import jakarta.transaction.Transactional;

@Service
public class AsistenciaService {

    private final ClienteActividadRepository clienteActividadRepository;
    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaService(
            ClienteActividadRepository clienteActividadRepository,
            AsistenciaRepository asistenciaRepository
    ) {
        this.clienteActividadRepository = clienteActividadRepository;
        this.asistenciaRepository = asistenciaRepository;
    }


    /**
     * Registra una asistencia para una inscripción (ActividadCliente)
     * en una fecha determinada.
     */

    // @Transactional
    // public void registrarAsistencia(Integer idActividadCliente,LocalDate fecha,boolean presente) {
    //     // Buscamos la inscripción (ActividadCliente)
    //     // Si no existe, se corta el proceso
    //     ActividadCliente ac = clienteActividadRepository.findById(idActividadCliente).orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

    //     // Delegamos la lógica de dominio a la entidad
    //     // - valida que no esté dada de baja
    //     // - valida que no exista asistencia para esa fecha
    //     // - crea y asocia la nueva Asistencia
    //     ac.tomarAsistencia(fecha, presente);

    //     // Recuperamos la asistencia recién creada (queda en memoria dentro del Set<Asistencia>)
    //     // reduce Dame el último elemento del conjunto
    //     Asistencia asistencia = ac.getAsistencias().stream().reduce((first, second) -> second).orElseThrow();
        
    //     asistenciaRepository.save(asistencia);
    // }
    @Transactional
    public void registrarAsistencia(Integer idActividadCliente, LocalDate fecha, boolean presente) {
        ActividadCliente ac = clienteActividadRepository.findById(idActividadCliente)
            .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        Asistencia asistenciaExistente = asistenciaRepository.findByFechaAndActividadCliente(fecha, ac);

        if (presente) {
            if (asistenciaExistente != null) {
                return;
            } else {
                ac.tomarAsistencia(fecha, true);
                Asistencia nuevaAsistencia = ac.getAsistencias().stream()
                        .filter(a -> a.getFecha().equals(fecha))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Error al crear asistencia"));
                asistenciaRepository.save(nuevaAsistencia);
            }
        } else {
            if (asistenciaExistente != null) {
                asistenciaRepository.delete(asistenciaExistente);
                ac.getAsistencias().remove(asistenciaExistente);
            }
        }
    }
    
    public List<ReporteAsistenciaDTO> generarReporteDiario(LocalDate fecha, Integer idActividadFiltro) {
    // 1. Buscamos TODAS las inscripciones activas (Gente que paga)
    List<ActividadCliente> inscripciones = clienteActividadRepository.findByEstado(EstadoInscripcion.ACTIVA);

    // 2. Filtramos por actividad si el usuario seleccionó una en el select
    if (idActividadFiltro != null) {
        inscripciones = inscripciones.stream()
            .filter(i -> i.getActividad().getIdActividad().equals(idActividadFiltro))
            .collect(Collectors.toList());
    }

    // 3. Convertimos esa lista en el DTO, verificando si vino o no
    return inscripciones.stream().map(inscripcion -> {
        
        // Verificamos si existe asistencia para ESTA fecha y ESTA inscripción
        boolean presente = asistenciaRepository.existsByFechaAndActividadCliente(fecha, inscripcion);

        return new ReporteAsistenciaDTO(
            inscripcion.getCliente().getNombre(),
            inscripcion.getCliente().getApellido(),
            inscripcion.getActividad().getNombre(),
            fecha,
            presente
        );
    }).collect(Collectors.toList());
}
}
