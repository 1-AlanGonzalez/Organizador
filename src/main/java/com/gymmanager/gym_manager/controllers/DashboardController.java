package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;

@Controller
public class DashboardController {

    //  Importo el controlador de clientes para poder utilizar la función que cree "cantidadTotal()"
    private final ClientesController clientesController;

    public DashboardController(ClientesController clientesController) {
        this.clientesController = clientesController;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        // Total de clientes
        model.addAttribute("totalClientes", clientesController.cantidadTotal());


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

