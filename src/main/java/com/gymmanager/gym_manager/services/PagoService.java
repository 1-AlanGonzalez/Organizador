package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final PagoRepository              pagoRepository;
    private final MetodoDePagoRepository      metodoDePagoRepository;

    public PagoService(ConfiguracionPagoRepository configuracionPagoRepository,
                       PagoRepository              pagoRepository,
                       MetodoDePagoRepository      metodoDePagoRepository) {
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.pagoRepository              = pagoRepository;
        this.metodoDePagoRepository      = metodoDePagoRepository;
    }

    // ← ahora devuelve Pago en lugar de void para poder redirigir al ticket
    @Transactional
    public Pago procesarPago(Integer idActividadCliente, Integer metodoPagoId,
                              String observaciones) {

        log.debug("Procesando pago para inscripción ID: {}", idActividadCliente);

        MetodoDePago metodo = metodoDePagoRepository.findById(metodoPagoId)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        ConfiguracionDePago config = configuracionPagoRepository
                .findByMetodoDePagoAndActivoTrue(metodo)
                .orElseThrow(() -> new RuntimeException(
                        "No hay configuración activa para ese método de pago"));

        Pago pago = pagoRepository
                .findByActividadCliente_IdActividadClienteAndEstado(
                        idActividadCliente, EstadoPago.ADEUDA)
                .orElseThrow(() -> new RuntimeException(
                        "No hay pagos pendientes para esta inscripción"));

        BigDecimal recargo = pago.getMontoAPagar()
                .multiply(config.getPorcentajeRecargo())
                .divide(BigDecimal.valueOf(100));

        pago.aplicarRecargo(recargo);
        pago.setMetodoPago(metodo);

        if (observaciones != null && !observaciones.isBlank())
            pago.setObservaciones(observaciones);

        pago.pagar();
        Pago pagado = pagoRepository.save(pago);

        log.info("Pago registrado — inscripción ID: {}, método: {}, monto: {}",
                idActividadCliente, metodo.getNombre(), pagado.getMontoAPagar());

        return pagado;  // ← devuelve el pago con su ID para el ticket
    }
}