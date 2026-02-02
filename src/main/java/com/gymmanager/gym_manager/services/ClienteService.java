package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    
    public ClienteService(
        ClienteRepository clienteRepository,
        ActividadRepository actividadRepository,
        ActividadClienteService actividadClienteService,
        ClienteActividadRepository clienteActividadRepository
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
// Este era el método que utilizaba para actualizar cliente. Como era un embole decidí separarlo en SUBCODIGOS todas las partes para hacer un código más entendible y encontrar el error
// @Transactional 
//     public void actualizarCliente(Cliente clienteForm, List<Integer> idsActividadesForm, LocalDate fechaInicioForm, TipoDeCobro tipoDeCobro) {
        
//         // 1. Buscamos al cliente original
//         Cliente clienteDb = clienteRepository.findById(clienteForm.getIdCliente())
//                 .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

//         // 2. Actualizamos datos personales
//         clienteDb.setNombre(clienteForm.getNombre());
//         clienteDb.setApellido(clienteForm.getApellido());
//         clienteDb.setDni(clienteForm.getDni());
//         clienteDb.setTelefono(clienteForm.getTelefono());
//         clienteDb.setObservaciones(clienteForm.getObservaciones());

//         // 3. MANEJO DE ACTIVIDADES
//         List<Integer> idsNuevos = (idsActividadesForm != null) ? idsActividadesForm : new ArrayList<>();

//         // A. DETECTAR LO QUE SE QUITÓ (BAJAS)
//         // Creamos una copia de la lista para poder iterar sin errores de concurrencia
//         List<ActividadCliente> inscripcionesActuales = new ArrayList<>(clienteDb.getInscripciones());

//         for (ActividadCliente inscripcion : inscripcionesActuales) {
//             // Si la inscripción está ACTIVA y su ID de actividad NO está en la nueva lista que viene del form...
//             if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA 
//                 && !idsNuevos.contains(inscripcion.getActividad().getIdActividad())) {
                
//                 // --- CAMBIO CLAVE AQUÍ ---
//                 // Usamos el método de TU entidad.
//                 // Como 'inscripcion' es el objeto EXACTO que está dentro de 'clienteDb',
//                 // al modificarlo aquí, Hibernate sabe que es parte del grafo de objetos a guardar.
                
//                 clienteDb.darseDeBajaAInscripcion(inscripcion);
//             }
//         }

//         // B. DETECTAR LO QUE SE AGREGÓ O SE ACTUALIZÓ (ALTAS Y CAMBIOS DE PLAN)
//         for (Integer idActividadNueva : idsNuevos) {
            
//             Actividad actividad = actividadRepository.findById(idActividadNueva)
//                     .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividadNueva));

//             Optional<ActividadCliente> inscripcionExistente = clienteDb.getInscripciones().stream()
//                     .filter(i -> i.getActividad().getIdActividad().equals(idActividadNueva) 
//                               && i.getEstado() == EstadoInscripcion.ACTIVA)
//                     .findFirst();

//             if (inscripcionExistente.isPresent()) {
//                 // CASO 1: YA EXISTE -> ACTUALIZAR SI CAMBIÓ TIPO COBRO
//                 ActividadCliente inscripcion = inscripcionExistente.get();
//                 if (inscripcion.getTipoDeCobro() != tipoDeCobro) {
//                     inscripcion.setTipoDeCobro(tipoDeCobro);
                    
//                     if (tipoDeCobro == TipoDeCobro.DIARIO) {
//                         inscripcion.setCosto(actividad.getPrecioDiario() != null ? actividad.getPrecioDiario() : actividad.getPrecio());
//                     } else {
//                         inscripcion.setCosto(actividad.getPrecio());
//                     }
//                     // No hace falta llamar a clienteActividadRepository.save(inscripcion) explícitamente
//                     // porque el CascadeType.ALL del cliente se encargará al final.
//                 }

//             } else {
//                 // CASO 2: ES NUEVA -> INSCRIBIR
//                 LocalDate fechaAlta = (fechaInicioForm != null) ? fechaInicioForm : LocalDate.now();
//                 actividadClienteService.inscribirCliente(clienteDb, actividad, fechaAlta, tipoDeCobro);
//             }
//         }

//         clienteRepository.save(clienteDb);
//     }

// Motivo por el cual cambié el método:
// Al inscribir el cliente en por ejemplo BOXEO y GYM: en la DB el cliente tiene 2 inscripciones ACTIVAS.
// Al editar el cliente y darle de baja una inscripción (por ejemplo BOXEO) en la DB el cliente tiene 2 inscripciones, una ACTIVA y una BAJA.
// Entonces con el método anterior, si queríamos volver a poner en ACTIVA la actividad BAJA no era posible porque no estaba manejando bien todos los posibles casos.

// Actualizar el cliente.
// Como parámetro traemos: 
// los datos del cliente que vienen del formulario panel-cliente llegados desde el endpoint /editar/{id}
// Las ID de las nuevas actividades 
// La nueva fecha 
// el tipo de cobro (todo esto por si lo cambia)  
@Transactional
    public void actualizarCliente(
            Cliente clienteForm,
            List<Integer> idsActividadesForm,
            LocalDate fechaInicioForm,
            TipoDeCobro tipoDeCobro
    ) {
        // Obtengo los datos del cliente antes de ser editados (el que se encuentra en la base de datos)
        Cliente clienteDb = obtenerCliente(clienteForm.getIdCliente());
        // Actualizo sus datos con los nuevos del formulario
        actualizarDatosPersonales(clienteDb, clienteForm);
        // Normalizamos las IDS:
        // Esto quiere decir que dejamos los datos en una forma segura y consistente para poder trabajar con ellos sin problemas
        List<Integer> idsNuevos = normalizarIds(idsActividadesForm);
        // Damos de baja las inscripciones que fueron eliminadas
        darDeBajaInscripcionesQuitadas(clienteDb, idsNuevos);
        // Damos de alta las inscripciones nuevas con su nueva fecha y tipo de cobro.
        procesarAltasYReactivaciones(clienteDb, idsNuevos, fechaInicioForm, tipoDeCobro);
        // Guardamos el cliente en la DB
        clienteRepository.save(clienteDb);
    }

    // SUBMÉTODOS 


    private Cliente obtenerCliente(Integer idCliente) {
    return clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    private void actualizarDatosPersonales(Cliente db, Cliente form) {
        db.setNombre(form.getNombre());
        db.setApellido(form.getApellido());
        db.setDni(form.getDni());
        db.setTelefono(form.getTelefono());
        db.setObservaciones(form.getObservaciones());
    }

    // Normalizar IDS -> Si la lista de IDS nuevos es vacía (es decir, se dieron de baja todas las inscripciones) devolvemos un array vacío
    // Esto sirve y hace que no se rompa el programa debido a que antes hacía un null.contains(...) y ahora hace [].contains

    private List<Integer> normalizarIds(List<Integer> ids) {
        return (ids != null) ? ids : new ArrayList<>();
    }

    private void darDeBajaInscripcionesQuitadas(Cliente cliente, List<Integer> idsNuevos) {
        List<ActividadCliente> actuales = new ArrayList<>(cliente.getInscripciones());

        for (ActividadCliente insc : actuales) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVA
                    && !idsNuevos.contains(insc.getActividad().getIdActividad())) {

                cliente.darseDeBajaAInscripcion(insc);
            }
        }
    }

    private void procesarAltasYReactivaciones(
            Cliente cliente,
            List<Integer> idsNuevos,
            LocalDate fechaInicio,
            TipoDeCobro tipoDeCobro
    ) {
        // Itero cada actividad NUEVA (con los ids normalizados)
        for (Integer idActividad : idsNuevos) {

            Actividad actividad = actividadRepository.findById(idActividad)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividad));
            
            // EXISTENTE = True solo si la actividad nueva ya existía en el cliente
            Optional<ActividadCliente> existente = cliente.getInscripciones().stream()
                    .filter(i -> i.getActividad().getIdActividad().equals(idActividad))
                    .findFirst();
            if (existente.isPresent()) {
                manejarInscripcionExistente(existente.get(), actividad, tipoDeCobro);
            } else {
                inscribirNueva(cliente, actividad, fechaInicio, tipoDeCobro);
            }
        }
    }
    private void manejarInscripcionExistente(
            ActividadCliente inscripcion,
            Actividad actividad,
            TipoDeCobro tipoDeCobro
    ) {
        // Si la actividad ya existe en el cliente pero está dada de BAJA
        if (inscripcion.getEstado() == EstadoInscripcion.BAJA) {
            inscripcion.setEstado(EstadoInscripcion.ACTIVA);
            inscripcion.setFechaDeInscripcion(LocalDate.now());
        }

        // También actualizamos el tipoDeCobro en caso de haber cambiado.
        if (inscripcion.getTipoDeCobro() != tipoDeCobro) {
            inscripcion.setTipoDeCobro(tipoDeCobro);
        }
        // Y actualizamos el costo por si es MENSUAL o DIARIO
        actualizarCosto(inscripcion, actividad);
    }
    private void actualizarCosto(ActividadCliente inscripcion, Actividad actividad) {
        if (inscripcion.getTipoDeCobro() == TipoDeCobro.DIARIO) {
            inscripcion.setCosto(
                // Acá por si ocurre algún error de por medio, siempre devuelvo la mensualidad en caso de no existir el precio diario (a analizar)
                    actividad.getPrecioDiario() != null
                            ? actividad.getPrecioDiario()
                            : actividad.getPrecio()
            );
        } else {
            // Si no, devuelvo el precio mensual
            inscripcion.setCosto(actividad.getPrecio());
        }
    }
    
    private void inscribirNueva(
            Cliente cliente,
            Actividad actividad,
            LocalDate fechaInicio,
            TipoDeCobro tipoDeCobro
    ) {
        LocalDate fechaAlta = (fechaInicio != null) ? fechaInicio : LocalDate.now();
        actividadClienteService.inscribirCliente(cliente, actividad, fechaAlta, tipoDeCobro);
    }
}
