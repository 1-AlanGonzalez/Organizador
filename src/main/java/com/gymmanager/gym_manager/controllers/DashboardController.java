package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping({"/", "/dashboard"})
    public String mostrarPaginaInicio() {
        return "inicio"; 
    }
}
