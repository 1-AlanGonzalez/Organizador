package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("title", "Gym Manager | Inicio");
        model.addAttribute("header", "Panel de control / Inicio");

        model.addAttribute("vista", "inicio");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "dashboard");

        return "layouts/main";
    }
}

