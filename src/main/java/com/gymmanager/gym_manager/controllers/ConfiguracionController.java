package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Configuracion;
import com.gymmanager.gym_manager.services.ConfiguracionService;


@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {
    private final ConfiguracionService configuracionService;

    
    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public String configuracion(Model model) {
        model.addAttribute("config", new Configuracion());

        model.addAttribute("title", "Gym Manager | Configuración");
        model.addAttribute("header", "Panel de control / Configuración");

        model.addAttribute("vista", "configuracion/configuracion");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "configuracion");

        return "layouts/main";
    }

    @PostMapping("/cambiar-recargo")
    public String cambiarPorcentaje(
        @RequestParam Integer idConfiguracion,
        @RequestParam BigDecimal porcentaje,
        Model model,
        RedirectAttributes redirectAttributes){
            try{

                configuracionService.cambiarPorcentajeInteres(idConfiguracion, porcentaje);
                redirectAttributes.addFlashAttribute("succes","Porcentaje de recargo actualizado correctamente");
            }
            catch(Exception e ){
                redirectAttributes.addFlashAttribute("error",e.getMessage());
            }
             return "redirect:/configuracion";
        }
}