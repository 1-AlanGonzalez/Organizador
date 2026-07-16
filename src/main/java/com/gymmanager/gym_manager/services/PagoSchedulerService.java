package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.gymmanager.gym_manager.entity.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Service
public class PagoSchedulerService {
    private final PagoRepository pagoRepository;
    private final ClienteActividadRepository clienteActividadRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;

    public PagoSchedulerService(PagoRepository pagoRepository,
                                ClienteActividadRepository clienteActividadRepository,
                                MetodoDePagoRepository metodoDePagoRepository) {
        this.pagoRepository = pagoRepository;
        this.clienteActividadRepository = clienteActividadRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
    }



    @Scheduled(cron = "0/10 * * * * *") //Ejecuta todos los días a medianoche
    @Transactional
    public void generarPagosMensuales() {

        System.out.println("Scheduler de pagos ejecutándose...");
        List<ActividadCliente> inscripciones = clienteActividadRepository.findAllActivas();

        for (ActividadCliente inscripcion : inscripciones) {
            Pago ultimoPago = pagoRepository
                    .findTopByActividadCliente_IdActividadClienteOrderByFechaVencimientoDesc(
                            inscripcion.getIdActividadCliente())
                    .orElse(null);

            if (ultimoPago == null) {
                continue;
            }

            // Si el vencimiento no pasó todavía → no hacer nada
            if (!ultimoPago.getFechaVencimiento().isBefore(LocalDate.now())) continue;

            if (ultimoPago.getEstado() == EstadoPago.ADEUDA) {
                ultimoPago.setEstado(EstadoPago.VENCIDO);
                pagoRepository.save(ultimoPago);
            }
            // Verificar que no exista ya un pago generado hoy
            Optional<Pago> pagoExistente = pagoRepository
                    .findByActividadCliente_IdActividadClienteAndFechaGeneracion(
                            inscripcion.getIdActividadCliente(),
                            LocalDate.now());

            if (pagoExistente.isPresent()) continue;

            // Generar nuevo pago ADEUDA
            Usuario usuario = inscripcion.getCliente().getUsuario();
            MetodoDePago sinMetodo = metodoDePagoRepository.findByNombreAndUsuario("NO_ESPECIFICADO", usuario).orElse(null);

            Pago nuevoPago = new Pago();
            nuevoPago.setActividadCliente(inscripcion);
            nuevoPago.setMontoAPagar(inscripcion.getActividad().getPrecio());
            nuevoPago.setFechaGeneracion(LocalDate.now());
            nuevoPago.setFechaVencimiento(LocalDate.now().plusMonths(1));
            nuevoPago.setEstado(EstadoPago.ADEUDA);
            nuevoPago.setMetodoPago(sinMetodo);
            pagoRepository.save(nuevoPago);

            System.out.println("Nuevo pago generado para inscripción: "
                    + inscripcion.getIdActividadCliente());
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // todos los días a las 00:00
    @Transactional
    public void desactivarInscripcionesDiarias() {

        List<ActividadCliente> inscripciones = clienteActividadRepository.findAllActivas();

        for (ActividadCliente inscripcion : inscripciones) {

            if (inscripcion.getTipoDeCobro() == TipoDeCobro.DIARIO) {

                Pago ultimoPago = pagoRepository
                    .findTopByActividadCliente_IdActividadClienteOrderByFechaGeneracionDesc(
                        inscripcion.getIdActividadCliente())
                    .orElse(null);

                if (ultimoPago == null) continue;

                if (ultimoPago.getFechaGeneracion().isBefore(LocalDate.now())) {

                    inscripcion.setEstado(EstadoInscripcion.BAJA);
                    clienteActividadRepository.save(inscripcion);

                    System.out.println("Inscripción diaria desactivada: " + inscripcion.getIdActividadCliente());
                }
            }
        }
    }
}
