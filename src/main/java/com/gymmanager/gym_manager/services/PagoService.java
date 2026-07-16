package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymmanager.gym_manager.config.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;
import com.gymmanager.gym_manager.entity.Usuario;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final PagoRepository              pagoRepository;
    private final MetodoDePagoRepository      metodoDePagoRepository;
    private final SecurityUtils securityUtils;

    public PagoService(ConfiguracionPagoRepository configuracionPagoRepository,
                       PagoRepository              pagoRepository,
                       MetodoDePagoRepository      metodoDePagoRepository,
                        SecurityUtils securityUtils
    ) {
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.pagoRepository              = pagoRepository;
        this.metodoDePagoRepository      = metodoDePagoRepository;
        this.securityUtils = securityUtils;
    }

    // ← ahora devuelve Pago en lugar de void para poder redirigir al ticket
    @Transactional
    public Pago procesarPago (Integer idActividadCliente, Integer metodoPagoId, String observaciones, LocalDate fechaDePago) {

        Usuario usuario = securityUtils.getUsuarioActual();
        System.out.println("Procesando pago...");
        log.debug("Procesando pago para inscripción ID: {}", idActividadCliente);

        MetodoDePago metodo = metodoDePagoRepository
                .findByIdMetodoDePagoAndUsuario(metodoPagoId, usuario)
                .orElseThrow(() ->
                        new RuntimeException("Método de pago no encontrado")
                );

        ConfiguracionDePago config = configuracionPagoRepository
                .findByMetodoDePagoAndActivoTrueAndMetodoDePago_Usuario(
                        metodo,
                        usuario
                )
                .orElseThrow(() -> new RuntimeException(
                        "No hay configuración activa para ese método de pago"));

        Pago pago = pagoRepository
                .findByActividadCliente_IdActividadClienteAndEstadoAndActividadCliente_Cliente_Usuario(
                idActividadCliente,
                EstadoPago.ADEUDA,
                usuario
        )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No hay pagos pendientes para esta inscripción"
                        )
                );

        BigDecimal recargo = pago.getMontoAPagar()
                .multiply(config.getPorcentajeRecargo())
                .divide(BigDecimal.valueOf(100));

        pago.aplicarRecargo(recargo);
        pago.setMetodoPago(metodo);

        if (observaciones != null && !observaciones.isBlank()){
            pago.setObservaciones(observaciones);}
        LocalDate fechaEfectiva =
                fechaDePago != null ? fechaDePago : LocalDate.now();
        pago.ajusteDeFechas(fechaEfectiva);
        pago.pagar();
        Pago pagado = pagoRepository.save(pago);

        log.info("Pago registrado — inscripción ID: {}, método: {}, monto: {}",
                idActividadCliente, metodo.getNombre(), pagado.getMontoAPagar());

        return pagado;  // ← devuelve el pago con su ID para el ticket
    }
    @Transactional
    public void eliminarPago(Integer id) {
        Usuario usuario = securityUtils.getUsuarioActual();
        Pago pago = pagoRepository
                .findByIdPagoAndActividadCliente_Cliente_Usuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.restaurarPago();
        pagoRepository.save(pago);
    }

    public Pago obtenerPago(Integer id) {
        Usuario usuario = securityUtils.getUsuarioActual();
        return pagoRepository
                .findByIdPagoAndActividadCliente_Cliente_Usuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public Pago editarPago(
            Integer id,
            Integer metodoPagoId,
            String observaciones,
            LocalDate fechaDePago) {

        Usuario usuario = securityUtils.getUsuarioActual();
        Pago pago = pagoRepository
                .findByIdPagoAndActividadCliente_Cliente_Usuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        MetodoDePago metodo = metodoDePagoRepository
                .findByIdMetodoDePagoAndUsuario(metodoPagoId, usuario)
                .orElseThrow(() ->
                        new RuntimeException("Método de pago no encontrado"));

        pago.editarPago(metodo, observaciones, fechaDePago);
        return pagoRepository.save(pago);
    }
}
