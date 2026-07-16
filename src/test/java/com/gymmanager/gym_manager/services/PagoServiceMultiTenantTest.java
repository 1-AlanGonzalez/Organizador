package com.gymmanager.gym_manager.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

class PagoServiceMultiTenantTest {

    private PagoRepository pagoRepository;
    private MetodoDePagoRepository metodoDePagoRepository;
    private SecurityUtils securityUtils;
    private PagoService pagoService;
    private Usuario usuarioActual;

    @BeforeEach
    void setUp() {
        pagoRepository = mock(PagoRepository.class);
        metodoDePagoRepository = mock(MetodoDePagoRepository.class);
        securityUtils = mock(SecurityUtils.class);
        usuarioActual = new Usuario("gimnasio-a", "password");
        when(securityUtils.getUsuarioActual()).thenReturn(usuarioActual);

        pagoService = new PagoService(
                mock(ConfiguracionPagoRepository.class),
                pagoRepository,
                metodoDePagoRepository,
                securityUtils);
    }

    @Test
    void noPermiteAbrirUnPagoAjeno() {
        when(pagoRepository.findByIdPagoAndActividadCliente_Cliente_Usuario(99, usuarioActual))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.obtenerPago(99))
                .hasMessage("Pago no encontrado");

        verify(pagoRepository, never()).findById(99);
    }

    @Test
    void noPermiteAnularUnPagoAjeno() {
        when(pagoRepository.findByIdPagoAndActividadCliente_Cliente_Usuario(99, usuarioActual))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.eliminarPago(99))
                .hasMessage("Pago no encontrado");

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void noPermiteEditarConUnMetodoDePagoAjeno() {
        Pago pago = mock(Pago.class);
        when(pagoRepository.findByIdPagoAndActividadCliente_Cliente_Usuario(10, usuarioActual))
                .thenReturn(Optional.of(pago));
        when(metodoDePagoRepository.findByIdMetodoDePagoAndUsuario(77, usuarioActual))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.editarPago(
                10, 77, "observación", LocalDate.now()))
                .hasMessage("Método de pago no encontrado");

        verify(pagoRepository, never()).save(pago);
    }
}
