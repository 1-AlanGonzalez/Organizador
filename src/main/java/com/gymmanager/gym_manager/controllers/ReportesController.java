package com.gymmanager.gym_manager.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    @GetMapping
    public String reportes(Model model) {
        
        model.addAttribute("title", "Gym Manager | Reportes");
        model.addAttribute("header", "Panel de control / Reportes");

        model.addAttribute("vista", "reportesExcel"); 
        
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "reportes");

        return "layouts/main";
    }
}