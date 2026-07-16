package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.entity.dto.ReporteAsistenciaDTO;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.services.AsistenciaService;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    private final ActividadRepository        actividadRepository;
    private final ClienteActividadRepository clienteActividadRepository;
    private final AsistenciaService          asistenciaService;
    private final AsistenciaRepository       asistenciaRepository;
    private final SecurityUtils              securityUtils;

    public AsistenciaController(AsistenciaRepository       asistenciaRepository,
                                ActividadRepository        actividadRepository,
                                ClienteActividadRepository clienteActividadRepository,
                                AsistenciaService          asistenciaService,
                                SecurityUtils              securityUtils) {
        this.actividadRepository        = actividadRepository;
        this.clienteActividadRepository = clienteActividadRepository;
        this.asistenciaService          = asistenciaService;
        this.asistenciaRepository       = asistenciaRepository;
        this.securityUtils              = securityUtils;
    }

   // DESPUÉS — en AsistenciaController.java
@GetMapping
public String asistencias(Model model,
                           @RequestParam(required = false) String fecha,
                           @RequestParam(required = false) Integer idActividad,
                           @RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int page) {
    Usuario usuario = securityUtils.getUsuarioActual();
    LocalDate fechaReporte = asistenciaService.parsearFecha(fecha);

    Page<ReporteAsistenciaDTO> reporte = asistenciaService.generarReporteDiarioPaginado(
            fechaReporte, idActividad, q.trim(), usuario,
            PageRequest.of(Math.max(page, 0), 20));

    model.addAttribute("title",             "Gym Manager | Asistencias");
    model.addAttribute("header",            "Panel de control / Asistencias");
    model.addAttribute("actividades",       actividadRepository.findByUsuario(usuario));
    model.addAttribute("reporteAsistencia", reporte.getContent());
    model.addAttribute("pagina", reporte);
    model.addAttribute("q", q.trim());
    model.addAttribute("idActividadSeleccionada", idActividad);
    model.addAttribute("fechaSeleccionada", fechaReporte.toString()); // para que el input muestre la fecha elegida
    model.addAttribute("vista",             "asistencias/asistencias");
    model.addAttribute("fragmento",         "contenido");
    model.addAttribute("active",            "asistencias");
    return "layouts/main";
}

    @GetMapping("/tomar")
    public String tomarAsistencia(@RequestParam(defaultValue = "") String q,
                                  @RequestParam(required = false) Integer idActividad,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) LocalDate fecha,
                                  Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("title",      "Gym Manager | Tomar Asistencia");
        model.addAttribute("header",     "Asistencias / Nueva Planilla");
        model.addAttribute("vista",      "asistencias/asistencias-tomar");
        model.addAttribute("fragmento",  "contenido");
        model.addAttribute("active",     "asistencias");
        model.addAttribute("fechaHoy",   fecha != null ? fecha : LocalDate.now());
        model.addAttribute("actividades", actividadRepository.findByUsuario(usuario)); 

        Page<ActividadCliente> pagina = clienteActividadRepository.buscarActivas(
                EstadoInscripcion.ACTIVA, usuario, q.trim(), idActividad,
                PageRequest.of(Math.max(page, 0), 20, Sort.by("cliente.apellido").ascending()));
        model.addAttribute("inscripciones", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q.trim());
        model.addAttribute("idActividadSeleccionada", idActividad);
        return "layouts/main";
    }

    @PostMapping("/guardar")
    public String guardarAsistencia(
            @RequestParam LocalDate fecha,
            @RequestParam(name = "presentes",    required = false) List<Integer> presentesIds,
            @RequestParam(name = "todosLosIds",  required = false) List<Integer> todosLosIds) {
        asistenciaService.guardarAsistencia(fecha, presentesIds, todosLosIds);
        return "redirect:/asistencias";
    }
}
