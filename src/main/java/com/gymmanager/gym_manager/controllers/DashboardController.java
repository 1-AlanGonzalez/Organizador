package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.gymmanager.gym_manager.entity.EstadoInscripcion;
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
        BigDecimal totalMesActual = pagoRepository.sumTotalRecaudadoEnMes(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        model.addAttribute("totalMesActual", totalMesActual != null ? totalMesActual : BigDecimal.ZERO);

        //   TOTAL DE CLIENTES
        
        model.addAttribute("totalClientes", clienteRepository.count());

        long totalInscriptos = clienteRepository.countClientesConInscripcionActiva(EstadoInscripcion.ACTIVA);
        long totalDeudores = clienteRepository.countClientesDeudores();
        long activosAlDia = totalInscriptos - totalDeudores;
        model.addAttribute("totalInscriptos", totalInscriptos);
        model.addAttribute("clientesActivos", activosAlDia); 
        model.addAttribute("clientesPendientes", totalDeudores); 
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

