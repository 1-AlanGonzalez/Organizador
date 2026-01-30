package com.gymmanager.gym_manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.Pago;

public class TestEntidades {
    @Test
    void pagar_cambia_estado_a_pagado() {
        Pago pago = new Pago(
            new BigDecimal("1000"),
            LocalDate.now(),
            LocalDate.now().plusMonths(1),
            null, null
        );

        pago.pagar();

        assertEquals(EstadoPago.PAGADO, pago.getEstado());
    }

    @Test
    void no_deberia_generar_dos_pagos_activos() {
    ActividadCliente inscripcion = new ActividadCliente(
        LocalDate.now(),
        new BigDecimal("5000"),
        null,
        null
    );

    inscripcion.generarPagoMensual(LocalDate.now(), null);

    assertThrows(
        RuntimeException.class,
        () -> inscripcion.generarPagoMensual(LocalDate.now(), null)
    );
}
}

