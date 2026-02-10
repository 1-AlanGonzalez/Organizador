package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
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
    private final ClienteActividadRepository clienteActividadRepository;
    private final PagoRepository pagoRepository; // <--- NECESARIO AGREGAR ESTO

    public ClienteService(
        ClienteRepository clienteRepository,
        ActividadRepository actividadRepository,
        ActividadClienteService actividadClienteService,
        ClienteActividadRepository clienteActividadRepository,
        PagoRepository pagoRepository // <--- INYECTAR EN CONSTRUCTOR
    ) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.actividadClienteService = actividadClienteService;
        this.clienteActividadRepository = clienteActividadRepository;
        this.pagoRepository = pagoRepository;
    }
    
    @Transactional
    public void guardarOActualizarCliente(
            Cliente cliente, 
            List<Integer> idActividades, 
            LocalDate fechaInicio, 
            TipoDeCobro tipoDeCobro,
            Boolean registrarPago,
            Double montoAbonado,
            String metodoPagoStr,
            String observacionPago) {

        // 1. Validaciones previas
        if (esEdicion(cliente)) {
            validarEdicion(cliente);
            actualizarCliente(cliente, idActividades, fechaInicio, tipoDeCobro);
        } else {
            validarAlta(cliente, fechaInicio, tipoDeCobro, idActividades);
            // Validar datos de pago si el checkbox está marcado
            if (Boolean.TRUE.equals(registrarPago)) {
                validarDatosPago(montoAbonado, metodoPagoStr);
            }
            // 2. Guardar Cliente e Inscripciones
            Cliente clienteGuardado = registrarClienteEInscribir(cliente, idActividades, fechaInicio, tipoDeCobro);
            // 3. Registrar el Pago (si corresponde)
            if (Boolean.TRUE.equals(registrarPago)) {
                // imputarPagoInicial(clienteGuardado, idActividades, montoAbonado, metodoPagoStr, observacionPago);
                // pagoService.procesarPago(pagoPendiente, metodo, observacionPago);
            }
        }
    }

    private void validarDatosPago(Double monto, String metodoStr) {
        if (monto == null || monto <= 0) {
            throw new RuntimeException("El monto abonado debe ser mayor a 0.");
        }
        if (metodoStr == null || metodoStr.trim().isEmpty()) {
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

    // --- NUEVA LÓGICA DE PAGO ADAPTADA A TU ENTIDAD ---
    private void registrarPagoInicial(Cliente cliente, List<Integer> idActividades, Double monto, String metodoStr, String observacion) {
        
        // TU ENTIDAD PAGO REQUIERE UNA 'ActividadCliente'.
        // Buscamos una de las inscripciones activas que acabamos de crear para asignarle el pago.
        // (Generalmente la primera de la lista de actividades seleccionadas).
        
        Integer idActividadPrincipal = idActividades.get(0); // Tomamos la primera ID seleccionada

        ActividadCliente inscripcion = cliente.getInscripciones().stream()
            .filter(ac -> ac.getActividad().getIdActividad().equals(idActividadPrincipal) 
                       && ac.getEstado() == EstadoInscripcion.ACTIVA)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No se encontró la inscripción para asociar el pago."));

        Pago nuevoPago = new Pago();
        
        // Seteo de datos según tu Entidad PAGO
        nuevoPago.setActividadCliente(inscripcion); // Relación obligatoria
        nuevoPago.setFechaGeneracion(LocalDate.now());
        
        // Convertimos Double a BigDecimal como usa tu entidad
        BigDecimal montoBig = BigDecimal.valueOf(monto);
        nuevoPago.setMontoAPagar(montoBig); // Asumimos que paga lo que debe, o ajusta esto según tu lógica de deuda
        
        nuevoPago.setMetodoPago(MetodoDePago.valueOf(metodoStr)); // Convertir String a Enum
        nuevoPago.setObservaciones(observacion != null ? observacion : "Pago inicial al registrar");
        
        // Lógica de estado y vencimiento
        nuevoPago.setEstado(EstadoPago.PAGADO);
        nuevoPago.setFechaVencimiento(LocalDate.now().plusMonths(1)); // O tu lógica de vencimiento
        
        pagoRepository.save(nuevoPago);
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
