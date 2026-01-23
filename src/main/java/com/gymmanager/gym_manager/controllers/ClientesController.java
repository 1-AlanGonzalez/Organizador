package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
// import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
// import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    
    // Inyección del repositorio de ActividadCliente para poder 
    // verificar inscripciones al guardar un cliente
    private final ClienteActividadRepository clienteActividadRepository;

    public ClientesController(ClienteRepository clienteRepository, ClienteActividadRepository clienteActividadRepository, ActividadRepository actividadRepository) {
        this.clienteRepository = clienteRepository;
        this.clienteActividadRepository = clienteActividadRepository;
        this.actividadRepository = actividadRepository;
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
    
@PostMapping("/guardar")
public String guardarCliente(
    @ModelAttribute Cliente cliente, 
    // RequestParam para la actividad seleccionada
    @RequestParam Integer idActividad,
    Model model, 
    RedirectAttributes redirectAttributes) {
    try {
    //    Comprobar si el DNI ya existe en otro cliente
        boolean dniExiste = clienteRepository.existsByDni(cliente.getDni());

        // Si es un nuevo cliente (idCliente es null) y el DNI ya existe, mostrar error

        if (cliente.getIdCliente() == null && dniExiste) {
            // ERROR: No redireccionamos. Cargamos el modelo y devolvemos la vista.
            prepararModelo(model);
            model.addAttribute("error", "El DNI ya está registrado para otro cliente.");
            model.addAttribute("abrirPanel", true); // Señal para el HTML
            return "layouts/main"; 
        }
        // Guardamos el cliente 
        Cliente clienteGuardado = clienteRepository.save(cliente);
        
        // Asignar la actividad seleccionada al cliente guardado
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        // Crear la inscripción del cliente en la actividad
        ActividadCliente inscripcion = new ActividadCliente(
                LocalDate.now(),
                actividad.getPrecio(),
                clienteGuardado,
                actividad
        );
        // Guardar la inscripción
        clienteActividadRepository.save(inscripcion);

        // ÉXITO: Aquí sí redireccionamos para limpiar el formulario y refrescar la tabla.
        redirectAttributes.addFlashAttribute("success", "Cliente guardado con éxito.");
        return "redirect:/clientes";

    } catch (Exception e) {
        prepararModelo(model);
        model.addAttribute("error", "Error inesperado: " + e.getMessage());
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

    // Eliminar cliente
    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes";
    }

    public long cantidadTotal() {
        return clienteRepository.count();
    }

}


