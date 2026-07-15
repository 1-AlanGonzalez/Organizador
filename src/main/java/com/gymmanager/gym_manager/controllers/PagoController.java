package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.repository.PagoRepository;
import com.gymmanager.gym_manager.services.PagoService;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService    pagoService;
    private final PagoRepository pagoRepository;
    private final SecurityUtils  securityUtils;

    public PagoController(PagoService    pagoService,
                          PagoRepository pagoRepository,
                          SecurityUtils  securityUtils) {
        this.pagoService    = pagoService;
        this.pagoRepository = pagoRepository;
        this.securityUtils  = securityUtils;
    }

    // ── POST /pagos/pagar ─────────────────────────────────────────────────────

    @PostMapping("/pagar")
    public String pagar(@RequestParam Integer idPago,
                        @RequestParam Integer metodoPagoId,
                        @RequestParam(required = false) String observaciones,
                        @RequestParam (required = false) LocalDate fechaDePago,
                        RedirectAttributes redirectAttributes) {
        System.out.println("ActividadCliente: " + idPago);
        System.out.println("MetodoPago: " + metodoPagoId);
        try {
            Pago pago = pagoService.procesarPago(idPago, metodoPagoId, observaciones, fechaDePago);
            // Redirige al ticket imprimible con el ID del pago recién registrado
            return "redirect:/pagos/ticket/" + pago.getIdPago();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes";
        }
    }

    // ── GET /pagos/ticket/{idPago} ────────────────────────────────────────────

    @GetMapping("/ticket/{idPago}")
    public String verTicket(@PathVariable Integer idPago, Model model) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        // Seguridad: solo el dueño del pago puede ver el ticket
        var usuarioActual = securityUtils.getUsuarioActual();
        var usuarioPago   = pago.getActividadCliente().getCliente().getUsuario();
        if (!usuarioActual.getId().equals(usuarioPago.getId()))
            return "redirect:/clientes";

        model.addAttribute("pago",    pago);
        model.addAttribute("usuario", usuarioActual);
        return "ingresos/ticket";
    }
}