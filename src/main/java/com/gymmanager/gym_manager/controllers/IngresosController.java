package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gymmanager.gym_manager.repository.PagoRepository;

@Controller
@RequestMapping("/ingresos")
public class IngresosController {
    
private final PagoRepository pagoRepository;

    public IngresosController(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @GetMapping
    public String ingresos(Model model) {
    BigDecimal total = pagoRepository.sumTotalRecaudado();
    BigDecimal pendientes = pagoRepository.sumTotalPendiente();
    BigDecimal efectivo = pagoRepository.sumEfectivo();
    BigDecimal transferencia = pagoRepository.sumTransferencia();

    model.addAttribute("ingresosTotales", total != null ? total : BigDecimal.ZERO);
    model.addAttribute("ingresosEfectivo", efectivo != null ? efectivo : BigDecimal.ZERO);
    model.addAttribute("ingresosTransferencia", transferencia != null ? transferencia : BigDecimal.ZERO);
    model.addAttribute("ingresosPendientes", pendientes != null ? pendientes : BigDecimal.ZERO);
    // Para gráfico

    // Añade esto para que el gráfico no falle en la sección de Ingresos
    model.addAttribute("datosGrafico", pagoRepository.obtenerIngresosMensuales());
    model.addAttribute("categoriasGrafico", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));
    
    // Datos de diseño
        model.addAttribute("title", "Gym Manager | Ingresos");
        model.addAttribute("header", "Contabilidad / Resumen de Ingresos");
        model.addAttribute("vista", "ingresos");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "ingresos");

        return "layouts/main";
    }
}


