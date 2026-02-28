package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class PagoService {
    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final PagoRepository pagoRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;

    public PagoService( ConfiguracionPagoRepository configuracionPagoRepository,PagoRepository pagoRepository, MetodoDePagoRepository metodoDePagoRepository) {
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.pagoRepository = pagoRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
    }



    @Transactional
    public void procesarPago(Integer idActividadCliente, Integer metodoPagoId, String observaciones) {

        MetodoDePago metodo = metodoDePagoRepository.findById(metodoPagoId)
            .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        ConfiguracionDePago config = configuracionPagoRepository
            .findByMetodoDePagoAndActivoTrue(metodo)
            .orElseThrow(() -> new RuntimeException("No hay configuración para ese método"));

        Pago pago = pagoRepository.findByActividadCliente_IdActividadClienteAndEstado(
                    idActividadCliente,
                    EstadoPago.ADEUDA)
            .orElseThrow(() -> new RuntimeException(
                    "No hay pagos pendientes para esta inscripción"
            ));

        BigDecimal recargo = pago.getMontoAPagar()
            .multiply(config.getPorcentajeRecargo())
            .divide(BigDecimal.valueOf(100));

        pago.aplicarRecargo(recargo);
        pago.setMetodoPago(metodo);

        if (observaciones != null && !observaciones.isBlank()) {
        pago.setObservaciones(observaciones);
        }

        pago.pagar();

        pagoRepository.save(pago);
    }
}
    

