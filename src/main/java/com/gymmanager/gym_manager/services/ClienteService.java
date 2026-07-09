package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.entity.Usuario;
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
    private final SecurityUtils securityUtils;

    public ClienteService(
            PagoService pagoService,
            ClienteRepository clienteRepository,
            ActividadRepository actividadRepository,
            ActividadClienteService actividadClienteService,
            ClienteActividadRepository clienteActividadRepository,
            SecurityUtils securityUtils) {

        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.actividadClienteService = actividadClienteService;
        this.pagoService = pagoService;
        this.clienteActividadRepository = clienteActividadRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public void guardarOActualizarCliente(
            Cliente cliente,
            List<Integer> idActividades,
            Map<Integer, LocalDate> fechasPorActividad,
            TipoDeCobro tipoDeCobro,
            Boolean registrarPago,
            MetodoDePago metodoPago,
            String observacionPago) {

        // Camino 1: edicion
        if (esEdicion(cliente)) {
            procesarEdicion(cliente, idActividades, fechasPorActividad, tipoDeCobro);
            return;
        }
        // Camino 2: alta.
        Cliente clienteGuardado = procesarAlta(cliente, idActividades, fechasPorActividad, tipoDeCobro);

        // El pago es opcional.
        if (Boolean.TRUE.equals(registrarPago)) {
            registrarPagoInicial(
                    clienteGuardado,
                    metodoPago,
                    observacionPago
            );
        }
    }
    private void validarMetodoDePago(MetodoDePago metodoPago) {
        if (metodoPago == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un método de pago."
            );
        }
    }

    private void procesarEdicion(
Cliente cliente,
            List<Integer> idActividades,
            Map<Integer, LocalDate> fechasPorActividad,
            TipoDeCobro tipoDeCobro) {

        validarEdicion(cliente);
        LocalDate fechaEdicion = obtenerPrimeraFecha(fechasPorActividad, LocalDate.now());

        actualizarCliente(cliente, idActividades, fechaEdicion, tipoDeCobro);
    }
    private Cliente procesarAlta(
            Cliente cliente,
            List<Integer> idActividades,
            Map<Integer, LocalDate> fechasPorActividad,
            TipoDeCobro tipoDeCobro) {

        LocalDate primeraFecha = obtenerPrimeraFecha(fechasPorActividad, null);

        validarAlta(cliente, primeraFecha, tipoDeCobro, idActividades);
        validarCuposDisponibles(idActividades);

        return registrarClienteEInscribir(cliente, idActividades, fechasPorActividad, tipoDeCobro);
    }
    private void registrarPagoInicial(
            Cliente cliente,
            MetodoDePago metodoPago,
            String observacionPago) {

        validarMetodoDePago(metodoPago);

        cliente.getInscripciones()
                .stream()
                .filter(inscripcion ->
                        inscripcion.getEstado() == EstadoInscripcion.ACTIVA
                )
                .forEach(inscripcion ->
                        pagoService.procesarPago(
                                inscripcion.getIdActividadCliente(),
                                metodoPago.getIdMetodoDePago(),
                                observacionPago,
                                LocalDate.now()
                        )
                );
    }
    private LocalDate obtenerPrimeraFecha(
            Map<Integer, LocalDate> fechasPorActividad,
            LocalDate valorPorDefecto) {

        if (fechasPorActividad == null || fechasPorActividad.isEmpty()) {
            return valorPorDefecto;
        }

        return fechasPorActividad.values()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(valorPorDefecto);
    }

    private TipoDeCobro convertirTipoDeCobro(
            String tipoDeCobroString) {

        if (tipoDeCobroString == null || tipoDeCobroString.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de cobro."
            );
        }

        try {
            return TipoDeCobro.valueOf(
                    tipoDeCobroString.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "El tipo de cobro seleccionado no es válido."
            );
        }
    }

    private Map<Integer, LocalDate> parsearFechasPorActividad(
            Map<String, String> fechaInicioMap) {

        Map<Integer, LocalDate> fechasPorActividad = new HashMap<>();

        if (fechaInicioMap == null || fechaInicioMap.isEmpty()) {
            return fechasPorActividad;
        }

        fechaInicioMap.forEach((clave, valor) -> {

            if (valor == null || valor.isBlank()) {
                return;
            }

            if (!clave.startsWith("fechaInicioMap[")
                    || !clave.endsWith("]")) {
                return;
            }

            String idActividadString = clave
                    .replace("fechaInicioMap[", "")
                    .replace("]", "");

            try {
                Integer idActividad =
                        Integer.parseInt(idActividadString);

                LocalDate fecha =
                        LocalDate.parse(valor);

                fechasPorActividad.put(idActividad, fecha);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "El identificador de una actividad no es válido."
                );

            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "La fecha ingresada para una actividad no es válida."
                );
            }
        });

        return fechasPorActividad;
    }
    // Método para validar los cupos antes de guardar un cliente
    private void validarCuposDisponibles(List<Integer> idActividades) {
        // Validación 1.
        if (idActividades == null || idActividades.isEmpty()) return;
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
        Usuario usuario = securityUtils.getUsuarioActual();
        if (clienteRepository.existsByDniAndUsuario(cliente.getDni(), usuario))
            throw new RuntimeException("Ya existe un cliente con ese DNI.");    
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
    public Cliente registrarClienteEInscribir(Cliente cliente,
                                               List<Integer> idActividades,
                                               Map<Integer, LocalDate> fechasPorActividad,
                                               TipoDeCobro tipoDeCobro) {
        Usuario usuario = securityUtils.getUsuarioActual();

        if (clienteRepository.existsByDniAndUsuario(cliente.getDni(), usuario))
            throw new RuntimeException("El cliente ya existe.");

        Cliente clienteGuardado = clienteRepository.save(cliente);

        for (Integer idActividad : idActividades) {
            Actividad actividad = actividadRepository.findById(idActividad)
                    .orElseThrow(() -> new RuntimeException(
                            "Actividad no encontrada ID: " + idActividad));

            LocalDate fecha = fechasPorActividad.getOrDefault(idActividad, LocalDate.now());
            
            actividadClienteService.inscribirCliente(clienteGuardado, actividad, fecha, tipoDeCobro, usuario);
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
        db.setEmail(form.getEmail());
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
            Usuario usuario = securityUtils.getUsuarioActual();
            for (Integer idActividad : idsNuevos) {
                
                Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada ID: " + idActividad));
                
                Optional<ActividadCliente> existente = cliente.getInscripciones().stream()
                .filter(i -> i.getActividad().getIdActividad().equals(idActividad))
                .findFirst();
                if (existente.isPresent()) {
                    manejarInscripcionExistente(existente.get(), actividad, tipoDeCobro);
                } else {
                    inscribirNueva(cliente, actividad, fechaInicio, tipoDeCobro, usuario);
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
        
    private void inscribirNueva(Cliente cliente, Actividad actividad, LocalDate fechaInicio, TipoDeCobro tipoDeCobro, Usuario usuario) {
            LocalDate fechaAlta = (fechaInicio != null) ? fechaInicio : LocalDate.now();
            actividadClienteService.inscribirCliente(cliente, actividad, fechaAlta, tipoDeCobro, usuario);
        }

    @Transactional
    public void eliminarCliente(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    
        clienteRepository.delete(cliente);
    }

    @Transactional
    public void procesarGuardado(
            String tipoDeCobroString,
            Map<String, String> fechaInicioMap,
            Cliente cliente,
            List<Integer> idActividades,
            Boolean registrarPago,
            MetodoDePago metodoPago,
            String observacionPago) {

        /*
         * Obtenemos al usuario/gimnasio actualmente autenticado.
         */
        Usuario usuario = securityUtils.getUsuarioActual();

        /*
         * Convertimos el String que llega desde el formulario
         * al enum TipoDeCobro.
         *
         * Por ejemplo:
         * "MENSUAL" -> TipoDeCobro.MENSUAL
         */
        TipoDeCobro tipoDeCobro =
                convertirTipoDeCobro(tipoDeCobroString);

        /*
         * Convertimos el mapa extraño que envía el formulario:
         *
         * fechaInicioMap[3] -> "2026-07-09"
         *
         * en un mapa más fácil de utilizar:
         *
         * 3 -> LocalDate
         */
        Map<Integer, LocalDate> fechasPorActividad =
                parsearFechasPorActividad(fechaInicioMap);

        /*
         * Asignamos el usuario dueño del cliente.
         * Esto es importante porque tu sistema es multi-tenant.
         */
        cliente.setUsuario(usuario);

        // Delegamos el alta o la actualización.
        guardarOActualizarCliente(
                cliente,
                idActividades,
                fechasPorActividad,
                tipoDeCobro,
                registrarPago,
                metodoPago,
                observacionPago
        );
    }

    public Map<Integer,String> fechaInscripcionModel(Cliente cliente){
    // Mapa actividadId -> fechaDeInscripcion para pre-cargar el input de fecha
        Map<Integer, String> fechasInscripcion = new HashMap<>();
        cliente.getInscripciones().stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .forEach(i -> fechasInscripcion.put(
                        i.getActividad().getIdActividad(),
                        i.getFechaDeInscripcion().toString()  // yyyy-MM-dd directo
                    ));
        return fechasInscripcion;
    }

    public Cliente obtenerClienteDeUsuario(Integer id) {
        Usuario usuario = securityUtils.getUsuarioActual();
        return clienteRepository.findByIdClienteAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}
