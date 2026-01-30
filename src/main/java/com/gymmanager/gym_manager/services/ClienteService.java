package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
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
    public Cliente registrarClienteEInscribir(Cliente cliente, List<Integer> idActividades, LocalDate fechaInicio, TipoDeCobro tipoDeCobro){
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

        actividadClienteService.inscribirCliente(clienteGuardado, actividad,fechaInicio, tipoDeCobro); 
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

    @Transactional
    public void actualizarCliente(Cliente clienteFormulario, List<Integer> idActividades, LocalDate fechaInicio, TipoDeCobro tipoDeCobro) {
        
        // 1. Buscamos y actualizamos datos personales del Cliente
        Cliente clienteExistente = clienteRepository.findById(clienteFormulario.getIdCliente())
                .orElseThrow(() -> new RuntimeException("El cliente a editar no existe."));

        // Validación de DNI duplicado
        if (!clienteExistente.getDni().equals(clienteFormulario.getDni())) {
            if (clienteRepository.existsByDni(clienteFormulario.getDni())) {
                throw new RuntimeException("El nuevo DNI ingresado ya pertenece a otro cliente.");
            }
        }

        // Actualizamos datos básicos
        clienteExistente.setNombre(clienteFormulario.getNombre());
        clienteExistente.setApellido(clienteFormulario.getApellido());
        clienteExistente.setDni(clienteFormulario.getDni());
        clienteExistente.setTelefono(clienteFormulario.getTelefono());
        
        // Evitamos nulos en la lista de actividades
        List<Integer> actividadesSeleccionadas = (idActividades != null) ? idActividades : List.of();

        

        // A) PROCESAR LO QUE VIENE DEL FORMULARIO (Altas, Reactivaciones y Cambios de Fecha)
        for (Integer idActividad : actividadesSeleccionadas) {
            
            // Buscamos si el cliente YA tiene una inscripción a esta actividad (Activa o Baja, no importa)
            ActividadCliente inscripcionExistente = clienteExistente.getInscripciones().stream()
                .filter(ins -> ins.getActividad().getIdActividad().equals(idActividad))
                .findFirst()
                .orElse(null);

            if (inscripcionExistente != null) {
                // CASO 1: YA EXISTE (Puede estar ACTIVA o BAJA)
                
                // Si estaba de BAJA, la reactivamos
                if (inscripcionExistente.getEstado() == EstadoInscripcion.BAJA) {
                    inscripcionExistente.activar();
                }
                
                // Actualizamos la fecha SIEMPRE que venga una nueva
                // NOTA: Usamos el nombre correcto de tu entidad: setFechaDeInscripcion
                if (fechaInicio != null) {
                    inscripcionExistente.setFechaDeInscripcion(fechaInicio);
                }

            } else {
                // CASO 2: NO EXISTE (Es una inscripción 100% nueva)
                Actividad actividad = actividadRepository.findById(idActividad)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividad));
                
                LocalDate fechaAlta = (fechaInicio != null) ? fechaInicio : LocalDate.now();
                
                // Usamos tu servicio existente para crear la relación limpia
                actividadClienteService.inscribirCliente(clienteExistente, actividad, fechaAlta, tipoDeCobro);
            }
        }

        // B) PROCESAR LO QUE NO VIENE (Bajas / Uncheck)
        // Recorremos las inscripciones que el cliente TIENE en base de datos
        clienteExistente.getInscripciones().forEach(inscripcion -> {
            Integer idActividadActual = inscripcion.getActividad().getIdActividad();
            
            // Si la inscripción está ACTIVA en BD, pero NO vino marcada en el formulario...
            if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA 
                && !actividadesSeleccionadas.contains(idActividadActual)) {
                
                // ... entonces el usuario la desmarcó -> DAR DE BAJA
                inscripcion.darseDeBaja();
            }
        });

        // 3. Guardamos
        clienteRepository.save(clienteExistente);
    }
}
