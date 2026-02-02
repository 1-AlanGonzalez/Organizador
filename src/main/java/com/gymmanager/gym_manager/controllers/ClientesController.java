package com.gymmanager.gym_manager.controllers;


import java.time.LocalDate;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.gymmanager.gym_manager.entity.TipoDeCobro;
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
     CAMBIOS EN EL /GUARDAR ---> Solución para EDITAR un cliente
     El problema está en cómo Spring bindea el formulario (@ModelAttribute Cliente cliente) cuando editás, 
     combinado con @Transactional y colecciones JPA.
     Cuando editás un cliente, Spring crea una nueva instancia de Cliente incompleta

     NO usar @ModelAttribute Cliente para editar
    Para editar, solo se recibe el ID + campos simples
    La entidad SIEMPRE se trabaja desde la DB
     */

    @PostMapping("/guardar")
    public String guardarCliente(
            @ModelAttribute Cliente cliente,
            @RequestParam(required = false) List<Integer> idActividades,
            // DateTimeFormat lo utilicé para transformar el localdate porque no me lo reconocía como tal
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam("tipoDeCobro") String tipoDeCobroString, 
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // // Acá transformé el tipoDeCobro que traje como String para que no hayan errores (Según entendí, venía como string).
             TipoDeCobro tipoDeCobro = TipoDeCobro.valueOf(tipoDeCobroString);

            // // Si existe el cliente -> Editar (actualizarCliente) -> Llevando consigo al cliente traído desde el formulario Editar
            // //  las actividades que estoy guardando, la fecha de inicio y el tipoDeCobro por si es modificado
            // if (cliente.getIdCliente() != null && cliente.getIdCliente() > 0) {
            //     clienteService.actualizarCliente(cliente, idActividades, fechaInicio, tipoDeCobro);
            //     redirectAttributes.addFlashAttribute("success", "Cliente actualizado y plan procesado.");
            // } else {
            //     if (fechaInicio == null) {
            //         throw new RuntimeException("La fecha de inicio es obligatoria.");
            //     }
            //     clienteService.registrarClienteEInscribir(cliente, idActividades, fechaInicio, tipoDeCobro);
            //     redirectAttributes.addFlashAttribute("success", "Cliente registrado e inscripto.");
            // }

            clienteService.guardarOActualizarCliente(cliente, idActividades, fechaInicio, tipoDeCobro);
            redirectAttributes.addFlashAttribute("succes", 
                cliente.getIdCliente() != null ? "Cliente actualizado y plan procesado." : "Cliente registrado e incripto.");

            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Tipo de cobro no válido.");
            prepararModelo(model); 
            return "layouts/main";
        } catch (Exception e) {
            prepararModelo(model);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cliente", cliente);
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
    public String eliminarCliente(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
        clienteService.eliminarCliente(id);
        redirectAttributes.addFlashAttribute("success", "Cliente dado de baja correctamente");
        } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

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
    // 🔍 DEBUG: ver estados reales
    cliente.getInscripciones().forEach(i ->
        System.out.println(
            "Actividad ID: " + i.getActividad().getIdActividad()
            + " | Estado: " + i.getEstado()
        )
    );
    model.addAttribute("vista", "fragments/panel-cliente");
    model.addAttribute("fragmento", "panelCliente");

    // ✅ ACÁ MISMO
    model.addAttribute("cliente", cliente);

    model.addAttribute("actividades", actividadRepository.findAll());

    prepararModeloBase(model, "Editar Cliente", "Clientes / Editar " + cliente.getNombre());
    return "layouts/main";
}
    @GetMapping("/ver/{id}") // O la ruta que estés usando
    public String verCliente(@PathVariable Integer id, Model model) {
        // ... lógica para buscar cliente y pagos ...
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        // Variables de datos
        model.addAttribute("cliente", cliente);
        // model.addAttribute("historialPagos", pagos);
        
        // Variables para el Layout
        model.addAttribute("titulo", "Detalle de Cliente");
        model.addAttribute("header", "Información del Cliente");
        
        model.addAttribute("vista", "clientes/ver_cliente"); // Ruta al archivo hijo
        model.addAttribute("fragmento", "detalle_cliente");  // Nombre del th:fragment dentro del hijo
        return "layouts/main"; // Nombre del archivo PADRE (layout.html)
    }
}


