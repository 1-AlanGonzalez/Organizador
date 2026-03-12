package com.gymmanager.gym_manager.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
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
        this.pagoRepository              = pagoRepository;
        this.clienteActividadRepository = clienteActividadRepository;
        this.metodoDePagoRepository     = metodoDePagoRepository;
    }



    @Scheduled(cron = "0 0 0 * * *") //Ejecuta todos los días a medianoche
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

            if (ultimoPago.getEstado() == EstadoPago.ADEUDA) {
                continue;
            }
        
            if (ultimoPago.getEstado() == EstadoPago.PAGADO &&
                ultimoPago.getFechaVencimiento().isBefore(LocalDate.now())) {
                    // Verificamos que no exista ya un pago generado hoy para esta inscripción
                    Optional<Pago> pagoExistente = pagoRepository
                                    .findByActividadCliente_IdActividadClienteAndFechaGeneracion(
                                        inscripcion.getIdActividadCliente(),
                                        LocalDate.now()
                                    );

                    if (pagoExistente.isPresent()) {
                        continue; // ya existe → no generamos otro
                    }
                    
                    Pago nuevoPago = new Pago();
                    nuevoPago.setActividadCliente(inscripcion);
                    nuevoPago.setMontoAPagar(inscripcion.getActividad().getPrecio());
                    nuevoPago.setFechaGeneracion(LocalDate.now());
                    nuevoPago.setFechaVencimiento(LocalDate.now().plusMonths(1));
                    nuevoPago.setEstado(EstadoPago.ADEUDA);
                    nuevoPago.setMetodoPago(metodoDePagoRepository.findByNombre("No especificado").orElse(null));
                    pagoRepository.save(nuevoPago);
                    System.out.println("Nuevo pago generado para inscripción: "
                        + inscripcion.getIdActividadCliente());
                }

        
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
