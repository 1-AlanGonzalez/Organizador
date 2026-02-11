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
        System.out.println("🔥 ENTRO A procesarPago");

        // Buscar el método de pago que ahora es una entidad
        MetodoDePago metodo = metodoDePagoRepository.findById(metodoPagoId)
            .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));


         //Busca la configuracion activa que esta relacionado con ese metodo
        ConfiguracionDePago config = configuracionPagoRepository
            .findByMetodoDePagoAndActivoTrue(metodo)
            .orElseThrow(() -> new RuntimeException("No hay configuración para ese método"));

        // Buscar el pago pendiente asociado a la actividad del cliente
        Pago pago = pagoRepository.findByActividadCliente_IdActividadClienteAndEstado(
                    idActividadCliente,
                    EstadoPago.ADEUDA)
            .orElseThrow(() -> new RuntimeException(
                    "No hay pagos pendientes para esta inscripción"
            ));
        
        System.out.println("👉 Pago encontrado");
        System.out.println("Estado: " + pago.getEstado());
        System.out.println("Monto inicial: " + pago.getMontoAPagar());
        System.out.println("Metodo actual: " + pago.getMetodoPago());
        System.out.println("👉 Metodo de pago: " + metodo.getNombre());

        // Calculae el recargo según la configuración
        BigDecimal recargo = pago.getMontoAPagar()
            .multiply(config.getPorcentajeRecargo())
            .divide(BigDecimal.valueOf(100));

        // Aplicar recargo y método
        pago.aplicarRecargo(recargo);
        pago.setMetodoPago(metodo);

        // Guardar observaciones si existen
        if (observaciones != null && !observaciones.isBlank()) {
        pago.setObservaciones(observaciones);
        }

        System.out.println("👉 Antes de pagar");
        System.out.println("Monto final: " + pago.getMontoAPagar());
        System.out.println("Metodo asignado: " + pago.getMetodoPago());

        //Marcar el pago como PAGADO
        pago.pagar();

        System.out.println("👉 Guardando pago...");
        pagoRepository.save(pago);
        System.out.println("✅ Pago guardado");
    }
}
    

