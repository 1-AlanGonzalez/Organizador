package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.services.PagoService;

@Controller
@RequestMapping("/pagos")
public class PagoController {
    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    
    @PostMapping("/pagar")
    public String pagar(
        @RequestParam Integer idPago,
        @RequestParam MetodoDePago metodoDePago,
        @RequestParam(required = false) String observaciones,
        RedirectAttributes redirectAttributes
    ){
        try{
            pagoService.procesarPago(idPago, metodoDePago, observaciones);
            redirectAttributes.addFlashAttribute("succes", "Pago registrado correctamente");
            return "redirect:/clientes";
        } catch(Exception e){
            redirectAttributes.addFlashAttribute("error",e.getMessage());
            return "redirect:/clientes";
        }
    }
}
