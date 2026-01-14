package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    @GetMapping
    public String asistencias(Model model) {
        model.addAttribute("title", "Gym Manager | Asistencias");
        model.addAttribute("header", "Panel de control / Asistencias");

        model.addAttribute("vista", "asistencias");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "asistencias");
        return "layouts/main";
    }
}



