package com.gymmanager.gym_manager.controllers;


// import java.math.BigDecimal;
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
    
    // NUEVO HOY 4/2 
    /* Al crear un cliente hay un botón de "registrar pago"
     * Para poder registrarlo necesito que en el controller existan variables y datos para enviar y recibir datos del pago
     */


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
        // Recibimos una lista porque en el HTML el input date está dentro del th:each
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") List<LocalDate> fechaInicio,
        @RequestParam("tipoDeCobro") String tipoDeCobroString,
        // Nuevos campos para el pago
        @RequestParam(required = false) Boolean registrarPago,
        @RequestParam(required = false) Double montoAbonado,
        @RequestParam(required = false) String metodoPago,
        @RequestParam(required = false) String observacionPago,
        Model model,
        RedirectAttributes redirectAttributes) {

    try {
        TipoDeCobro tipoDeCobro = TipoDeCobro.valueOf(tipoDeCobroString);

        LocalDate fechaInicioReal = LocalDate.now();
        if (fechaInicio != null) {
            fechaInicioReal = fechaInicio.stream()
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(LocalDate.now());
        }
        clienteService.guardarOActualizarCliente(
            cliente, 
            idActividades, 
            fechaInicioReal, 
            tipoDeCobro,
            registrarPago,
            montoAbonado,
            metodoPago,
            observacionPago
        );

        redirectAttributes.addFlashAttribute("success", 
            cliente.getIdCliente() != null ? "Cliente actualizado correctamente." : "Cliente registrado e inscripto.");

        return "redirect:/clientes";

    } catch (IllegalArgumentException e) {
        model.addAttribute("error", "Datos incorrectos: " + e.getMessage());
        return "layouts/main";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", e.getMessage());
        model.addAttribute("cliente", cliente);
        // Importante: volver a cargar las actividades si falla para que el form se vea bien
        // model.addAttribute("actividades", actividadRepository.findAll()); 
        return "layouts/main"; // Asegúrate de retornar la vista correcta donde está el formulario
    }
}

// Método auxiliar para evitar repetir código en los métodos del controlador
// private void prepararModelo(Model model) {
//     model.addAttribute("clientes", clienteRepository.findAll());
//     model.addAttribute("title", "Gym Manager | Clientes");
//     model.addAttribute("header", "Panel de control / Clientes");
//     model.addAttribute("vista", "clientes");
//     model.addAttribute("fragmento", "contenido");
//     model.addAttribute("active", "clientes");
// }
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


