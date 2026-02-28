package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.DeudaDTO;
import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;
import com.gymmanager.gym_manager.services.ConfiguracionDePagoService;
import com.gymmanager.gym_manager.services.PagoService;

@Controller
@RequestMapping("/ingresos")
public class IngresosController {

    private static final Logger log = LoggerFactory.getLogger(IngresosController.class);

    private final ClienteRepository          clienteRepository;
    private final PagoRepository             pagoRepository;
    private final PagoService                pagoService;
    private final MetodoDePagoRepository     metodoDePagoRepository;
    private final ConfiguracionDePagoService configuracionDePagoService;
    private final SecurityUtils              securityUtils;

    public IngresosController(ConfiguracionDePagoService configuracionDePagoService,
                               ClienteRepository          clienteRepository,
                               PagoService                pagoService,
                               PagoRepository             pagoRepository,
                               MetodoDePagoRepository     metodoDePagoRepository,
                               SecurityUtils              securityUtils) {
        this.pagoRepository             = pagoRepository;
        this.pagoService                = pagoService;
        this.metodoDePagoRepository     = metodoDePagoRepository;
        this.clienteRepository          = clienteRepository;
        this.configuracionDePagoService = configuracionDePagoService;
        this.securityUtils              = securityUtils;
    }

    @GetMapping
    public String ingresos(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        BigDecimal total      = pagoRepository.sumTotalRecaudado(usuario);
        BigDecimal pendientes = pagoRepository.sumTotalPendiente(usuario);

        MetodoDePago efectivo      = metodoDePagoRepository.findByNombreAndUsuario("EFECTIVO",      usuario).orElse(null);
        MetodoDePago transferencia = metodoDePagoRepository.findByNombreAndUsuario("TRANSFERENCIA", usuario).orElse(null);

        BigDecimal totalEfectivo      = efectivo      != null ? pagoRepository.sumPorMetodo(efectivo,      usuario) : BigDecimal.ZERO;
        BigDecimal totalTransferencia = transferencia != null ? pagoRepository.sumPorMetodo(transferencia, usuario) : BigDecimal.ZERO;

        model.addAttribute("ingresosTotales",       total      != null ? total      : BigDecimal.ZERO);
        model.addAttribute("ingresosEfectivo",       totalEfectivo);
        model.addAttribute("ingresosTransferencia",  totalTransferencia);
        model.addAttribute("ingresosPendientes",     pendientes != null ? pendientes : BigDecimal.ZERO);

        model.addAttribute("datosGrafico",      pagoRepository.obtenerIngresosMensuales(usuario.getId()));
        model.addAttribute("categoriasGrafico", Arrays.asList(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));

        model.addAttribute("pagosRecientes", pagoRepository.findByActividadCliente_Cliente_Usuario(usuario));

        model.addAttribute("title",     "Gym Manager | Ingresos");
        model.addAttribute("header",    "Contabilidad / Resumen de Ingresos");
        model.addAttribute("vista",     "ingresos");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "ingresos");
        return "layouts/main";
    }

    @GetMapping("/nuevo")
    public String nuevoIngreso(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();
        model.addAttribute("metodosPago", configuracionDePagoService.listarActivos(usuario));
        model.addAttribute("clientes",    clienteRepository.findByUsuario(usuario));
        model.addAttribute("title",     "Gym Manager | Nuevo Ingreso");
        model.addAttribute("header",    "Contabilidad / Nuevo Ingreso");
        model.addAttribute("vista",     "ingresos-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "ingresos");
        return "layouts/main";
    }

    @PostMapping("/guardar")
    public String guardarIngreso(@RequestParam Integer idActividadCliente,
                                  @RequestParam Integer metodoPagoId,
                                  @RequestParam(required = false) String observaciones,
                                  RedirectAttributes flash) {
        try {
            Pago pago = pagoService.procesarPago(idActividadCliente, metodoPagoId, observaciones);
            // ← redirige al ticket en vez de solo mostrar "éxito"
            return "redirect:/pagos/ticket/" + pago.getIdPago();
        } catch (Exception e) {
            log.error("Error al registrar pago: {}", e.getMessage(), e);
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingresos";
        }
    }

    @GetMapping("/deudas")
    @ResponseBody
    public List<DeudaDTO> obtenerDeudas(@RequestParam Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow()
                .getInscripciones()
                .stream()
                .map(insc -> new DeudaDTO(
                        insc.getIdActividadCliente(),
                        insc.getActividad().getNombre(),
                        insc.calcularAdeudado()))
                .filter(d -> d.montoAdeudado().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }
}