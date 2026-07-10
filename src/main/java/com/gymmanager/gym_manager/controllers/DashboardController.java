package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

@Controller
public class DashboardController {

    private final ClienteRepository clienteRepository;
    private final PagoRepository    pagoRepository;
    private final SecurityUtils     securityUtils;

    public DashboardController(ClienteRepository clienteRepository,
                               PagoRepository    pagoRepository,
                               SecurityUtils     securityUtils) {
        this.clienteRepository = clienteRepository;
        this.pagoRepository    = pagoRepository;
        this.securityUtils     = securityUtils;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        // ── Ingresos del mes
        BigDecimal totalMesActual = pagoRepository.sumTotalRecaudadoEnMes(
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), usuario);
        model.addAttribute("totalMesActual",
                totalMesActual != null ? totalMesActual : BigDecimal.ZERO);

        // ── Totales de ingresos
        BigDecimal totalRecaudado = pagoRepository.sumTotalRecaudado(usuario);
        BigDecimal totalPendiente = pagoRepository.sumTotalPendiente(usuario);
        model.addAttribute("totalRecaudado",
                totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO);
        model.addAttribute("totalPendiente",
                totalPendiente != null ? totalPendiente : BigDecimal.ZERO);

        // ── Clientes
        long totalClientes   = clienteRepository.findByUsuario(usuario).size();
        long totalInscriptos = clienteRepository.countClientesConInscripcionActiva(
                EstadoInscripcion.ACTIVA, usuario);
        long totalDeudores   = clienteRepository.countClientesDeudores(usuario);
        long activosAlDia    = totalInscriptos - totalDeudores;

        model.addAttribute("totalClientes",      totalClientes);
        model.addAttribute("totalInscriptos",    totalInscriptos);
        model.addAttribute("clientesActivos",    activosAlDia);
        model.addAttribute("clientesPendientes", totalDeudores);

        // ── Deudores (lista para mostrar en dashboard)
        List<Cliente> clientesDeudores = clienteRepository.findClientesConDeudaByUsuario(usuario);
        model.addAttribute("clientesDeudores", clientesDeudores);

        // ── Próximos vencimientos (próximos 7 días)
        LocalDate hoy     = LocalDate.now();
        LocalDate en7dias = hoy.plusDays(7);
        List<Cliente> proximosVencimientos = clienteRepository
                .findClientesConVencimientoEntre(usuario, hoy, en7dias);
        model.addAttribute("proximosVencimientos", proximosVencimientos);

        // ── Gráfico
        List<Object[]> ingresosMensuales = pagoRepository.obtenerIngresosMensuales(usuario.getId());
        model.addAttribute("datosGrafico",      ingresosMensuales);
        model.addAttribute("categoriasGrafico", Arrays.asList(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));

        model.addAttribute("title",     "Gym Manager | Inicio");
        model.addAttribute("header",    "Panel de control / Inicio");
        model.addAttribute("vista",     "inicio/inicio");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "dashboard");

        return "layouts/main";
    }
}