package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

import jakarta.transaction.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadClienteService actividadClienteService;
    private final ClienteActividadRepository clienteActividadRepository;
    
    public ClienteService(
        ClienteRepository clienteRepository,
        ActividadRepository actividadRepository,
        ActividadClienteService actividadClienteService,
        ClienteActividadRepository clienteActividadRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.actividadClienteService = actividadClienteService;
        this.clienteActividadRepository = clienteActividadRepository;
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
    
@Transactional // Importante para que los cambios se guarden
    public void actualizarCliente(Cliente clienteForm, List<Integer> idsActividadesForm, LocalDate fechaInicioForm, TipoDeCobro tipoDeCobro) {
        
        // 1. Buscamos al cliente original en la BD
        Cliente clienteDb = clienteRepository.findById(clienteForm.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 2. Actualizamos datos personales básicos
        clienteDb.setNombre(clienteForm.getNombre());
        clienteDb.setApellido(clienteForm.getApellido());
        clienteDb.setDni(clienteForm.getDni());
        clienteDb.setTelefono(clienteForm.getTelefono());
        // clienteDb.setObservaciones(clienteForm.getObservaciones());

        // 3. MANEJO DE ACTIVIDADES
        List<Integer> idsNuevos = (idsActividadesForm != null) ? idsActividadesForm : new ArrayList<>();

        // A. DETECTAR LO QUE SE QUITÓ (BAJAS)
        List<ActividadCliente> inscripcionesActuales = new ArrayList<>(clienteDb.getInscripciones());
        
        for (ActividadCliente inscripcion : inscripcionesActuales) {
            if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA 
                && !idsNuevos.contains(inscripcion.getActividad().getIdActividad())) {
                
                // Dar de baja
                actividadClienteService.darseDeBaja(clienteDb, inscripcion.getActividad());
            }
        }

        // B. DETECTAR LO QUE SE AGREGÓ O SE ACTUALIZÓ (ALTAS Y CAMBIOS DE PLAN)
        for (Integer idActividadNueva : idsNuevos) {
            
            // Buscamos la actividad entidad
            Actividad actividad = actividadRepository.findById(idActividadNueva)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividadNueva));

            // Verificamos si ya tiene esta inscripción ACTIVA
            Optional<ActividadCliente> inscripcionExistente = clienteDb.getInscripciones().stream()
                    .filter(i -> i.getActividad().getIdActividad().equals(idActividadNueva) 
                              && i.getEstado() == EstadoInscripcion.ACTIVA)
                    .findFirst();

            if (inscripcionExistente.isPresent()) {
                // CASO 1: YA EXISTE -> VERIFICAR SI CAMBIÓ EL TIPO DE COBRO
                ActividadCliente inscripcion = inscripcionExistente.get();
                
                if (inscripcion.getTipoDeCobro() != tipoDeCobro) {
                    // Actualizamos el tipo
                    inscripcion.setTipoDeCobro(tipoDeCobro);
                    
                    // Actualizamos el precio acorde al nuevo tipo
                    if (tipoDeCobro == TipoDeCobro.DIARIO) {
                        inscripcion.setCosto(actividad.getPrecioDiario());
                    } else {
                        inscripcion.setCosto(actividad.getPrecio());
                    }
                    
                    // Guardamos la actualización de la inscripción explícitamente
                    clienteActividadRepository.save(inscripcion);
                }

            } else {
                // CASO 2: ES NUEVA -> INSCRIBIR
                LocalDate fechaAlta = (fechaInicioForm != null) ? fechaInicioForm : LocalDate.now();
                actividadClienteService.inscribirCliente(clienteDb, actividad, fechaAlta, tipoDeCobro);
            }
        }

        // 4. Guardamos los cambios del cliente (datos personales)
        clienteRepository.save(clienteDb);
    }
}
