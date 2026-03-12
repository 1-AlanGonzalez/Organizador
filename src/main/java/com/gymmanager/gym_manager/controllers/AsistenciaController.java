package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
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
                           Integer idActividad) {
    Usuario usuario = securityUtils.getUsuarioActual();

    LocalDate fechaReporte = (fecha != null && !fecha.isBlank())
            ? LocalDate.parse(fecha)   // el input type="date" manda yyyy-MM-dd
            : LocalDate.now();

    List<ReporteAsistenciaDTO> reporte =
            asistenciaService.generarReporteDiario(fechaReporte, idActividad, usuario);

    model.addAttribute("title",             "Gym Manager | Asistencias");
    model.addAttribute("header",            "Panel de control / Asistencias");
    model.addAttribute("actividades",       actividadRepository.findByUsuario(usuario));
    model.addAttribute("asistencias",       asistenciaRepository.findByActividadCliente_Cliente_Usuario(usuario));
    model.addAttribute("reporteAsistencia", reporte);
    model.addAttribute("fechaSeleccionada", fechaReporte.toString()); // para que el input muestre la fecha elegida
    model.addAttribute("vista",             "asistencias");
    model.addAttribute("fragmento",         "contenido");
    model.addAttribute("active",            "asistencias");
    return "layouts/main";
}

    @GetMapping("/tomar")
    public String tomarAsistencia(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("title",      "Gym Manager | Tomar Asistencia");
        model.addAttribute("header",     "Asistencias / Nueva Planilla");
        model.addAttribute("vista",      "asistencias-tomar");
        model.addAttribute("fragmento",  "contenido");
        model.addAttribute("active",     "asistencias");
        model.addAttribute("fechaHoy",   LocalDate.now());
        model.addAttribute("actividades", actividadRepository.findByUsuario(usuario)); 

        model.addAttribute("inscripciones",
                clienteActividadRepository.findByEstadoAndCliente_Usuario(
                        EstadoInscripcion.ACTIVA, usuario));                           
        return "layouts/main";
    }

    @PostMapping("/guardar")
    public String guardarAsistencia(
            @RequestParam LocalDate fecha,
            @RequestParam(name = "presentes",    required = false) List<Integer> presentesIds,
            @RequestParam(name = "todosLosIds",  required = false) List<Integer> todosLosIds) {

        if (presentesIds == null) presentesIds = new ArrayList<>();
        if (todosLosIds  == null) todosLosIds  = new ArrayList<>();

        for (Integer id : todosLosIds) {
            boolean estaPresente = presentesIds.contains(id);
            asistenciaService.registrarAsistencia(id, fecha, estaPresente);
        }
        return "redirect:/asistencias";
    }
}