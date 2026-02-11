package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.util.Arrays;
// import java.time.LocalDate;
// import java.util.Arrays;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


// import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;
import com.gymmanager.gym_manager.services.PagoService;

@Controller
@RequestMapping("/ingresos")
public class IngresosController {
    
private final PagoRepository pagoRepository;
private final PagoService pagoService;
// clientesList
private final ClienteActividadRepository clienteActividadRepository;
private final MetodoDePagoRepository metodoDePagoRepository;

public IngresosController(PagoService pagoService, PagoRepository pagoRepository, ClienteActividadRepository clienteActividadRepository, MetodoDePagoRepository metodoDePagoRepository) {
        this.pagoRepository = pagoRepository;
        this.pagoService = pagoService;
        this.clienteActividadRepository = clienteActividadRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
    }


    @GetMapping
    public String ingresos(Model model) {

    BigDecimal total = pagoRepository.sumTotalRecaudado();
    BigDecimal pendientes = pagoRepository.sumTotalPendiente();

    MetodoDePago efectivo = metodoDePagoRepository.findByNombre("EFECTIVO").orElse(null);
    MetodoDePago transferencia = metodoDePagoRepository.findByNombre("TRANSFERENCIA").orElse(null);

    BigDecimal totalEfectivo = efectivo != null
            ? pagoRepository.sumPorMetodo(efectivo)
            : BigDecimal.ZERO;

    BigDecimal totalTransferencia = transferencia != null
            ? pagoRepository.sumPorMetodo(transferencia)
            : BigDecimal.ZERO;

    model.addAttribute("ingresosTotales", total != null ? total : BigDecimal.ZERO);
    model.addAttribute("ingresosEfectivo", totalEfectivo);
    model.addAttribute("ingresosTransferencia", totalTransferencia);
    model.addAttribute("ingresosPendientes", pendientes != null ? pendientes : BigDecimal.ZERO);

    model.addAttribute("datosGrafico", pagoRepository.obtenerIngresosMensuales());
    model.addAttribute("categoriasGrafico",
            Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                          "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));

    model.addAttribute("pagosRecientes", pagoRepository.findAll());

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
        // List<ActividadCliente> listaDeClientes = clienteActividadRepository.findAll();
        // // Datos de diseño
        // model.addAttribute("title", "Gym Manager | Nuevo Ingreso");
        // model.addAttribute("header", "Contabilidad / Nuevo Ingreso");
        // model.addAttribute("vista", "ingresos-nuevo");
        // model.addAttribute("fragmento", "contenido");
        // model.addAttribute("clientesList", listaDeClientes);
        
        // model.addAttribute("active", "ingresos");
        model.addAttribute("clientesList", clienteActividadRepository.findAll());
        model.addAttribute("metodosPago", metodoDePagoRepository.findAll());
        
        model.addAttribute("title", "Gym Manager | Nuevo Ingreso");
        model.addAttribute("header", "Contabilidad / Nuevo Ingreso");
        model.addAttribute("vista", "ingresos-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "ingresos");
        return "layouts/main";
    }


    @PostMapping("/guardar")
    public String guardarIngreso(
        @RequestParam Integer idActividadCliente,
        @RequestParam Integer metodoPagoId,
        @RequestParam(required = false) String observaciones,
        RedirectAttributes flash) {

    // try {
    //     pagoService.procesarPago(idPago, metodoPago, observaciones);
    //     flash.addFlashAttribute("success", "Pago registrado correctamente.");
    // } catch (Exception e) {
    //     flash.addFlashAttribute("error", e.getMessage());
    // }

    // return "redirect:/ingresos";
    System.out.println("id de la isncripcion = " + idActividadCliente);
    System.out.println("metodoPagoId = " + metodoPagoId);
    System.out.println("observaciones = " + observaciones);
    try {
        System.out.println("Entre al try");
        pagoService.procesarPago(idActividadCliente, metodoPagoId, observaciones);
        System.out.println("Ejecute el metodo procesarPago");
        flash.addFlashAttribute("success", "Pago registrado correctamente.");
        System.out.println("se huso el flash");
    } catch (Exception e) {
        e.printStackTrace();
        flash.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/ingresos";
}

}


