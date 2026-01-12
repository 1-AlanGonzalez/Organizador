package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/actividades")
public class ActividadesController {

    @GetMapping
    public String actividades(Model model) {
        model.addAttribute("title", "Gym Manager | Actividades");
        model.addAttribute("header", "Panel de control / Actividades");

        model.addAttribute("vista", "actividades");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "actividades");
        
        return "layouts/main";
    }
}


