package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingresos")
public class IngresosController {

    @GetMapping
    public String ingresos(Model model) {
        model.addAttribute("title", "Gym Manager | Ingresos");
        model.addAttribute("header", "Panel de control / Ingresos");

        model.addAttribute("vista", "ingresos");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "ingresos");
        return "layouts/main";
    }
}


