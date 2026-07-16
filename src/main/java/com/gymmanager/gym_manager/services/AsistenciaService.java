package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.ReporteAsistenciaDTO;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

import jakarta.transaction.Transactional;

@Service
public class AsistenciaService {

    private final ClienteActividadRepository clienteActividadRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final SecurityUtils securityUtils;

    public AsistenciaService(
            ClienteActividadRepository clienteActividadRepository,
            AsistenciaRepository asistenciaRepository,
            SecurityUtils securityUtils
    ) {
        this.clienteActividadRepository = clienteActividadRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public void guardarAsistencia(LocalDate fecha, List<Integer> presentesIds, List<Integer> todosLosIds){
        if (presentesIds == null) presentesIds = new ArrayList<>();
        if (todosLosIds  == null) todosLosIds  = new ArrayList<>();

        for (Integer id : todosLosIds) {
            boolean estaPresente = presentesIds.contains(id);
            this.registrarAsistencia(id, fecha, estaPresente);
        }
    }

    @Transactional
    public void registrarAsistencia(Integer idActividadCliente, LocalDate fecha, boolean presente) {
        Usuario usuario = securityUtils.getUsuarioActual();

        ActividadCliente ac = clienteActividadRepository
                .findByIdActividadClienteAndCliente_Usuario(
                        idActividadCliente,
                        usuario
                )
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
    
    public List<ReporteAsistenciaDTO> generarReporteDiario(LocalDate fecha, Integer idActividadFiltro, Usuario usuario) {
    // 1. Buscamos TODAS las inscripciones activas (Gente que paga)
    List<ActividadCliente> inscripciones = clienteActividadRepository.findByEstadoAndCliente_Usuario(EstadoInscripcion.ACTIVA, usuario);

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

    public Page<ReporteAsistenciaDTO> generarReporteDiarioPaginado(
            LocalDate fecha, Integer idActividad, String texto, Usuario usuario, Pageable pageable) {
        return clienteActividadRepository.buscarActivas(
                EstadoInscripcion.ACTIVA, usuario, texto, idActividad, pageable)
                .map(inscripcion -> new ReporteAsistenciaDTO(
                        inscripcion.getCliente().getNombre(),
                        inscripcion.getCliente().getApellido(),
                        inscripcion.getActividad().getNombre(),
                        fecha,
                        asistenciaRepository.existsByFechaAndActividadCliente(fecha, inscripcion)));
    }

    public LocalDate parsearFecha(String fecha) {
        return (fecha != null && !fecha.isBlank()) ? LocalDate.parse(fecha) : LocalDate.now();
    }

    public List<Asistencia> listarAsistenciasDeUsuario() {
        return asistenciaRepository.findByActividadCliente_Cliente_Usuario(securityUtils.getUsuarioActual());
    }
}
