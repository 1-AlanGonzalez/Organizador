package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import org.springframework.ui.Model;

@Controller
public class DashboardController {

    // Inyectamos el repositorio de Cliente para obtener el total de clientes
    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    public DashboardController(ClienteRepository clienteRepository, PagoRepository pagoRepository) {
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        BigDecimal total = pagoRepository.sumTotalRecaudado();
        model.addAttribute("ingresosTotales", total != null ? total : BigDecimal.ZERO);
        model.addAttribute("totalClientes", clienteRepository.count());
//   TOTAL DE CLIENTES

        model.addAttribute("totalClientes", clienteRepository.count());

        // 3. Lógica de Activos vs Pendientes
        long totalInscriptos = clienteRepository.countClientesConInscripcionActiva();
        long totalDeudores = clienteRepository.countClientesDeudores();
    // "Activos" en la caja verde serán los que NO deben nada
        long activosAlDia = totalInscriptos - totalDeudores;

        model.addAttribute("clientesActivos", activosAlDia);     // Caja Verde (Sin deuda)
        model.addAttribute("clientesPendientes", totalDeudores); // Caja Naranja (Con deuda)
        // Datos para el gráfico
        model.addAttribute("datosGrafico", pagoRepository.obtenerIngresosMensuales());
        model.addAttribute("categoriasGrafico", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));

        model.addAttribute("title", "Gym Manager | Inicio");
        model.addAttribute("header", "Panel de control / Inicio");

        model.addAttribute("vista", "inicio");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "dashboard");

        return "layouts/main";
    }
}

