package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class PagoService {
    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final PagoRepository pagoRepository;

    public PagoService(ConfiguracionPagoRepository configuracionPagoRepository, PagoRepository pagoRepository) {
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.pagoRepository = pagoRepository;
    }


    public void procesarPago(Integer idPago, MetodoDePago metodoDePago, String observaciones){
        ConfiguracionDePago config = configuracionPagoRepository
        .findByMetodoDePagoAndActivoTrue(metodoDePago)
        .orElseThrow(() -> new RuntimeException("No hay configuración para ese método"));
        
        Pago pago = pagoRepository.findById(idPago).orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        
        if (pago.getEstado() == EstadoPago.PAGADO) {
            throw new RuntimeException("Este pago ya fue pagado");
        }

        BigDecimal recargo = pago.getMontoAPagar().multiply(config.getPorcentajeRecargo()).divide(BigDecimal.valueOf(100));

        pago.aplicarRecargo(recargo);

        pago.setMetodoPago(metodoDePago);

        if (observaciones != null && !observaciones.isBlank()) {
            pago.setObservaciones(observaciones);
        }

        pago.pagar();

        pagoRepository.save(pago);
    }
    
}
