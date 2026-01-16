package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Cliente;
// import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    private final ClienteRepository clienteRepository;
    // private ActividadRepository actividadRepository;

    public ClientesController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }


    @GetMapping
    public String clientes(Model model) {

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
    public String guardarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            // Iterar en toda la tabla Clientes buscando un DNI similar, si existe lanzamos error
            if (clienteRepository.existsByDni(cliente.getDni())) {
                // Aquí podrías agregar un mensaje de error al modelo si lo deseas
                redirectAttributes.addFlashAttribute("error", "El DNI ya está registrado para otro cliente.");
                return "redirect:/clientes";
            }
            clienteRepository.save(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente guardado con éxito.");
        }
        catch (Exception e) {
            // Aquí podrías manejar la excepción y agregar un mensaje de error al modelo si lo deseas
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    // Eliminar cliente
    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes";
    }



//     // Inscribir cliente a una actividad
//     @PostMapping("/{clienteId}/inscribir/{actividadId}")
// public String inscribirActividad(@PathVariable Integer clienteId, 
//                                  @PathVariable Integer actividadId, 
//                                  RedirectAttributes redirectAttributes) {
//     try {
//         // 1. Buscar el cliente y la actividad
//         Cliente cliente = clienteRepository.findById(clienteId)
//                 .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
//         Actividad actividad = actividadRepository.findById(actividadId)
//                 .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

//         // 2. Intentar la lógica de inscripción (aquí saltará la excepción si ya existe)
//         cliente.inscribirseAUnaActividad(actividad);

//         // 3. Guardar cambios
//         clienteRepository.save(cliente);

//         // 4. Mensaje de éxito
//         redirectAttributes.addFlashAttribute("success", "Inscripción realizada con éxito.");

//     } catch (RuntimeException e) {
//         // 5. CAPTURA DEL ERROR: Aquí atrapas el throw que hiciste en la Entidad
//         redirectAttributes.addFlashAttribute("error", e.getMessage());
//     }

//     return "redirect:/clientes";
// }
}


