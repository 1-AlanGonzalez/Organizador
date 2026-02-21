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
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Service
public class ClienteService {
private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadClienteService actividadClienteService;
    private final PagoService pagoService;
    private final ClienteActividadRepository clienteActividadRepository;
    public ClienteService(
        PagoService pagoService,
        ClienteRepository clienteRepository,
        ActividadRepository actividadRepository,
        ActividadClienteService actividadClienteService,
        ClienteActividadRepository clienteActividadRepository,
        PagoRepository pagoRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.actividadClienteService = actividadClienteService;
        this.pagoService = pagoService;
        this.clienteActividadRepository = clienteActividadRepository;
    }
    
    @Transactional
    public void guardarOActualizarCliente(
            Cliente cliente, 
            List<Integer> idActividades, 
            LocalDate fechaInicio, 
            TipoDeCobro tipoDeCobro,
            Boolean registrarPago,
            Double montoAbonado,
            MetodoDePago metodoPago,
            String observacionPago) {

        if (esEdicion(cliente)) {
            validarEdicion(cliente);
            actualizarCliente(cliente, idActividades, fechaInicio, tipoDeCobro);
        } else {
            validarAlta(cliente, fechaInicio, tipoDeCobro, idActividades);

            validarCuposDisponibles(idActividades);
            Cliente clienteGuardado = registrarClienteEInscribir(cliente, idActividades, fechaInicio, tipoDeCobro);

            if (!Boolean.TRUE.equals(registrarPago)) {return;}

            if (Boolean.TRUE.equals(registrarPago)) {
                validarDatosPago(montoAbonado, metodoPago);
            }
            if (Boolean.TRUE.equals(registrarPago)) {
                for (ActividadCliente inscripcion : clienteGuardado.getInscripciones()) {
                    if (inscripcion.getEstado() == EstadoInscripcion.ACTIVA) {

                        pagoService.procesarPago(
                            inscripcion.getIdActividadCliente(),
                            metodoPago.getIdMetodoDePago(),
                            observacionPago
                        );
                    }
                }
            }
        }
    }
    // Método para validar los cupos antes de guardar un cliente
    private void validarCuposDisponibles(List<Integer> idActividades) {
        // Validación 1.
        if (idActividades == null || idActividades.isEmpty()) {
            return; // nada que validar
        }
        // Validación 2.
        // Itero en cada actividad.
        for (Integer idActividad : idActividades) {
            Actividad actividad = actividadRepository.findById(idActividad)
                    .orElseThrow(() -> 
                        new IllegalArgumentException("Actividad no encontrada ID: " + idActividad)
                    );

            int inscriptosActuales =
                    clienteActividadRepository.countByActividadAndEstado(
                            actividad,
                            EstadoInscripcion.ACTIVA
                    );

            if (inscriptosActuales >= actividad.getCupoMaximo()) {
                throw new IllegalArgumentException(
                    "No hay cupos disponibles para la actividad: " + actividad.getNombre()
                );
            }
        }
    }
    private void validarDatosPago(Double monto, MetodoDePago metodoPago) {
        if (monto == null || monto <= 0) {
            throw new RuntimeException("El monto abonado debe ser mayor a 0.");
        }
        if (metodoPago == null) {
            throw new RuntimeException("Debe seleccionar un método de pago.");
        }
    }

    // --- MÉTODOS EXISTENTES (validarAlta, esEdicion, validarEdicion, etc.) ---
    // (Mantenlos tal cual los tenías en tu código original)
    
    private void validarAlta(Cliente cliente, LocalDate fechaInicio, TipoDeCobro tipoDeCobro, List<Integer> idActividades) {
        if (fechaInicio == null) throw new RuntimeException("La fecha de inicio es obligatoria.");
        if (tipoDeCobro == null) throw new RuntimeException("Debe seleccionar un tipo de cobro.");
        if (idActividades == null || idActividades.isEmpty()) throw new RuntimeException("Debe seleccionar al menos una actividad.");
        if (clienteRepository.existsByDni(cliente.getDni())) throw new RuntimeException("Ya existe un cliente con ese DNI.");
    }

    private boolean esEdicion(Cliente cliente) {
        return cliente.getIdCliente() != null && cliente.getIdCliente() > 0;
    }

    private void validarEdicion(Cliente cliente) {
        if (!clienteRepository.existsById(cliente.getIdCliente())) {
            throw new RuntimeException("El cliente que intenta editar no existe.");
        }
    }

    @Transactional
    public Cliente registrarClienteEInscribir(Cliente cliente, List<Integer> idActividades, LocalDate fechaInicio, TipoDeCobro tipoDeCobro) {
        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new RuntimeException("El cliente ya existe.");
        }
        
        Cliente clienteGuardado = clienteRepository.save(cliente);
        
        for (Integer idActividad : idActividades) {
            Actividad actividad = actividadRepository.findById(idActividad)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividad));
            
            actividadClienteService.inscribirCliente(clienteGuardado, actividad, fechaInicio, tipoDeCobro);
        }
        
        return clienteGuardado;
    }


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
        return clienteRepository.findById(idCliente).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
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
        
    private void inscribirNueva(Cliente cliente, Actividad actividad, LocalDate fechaInicio, TipoDeCobro tipoDeCobro) {
            LocalDate fechaAlta = (fechaInicio != null) ? fechaInicio : LocalDate.now();
            actividadClienteService.inscribirCliente(cliente, actividad, fechaAlta, tipoDeCobro);
        }

    @Transactional
    public void eliminarCliente(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    
        clienteRepository.delete(cliente);
    }
}
