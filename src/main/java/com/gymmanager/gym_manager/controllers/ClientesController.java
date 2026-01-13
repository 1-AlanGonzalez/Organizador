package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gymmanager.gym_manager.entity.Cliente;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    @GetMapping
    public String clientes(Model model) {
        model.addAttribute("title", "Gym Manager | Clientes");
        model.addAttribute("header", "Panel de control / Clientes");

        model.addAttribute("vista", "clientes");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "clientes");
        return "layouts/main";
    }

    @GetMapping("/nuevo")
    public String nuevoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("vista", "clientes_form");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "clientes");
        return "layouts/main";
    }
}


