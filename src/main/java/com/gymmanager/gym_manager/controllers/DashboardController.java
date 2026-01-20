package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.gymmanager.gym_manager.repository.ClienteRepository;

import org.springframework.ui.Model;

@Controller
public class DashboardController {

    // Inyectamos el repositorio de Cliente para obtener el total de clientes
    private final ClienteRepository clienteRepository;

    public DashboardController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        // Total de clientes
        model.addAttribute("totalClientes", clienteRepository.count());


        model.addAttribute("title", "Gym Manager | Inicio");
        model.addAttribute("header", "Panel de control / Inicio");

        model.addAttribute("vista", "inicio");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "dashboard");

        // Necesitamos obtener la cantidad totales de clientes para mostrar en el dashboard
        // model.addAttribute("totalClientes", ClienteRepository.); 

        return "layouts/main";
    }
}

