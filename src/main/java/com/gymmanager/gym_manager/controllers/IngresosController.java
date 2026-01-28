package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

@Controller
@RequestMapping("/ingresos")
public class IngresosController {
    
private final PagoRepository pagoRepository;
// clientesList
private final ClienteActividadRepository clienteActividadRepository;

    public IngresosController(PagoRepository pagoRepository, ClienteActividadRepository clienteActividadRepository) {
        this.pagoRepository = pagoRepository;
        this.clienteActividadRepository = clienteActividadRepository;
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
    // /nuevo

    @GetMapping("/nuevo")
    
    public String nuevoIngreso(Model model) {
        List<ActividadCliente> listaDeClientes = clienteActividadRepository.findAll();
        // Datos de diseño
        model.addAttribute("title", "Gym Manager | Nuevo Ingreso");
        model.addAttribute("header", "Contabilidad / Nuevo Ingreso");
        model.addAttribute("vista", "ingresos-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("clientesList", listaDeClientes);
        
        model.addAttribute("active", "ingresos");
        return "layouts/main";
    }

        @PostMapping("/guardar")
        public String guardarIngreso(
                @RequestParam Integer actividadClienteId,
                @RequestParam BigDecimal monto,
                @RequestParam MetodoDePago metodoPago
        ) {

            return "redirect:/ingresos";
        }

}


