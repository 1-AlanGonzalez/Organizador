package com.gymmanager.gym_manager.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

import jakarta.transaction.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadClienteService actividadClienteService;

    public ClienteService(
        ClienteRepository clienteRepository,
        ActividadRepository actividadRepository,
        ActividadClienteService actividadClienteService
    ) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.actividadClienteService = actividadClienteService;
    }

    /* Solo registra al cliente y valida si existe */
    @Transactional /* transactional quiere decir:Todo lo que pase acá adentro es una sola operación- Si algo falla, volvé todo atrás */
    public Cliente registrarClienteEInscribir(Cliente cliente, List<Integer> idActividades){
    Boolean yaEstaElDni = clienteRepository.existsByDni(cliente.getDni());

    if (yaEstaElDni) {
        throw new RuntimeException("El cliente que quiere registrar ya está cargado en el sistema");
    }

    Cliente clienteGuardado = clienteRepository.save(cliente);

    if (idActividades == null || idActividades.isEmpty()) {
        throw new RuntimeException("Debe seleccionar al menos una actividad");
    }

    for (Integer idActividad : idActividades) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        actividadClienteService.inscribirCliente(clienteGuardado, actividad); 
    }

        return clienteGuardado;
    }

    @Transactional
    public void eliminarCliente(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Dar de baja todas las inscripciones activas
        cliente.getInscripciones().forEach(inscripcion -> {
            if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA) {
                inscripcion.darseDeBaja();
            }
        });
    }

}
