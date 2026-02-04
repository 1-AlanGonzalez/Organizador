package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    @GetMapping
    public String configuracion(Model model) {

        model.addAttribute("title", "Gym Manager | Configuración");
        model.addAttribute("header", "Panel de control / Configuración");

        model.addAttribute("vista", "configuracion/configuracion");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "configuracion");

        return "layouts/main";
    }
    
}