package com.gymmanager.gym_manager.controllers;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

import com.gymmanager.gym_manager.services.ClienteService;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final ClienteService clienteService;
    

    public ClientesController(ClienteRepository clienteRepository, ActividadRepository actividadRepository,
            ClienteService clienteService) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String clientes(Model model) {
        // Añado las actividades para el panel de inscripciones
        model.addAttribute("actividades", actividadRepository.findAll());
    
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("title", "Gym Manager | Clientes");
        model.addAttribute("header", "Panel de control / Clientes");
        model.addAttribute("cliente", new Cliente());

        model.addAttribute("vista", "clientes");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "clientes");
        return "layouts/main";
    }
    /*
     Bueno le saque peso de codeo al guardarCliente ahora se encarga de la verificaciones y logica los servicios 
     tanto de cliente service y la relacion clienteActividad service para que esto funcione
     */
    @PostMapping("/guardar")
        public String guardarCliente(
            @ModelAttribute Cliente cliente, 
            // RequestParam para la actividad seleccionada
            @RequestParam(required = false) List<Integer> idActividades,
            Model model, 
            RedirectAttributes redirectAttributes) {
            try {

                clienteService.registrarClienteEInscribir(cliente, idActividades);

                redirectAttributes.addFlashAttribute("success", "Cliente guardado con éxito.");
                return "redirect:/clientes";

            } catch (Exception e) {
                prepararModelo(model);
                model.addAttribute("error", e.getMessage());
                model.addAttribute("abrirPanel", true);
                return "layouts/main";
            }
        }

// Método auxiliar para evitar repetir código en los métodos del controlador
private void prepararModelo(Model model) {
    model.addAttribute("clientes", clienteRepository.findAll());
    model.addAttribute("title", "Gym Manager | Clientes");
    model.addAttribute("header", "Panel de control / Clientes");
    model.addAttribute("vista", "clientes");
    model.addAttribute("fragmento", "contenido");
    model.addAttribute("active", "clientes");
}
// Método auxiliar limpio para el layout
private void prepararModeloBase(Model model, String title, String header) {
    model.addAttribute("title", "Gym Manager | " + title);
    model.addAttribute("header", header);
    model.addAttribute("active", "clientes");
}
    // Eliminar cliente
    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes";
    }

    public long cantidadTotal() {
        return clienteRepository.count();
    }
    // Listado de clientes con inscripciones y actividades

// Añado la página para editar cliente
@GetMapping("/nuevo")
public String nuevoCliente(Model model) {
    // Definimos qué queremos ver en el contenido principal
    model.addAttribute("vista", "fragments/panel-cliente");
    model.addAttribute("fragmento", "panelCliente");
    
    // Datos necesarios para el formulario
    model.addAttribute("cliente", new Cliente());
    model.addAttribute("actividades", actividadRepository.findAll());
    
    // Datos del layout
    prepararModeloBase(model, "Añadir Cliente", "Clientes / Nuevo");
    return "layouts/main";
}

@GetMapping("/editar/{id}")
public String editarCliente(@PathVariable Integer id, Model model) {
    Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

    // Definimos el fragmento del formulario
    // panelClienteTitulo
    model.addAttribute("vista", "fragments/panel-cliente");
    model.addAttribute("fragmento", "panelCliente");
    
    model.addAttribute("cliente", cliente);
    model.addAttribute("actividades", actividadRepository.findAll());
    
    prepararModeloBase(model, "Editar Cliente", "Clientes / Editar " + cliente.getNombre());
    return "layouts/main";
}
}


