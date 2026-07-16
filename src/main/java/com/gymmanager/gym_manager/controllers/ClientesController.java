package com.gymmanager.gym_manager.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public String clientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String actividad,
            Model model) {
        
        Usuario usuario = securityUtils.getUsuarioActual();
        int paginaSolicitada = Math.max(page, 0);
        String textoFiltro = q == null ? "" : q.trim();
        String actividadFiltro = actividad == null ? "" : actividad.trim();
        Page<Cliente> paginaClientes = clienteRepository.buscarPagina(
                usuario, textoFiltro, actividadFiltro,
                PageRequest.of(paginaSolicitada, 20, Sort.by("apellido", "nombre").ascending()));

        int paginaActual = paginaClientes.getNumber();
        int totalPaginas = paginaClientes.getTotalPages();
        int paginaInicial = Math.max(0, paginaActual - 2);
        int paginaFinal = Math.min(Math.max(totalPaginas - 1, 0), paginaActual + 2);

        model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
        model.addAttribute("clientes", paginaClientes.getContent());
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalClientes", paginaClientes.getTotalElements());
        model.addAttribute("paginaInicial", paginaInicial);
        model.addAttribute("paginaFinal", paginaFinal);
        model.addAttribute("q", textoFiltro);
        model.addAttribute("actividadFiltro", actividadFiltro);
        model.addAttribute("primerCliente", paginaClientes.isEmpty() ? 0 : paginaActual * 20 + 1);
        model.addAttribute("ultimoCliente", paginaClientes.isEmpty() ? 0
                : paginaActual * 20 + paginaClientes.getNumberOfElements());
        model.addAttribute("cliente", new Cliente());
        
        model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));

        model.addAttribute("title", "Gym Manager | Clientes");
        model.addAttribute("header", "Panel de control / Clientes");
        model.addAttribute("vista", "clientes/clientes");
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
            @RequestParam(required = false) MetodoDePago metodoPagoId,
            @RequestParam(required = false) String observacionPago,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            boolean esActualizacion = cliente.getIdCliente() != null;
            clienteService.procesarGuardado(tipoDeCobroString, fechaInicioMap, cliente, idActividades, registrarPago, metodoPagoId, observacionPago);
            redirectAttributes.addFlashAttribute("success",
                esActualizacion ? "Cliente actualizado correctamente." : "Cliente registrado e inscripto.");
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
            model.addAttribute("vista", "clientes/fragments/panel-cliente");
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
            model.addAttribute("vista", "clientes/fragments/panel-cliente");
            model.addAttribute("fragmento", "panelCliente");
            model.addAttribute("cliente", new Cliente());
            model.addAttribute("actividades", actividadRepository.findByUsuario(usuario));
            model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));
            prepararModeloBase(model, "Añadir Cliente", "Clientes / Nuevo");

            return "layouts/main";
        }

    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.obtenerClienteDeUsuario(id);
        Usuario usuario = securityUtils.getUsuarioActual();
        model.addAttribute("fechasInscripcion", clienteService.fechaInscripcionModel(cliente));
        model.addAttribute("vista",      "clientes/fragments/panel-cliente");
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

        Cliente cliente = clienteService.obtenerClienteDeUsuario(id);
        List<Pago> pagos = pagoRepository
                .findByActividadCliente_Cliente_IdClienteAndActividadCliente_Cliente_UsuarioOrderByFechaGeneracionDesc(
                id,
                usuario
        );
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


