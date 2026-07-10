package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Cliente;
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

    // ── GET /ingresos ─────────────────────────────────────────────────────────
    // Carga la página con stats del mes actual por defecto

    @GetMapping
    public String ingresos(Model model) {
        Usuario   usuario = securityUtils.getUsuarioActual();
        YearMonth mesActual = YearMonth.now();

        agregarStatsAlModelo(model, usuario, mesActual);

        model.addAttribute("mesSeleccionado",    mesActual.toString()); // "2025-03"
        model.addAttribute("pagosRecientes",     pagoRepository.findPagosVisibles(usuario));
        model.addAttribute("datosGrafico",       pagoRepository.obtenerIngresosMensuales(usuario.getId()));
        model.addAttribute("categoriasGrafico",  Arrays.asList(
                "Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"));

        model.addAttribute("title",     "Gym Manager | Ingresos");
        model.addAttribute("header",    "Contabilidad / Resumen de Ingresos");
        model.addAttribute("vista",     "ingresos/ingresos");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "ingresos");
        return "layouts/main";
    }

    // ── GET /ingresos/stats?mes=2025-03 ──────────────────────────────────────
    // Devuelve JSON con las 4 stats del mes — lo llama el JS al cambiar el filtro

    @GetMapping("/stats")
    @ResponseBody
    public Map<String, BigDecimal> statsMensuales(@RequestParam String mes) {
        Usuario   usuario   = securityUtils.getUsuarioActual();
        YearMonth yearMonth = YearMonth.parse(mes);

        LocalDate desde = yearMonth.atDay(1);
        LocalDate hasta = yearMonth.atEndOfMonth();

        MetodoDePago efectivo      = metodoDePagoRepository.findByNombreAndUsuario("EFECTIVO",      usuario).orElse(null);
        MetodoDePago transferencia = metodoDePagoRepository.findByNombreAndUsuario("TRANSFERENCIA", usuario).orElse(null);

        BigDecimal total    = pagoRepository.sumTotalRecaudadoEntreFechas(usuario, desde, hasta);
        BigDecimal pendiente = pagoRepository.sumTotalPendienteEntreFechas(usuario, desde, hasta);
        BigDecimal efect    = efectivo      != null ? pagoRepository.sumPorMetodoEntreFechas(efectivo,      usuario, desde, hasta) : BigDecimal.ZERO;
        BigDecimal transf   = transferencia != null ? pagoRepository.sumPorMetodoEntreFechas(transferencia, usuario, desde, hasta) : BigDecimal.ZERO;

        return Map.of(
                "total",        total      != null ? total      : BigDecimal.ZERO,
                "efectivo",     efect,
                "transferencia",transf,
                "pendiente",    pendiente  != null ? pendiente  : BigDecimal.ZERO
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void agregarStatsAlModelo(Model model, Usuario usuario, YearMonth mes) {
        LocalDate desde = mes.atDay(1);
        LocalDate hasta = mes.atEndOfMonth();

        MetodoDePago efectivo      = metodoDePagoRepository.findByNombreAndUsuario("EFECTIVO",      usuario).orElse(null);
        MetodoDePago transferencia = metodoDePagoRepository.findByNombreAndUsuario("TRANSFERENCIA", usuario).orElse(null);

        BigDecimal total     = pagoRepository.sumTotalRecaudadoEntreFechas(usuario, desde, hasta);
        BigDecimal pendiente = pagoRepository.sumTotalPendienteEntreFechas(usuario, desde, hasta);
        BigDecimal efect     = efectivo      != null ? pagoRepository.sumPorMetodoEntreFechas(efectivo,      usuario, desde, hasta) : BigDecimal.ZERO;
        BigDecimal transf    = transferencia != null ? pagoRepository.sumPorMetodoEntreFechas(transferencia, usuario, desde, hasta) : BigDecimal.ZERO;

        model.addAttribute("ingresosTotales",      total      != null ? total      : BigDecimal.ZERO);
        model.addAttribute("ingresosEfectivo",      efect);
        model.addAttribute("ingresosTransferencia", transf);
        model.addAttribute("ingresosPendientes",    pendiente  != null ? pendiente  : BigDecimal.ZERO);
    }

// ── GET /ingresos/nuevo ───────────────────────────────────────────────────────
// Solo pasa clientes que tienen al menos una deuda pendiente

@GetMapping("/nuevo")
public String nuevoIngreso(Model model) {
    Usuario usuario = securityUtils.getUsuarioActual();

    // Filtrar en memoria: solo clientes con al menos un pago ADEUDA > 0
    List<Cliente> clientesConDeuda = clienteRepository.findByUsuario(usuario)
            .stream()
            .filter(c -> c.getInscripciones().stream()
                    .anyMatch(i -> i.calcularAdeudado().compareTo(BigDecimal.ZERO) > 0))
            .toList();

    model.addAttribute("metodosPago",    configuracionDePagoService.listarActivos(usuario));
    model.addAttribute("clientes",       clientesConDeuda);
    model.addAttribute("title",     "Gym Manager | Nuevo Ingreso");
    model.addAttribute("header",    "Contabilidad / Nuevo Ingreso");
    model.addAttribute("vista",     "ingresos/ingresos-nuevo");
    model.addAttribute("fragmento", "contenido");
    model.addAttribute("active",    "ingresos");
    return "layouts/main";
}

    // ── POST /ingresos/guardar ────────────────────────────────────────────────

    @PostMapping("/guardar")
    public String guardarIngreso(@RequestParam Integer idActividadCliente,
                                  @RequestParam Integer metodoPagoId,
                                  @RequestParam(required = false) String observaciones,
                                  @RequestParam (required = false) LocalDate fechaDePago,
                                  RedirectAttributes flash) {
        System.out.println("ActividadCliente: " + idActividadCliente);
        System.out.println("MetodoPago: " + metodoPagoId);
        System.out.println("Observaciones: " + observaciones);
        System.out.println("Fecha de Pago: " + fechaDePago);

        try {
            Pago pago = pagoService.procesarPago(idActividadCliente, metodoPagoId, observaciones, fechaDePago);
            return "redirect:/pagos/ticket/" + pago.getIdPago();
        } catch (Exception e) {
            log.error("Error al registrar pago: {}", e.getMessage(), e);
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingresos";
        }
    }

    // ── GET /ingresos/deudas ──────────────────────────────────────────────────

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