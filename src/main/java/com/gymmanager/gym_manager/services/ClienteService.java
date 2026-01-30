package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
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
    public Cliente registrarClienteEInscribir(Cliente cliente, List<Integer> idActividades, LocalDate fechaInicio){
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

        actividadClienteService.inscribirCliente(clienteGuardado, actividad,fechaInicio); 
    }

        return clienteGuardado;
    }

    @Transactional
    public void eliminarCliente(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // AGREGADO HOY 29/1
        clienteRepository.delete(cliente);

        // // Dar de baja todas las inscripciones activas
        // cliente.getInscripciones().forEach(inscripcion -> {
        //     if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA) {
        //         inscripcion.darseDeBaja();
        //     }
        // });
    }
public void actualizarCliente(Cliente clienteForm, List<Integer> idsActividadesForm, LocalDate fechaInicioForm) {
    // 1. Buscamos al cliente original en la BD
    Cliente clienteDb = clienteRepository.findById(clienteForm.getIdCliente())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    // 2. Actualizamos datos personales básicos
    clienteDb.setNombre(clienteForm.getNombre());
    clienteDb.setApellido(clienteForm.getApellido());
    clienteDb.setDni(clienteForm.getDni());
    clienteDb.setTelefono(clienteForm.getTelefono());
    // clienteDb.setObservaciones(clienteForm.getObservaciones());

    // 3. MANEJO DE ACTIVIDADES (La parte crítica)
    
    // Lista de IDs que vienen del formulario (si es null, creamos lista vacía para evitar errores)
    List<Integer> idsNuevos = (idsActividadesForm != null) ? idsActividadesForm : new ArrayList<>();

    // A. DETECTAR LO QUE SE QUITÓ (Estaba Activo en BD, pero no vino en el Form)
    // Recorremos una copia de las inscripciones para evitar errores de concurrencia
    List<ActividadCliente> inscripcionesActuales = new ArrayList<>(clienteDb.getInscripciones());
    
    for (ActividadCliente inscripcion : inscripcionesActuales) {
        // Si la inscripción está ACTIVA y su ID de actividad NO está en la lista nueva...
        if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA 
            && !idsNuevos.contains(inscripcion.getActividad().getIdActividad())) {
            
            // ... Llamamos a tu servicio de bajas para mantener la lógica correcta
            actividadClienteService.darseDeBaja(clienteDb, inscripcion.getActividad());
        }
    }

    // B. DETECTAR LO QUE SE AGREGÓ (Vino en el Form, pero no estaba Activo en BD)
    for (Integer idActividadNueva : idsNuevos) {
        boolean yaEstaActivo = clienteDb.getInscripciones().stream()
                .anyMatch(i -> i.getActividad().getIdActividad().equals(idActividadNueva) 
                               && i.getEstado() == EstadoInscripcion.ACTIVA);

        if (!yaEstaActivo) {
            // Buscamos la actividad entidad
            Actividad actividad = actividadRepository.findById(idActividadNueva)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividadNueva));
            
            // Usamos la fecha del formulario. Si viene nula, usamos HOY.
            LocalDate fechaAlta = (fechaInicioForm != null) ? fechaInicioForm : LocalDate.now();

            // Llamamos a tu servicio de inscripción (esto genera el pago y guarda)
            actividadClienteService.inscribirCliente(clienteDb, actividad, fechaAlta);
        }
    }

    // 4. Guardamos los cambios del cliente (datos personales)
    clienteRepository.save(clienteDb);
}
}
