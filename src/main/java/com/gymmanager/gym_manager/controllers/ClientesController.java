package com.gymmanager.gym_manager.controllers;


// import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;
import com.gymmanager.gym_manager.services.ClienteService;
import com.gymmanager.gym_manager.services.ConfiguracionDePagoService;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final ClienteService clienteService;
    private final PagoRepository pagoRepository;
    private final ConfiguracionDePagoService configuracionDePagoService;
    private final SecurityUtils securityUtils;


    // NUEVO HOY 4/2 
    /* Al crear un cliente hay un botón de "registrar pago"
     * Para poder registrarlo necesito que en el controller existan variables y datos para enviar y recibir datos del pago
     */


    public ClientesController(PagoRepository pagoRepository, ClienteRepository clienteRepository, ActividadRepository actividadRepository,
            ClienteService clienteService, ConfiguracionDePagoService configuracionDePagoService, SecurityUtils securityUtils) {
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.clienteService = clienteService;
        this.configuracionDePagoService = configuracionDePagoService;
        this.pagoRepository = pagoRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String clientes(Model model) {
        
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
        model.addAttribute("clientes", clienteRepository.findByUsuario(usuario));
        model.addAttribute("cliente", new Cliente());
        
        // model.addAttribute("metodosPago", configuracionDePagoService.listarActivos());
        model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));

        model.addAttribute("title", "Gym Manager | Clientes");
        model.addAttribute("header", "Panel de control / Clientes");
        model.addAttribute("vista", "clientes");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "clientes");

        return "layouts/main";
    }

    @PostMapping("/guardar")
    public String guardarCliente(
            @ModelAttribute Cliente cliente,
            @RequestParam(required = false) List<Integer> idActividades,
            @RequestParam(required = false) Map<String, String> fechaInicioMap,
            @RequestParam("tipoDeCobro") String tipoDeCobroString,
            @RequestParam(required = false) Boolean registrarPago,
            @RequestParam(required = false) Double montoAbonado,
            @RequestParam(required = false) MetodoDePago metodoPagoId,
            @RequestParam(required = false) String observacionPago,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = securityUtils.getUsuarioActual();
            TipoDeCobro tipoDeCobro = TipoDeCobro.valueOf(tipoDeCobroString);

            Map<Integer, LocalDate> fechasPorActividad = new HashMap<>();
            if (fechaInicioMap != null) {
                fechaInicioMap.forEach((key, value) -> {
                    // La clave llega como "fechaInicioMap[3]", no como "3"
                    if (key.startsWith("fechaInicioMap[") && value != null && !value.isEmpty()) {
                        try {
                            String idStr = key.replace("fechaInicioMap[", "").replace("]", "");
                            Integer actId = Integer.parseInt(idStr);
                            fechasPorActividad.put(actId, LocalDate.parse(value));
                        } catch (Exception e) {
                            // System.out.println("Error parseando fecha para clave: " + key + " → " + e.getMessage());
                        }
                    }
                });
            }
            // Asignar el usuario dueño antes de guardar
            cliente.setUsuario(usuario);

            clienteService.guardarOActualizarCliente(
                cliente, 
                idActividades, 
                fechasPorActividad, 
                tipoDeCobro,
                registrarPago,
                montoAbonado,
                metodoPagoId,
                observacionPago
            );

            redirectAttributes.addFlashAttribute("success", 
                cliente.getIdCliente() != null ? "Cliente actualizado correctamente." : "Cliente registrado e inscripto.");

            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Datos incorrectos: " + e.getMessage());
            return volverFormulario(model, cliente, e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cliente", cliente);
            return volverFormulario(model, cliente, e.getMessage());
        }
        }
        private String volverFormulario(Model model, Cliente cliente, String errorMsg) {
            Usuario usuario = securityUtils.getUsuarioActual();

            // Spring binding convierte idCliente="" a 0 en vez de null.
            // Si es 0 o negativo, es un cliente nuevo — reseteamos a null
            // para que Thymeleaf no lo muestre como modo edición.
            if (cliente.getIdCliente() != null && cliente.getIdCliente() <= 0) {
                cliente.setIdCliente(null);
            }

            model.addAttribute("error", errorMsg);
            model.addAttribute("cliente", cliente);
            model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
            model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));
            model.addAttribute("vista", "fragments/panel-cliente");
            model.addAttribute("fragmento", "panelCliente");
            prepararModeloBase(model, "Añadir Cliente", "Clientes / Nuevo");

            return "layouts/main";
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

    @GetMapping("/nuevo")
        public String nuevoCliente(Model model) {
            Usuario usuario = securityUtils.getUsuarioActual();
            model.addAttribute("vista", "fragments/panel-cliente");
            model.addAttribute("fragmento", "panelCliente");
            model.addAttribute("cliente", new Cliente());
            model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
            model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));
            prepararModeloBase(model, "Añadir Cliente", "Clientes / Nuevo");

            return "layouts/main";
        }

    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Integer id, Model model) {

        Usuario usuario = securityUtils.getUsuarioActual();

        // findByIdClienteAndUsuario garantiza que no puedas ver clientes ajenos
        Cliente cliente = clienteRepository.findByIdClienteAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        model.addAttribute("vista",      "fragments/panel-cliente");
        model.addAttribute("fragmento",  "panelCliente");
        model.addAttribute("cliente",    cliente);
        model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
        model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));

        prepararModeloBase(model, "Editar Cliente", "Clientes / Editar " + cliente.getNombre());
        return "layouts/main";
    }

  @GetMapping("/ver/{id}")
    public String verCliente(@PathVariable Integer id, Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        Cliente cliente = clienteRepository.findByIdClienteAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        List<Pago> pagos = pagoRepository
                .findByActividadCliente_Cliente_IdClienteOrderByFechaGeneracionDesc(id);
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("historialPagos", pagos);
        model.addAttribute("title", "Gym Manager | Detalle Cliente");
        model.addAttribute("header", "Clientes / " + cliente.getNombre() + " " + cliente.getApellido());
        model.addAttribute("active", "clientes");
        model.addAttribute("vista", "clientes/ver_cliente"); 
        model.addAttribute("fragmento", "detalle_cliente"); 
        
        return "layouts/main"; 
    }

    private void prepararModeloBase(Model model, String title, String header) {
        model.addAttribute("title", "Gym Manager | " + title);
        model.addAttribute("header", header);
        model.addAttribute("active", "clientes");
    }
}


