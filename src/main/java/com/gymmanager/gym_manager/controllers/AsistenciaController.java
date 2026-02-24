package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gymmanager.gym_manager.entity.EstadoInscripcion;
import com.gymmanager.gym_manager.entity.dto.ReporteAsistenciaDTO;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.AsistenciaRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;
import com.gymmanager.gym_manager.services.AsistenciaService;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {
    ActividadRepository actividadRepository; 
    ClienteActividadRepository clienteActividadRepository;
    AsistenciaService asistenciaService;
    AsistenciaRepository asistenciaRepository;

    public AsistenciaController(AsistenciaRepository asistenciaRepository, ActividadRepository actividadRepository, ClienteActividadRepository clienteActividadRepository, AsistenciaService asistenciaService) {
        this.actividadRepository = actividadRepository;
        this.clienteActividadRepository = clienteActividadRepository;
        this.asistenciaService = asistenciaService;
        this.asistenciaRepository = asistenciaRepository;
    }
    @GetMapping
    public String asistencias(Model model, Integer idActividad) {
        model.addAttribute("title", "Gym Manager | Asistencias");
        model.addAttribute("header", "Panel de control / Asistencias");
        model.addAttribute("asistencias", asistenciaRepository.findAll());
        model.addAttribute("actividades", actividadRepository.findAll());
        model.addAttribute("vista", "asistencias");
        model.addAttribute("fragmento", "contenido");
        LocalDate fechaReporte = LocalDate.now();
        List<ReporteAsistenciaDTO> reporte = asistenciaService.generarReporteDiario(fechaReporte, idActividad);
            model.addAttribute("reporteAsistencia", reporte);
        model.addAttribute("active", "asistencias");
        return "layouts/main";
    }
   @GetMapping("/tomar")
    public String tomarAsistencia(Model model) {
        model.addAttribute("title", "Gym Manager | Tomar Asistencia");
        model.addAttribute("header", "Asistencias / Nueva Planilla");
        model.addAttribute("vista", "asistencias-tomar"); 
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "asistencias");

        model.addAttribute("fechaHoy", LocalDate.now());
        
        model.addAttribute("actividades", actividadRepository.findAll());
        
        model.addAttribute("inscripciones",
                clienteActividadRepository.findByEstado(EstadoInscripcion.ACTIVA));
            
        return "layouts/main";
    }


    @PostMapping("/guardar")
    public String guardarAsistencia(
        @RequestParam LocalDate fecha,
        @RequestParam(name = "presentes", required = false) List<Integer> presentesIds, // Los que tienen Check
        @RequestParam(name = "todosLosIds", required = false) List<Integer> todosLosIds // TODOS los que había en pantalla
    ) {
        
        // Manejo de nulls: Si no se marcó ninguno, la lista llega null.
        if (presentesIds == null) {
            presentesIds = new ArrayList<>();
        }
        if (todosLosIds == null) {
            todosLosIds = new ArrayList<>();
        }

        // Recorremos TODOS los alumnos que se mostraron en la tabla
        for (Integer id : todosLosIds) {
            // Verificamos: ¿Está este ID en la lista de presentes?
            boolean estaPresente = presentesIds.contains(id);

            // Llamamos al servicio con el resultado (true o false)
            // - Si estaPresente es TRUE: El servicio crea o mantiene.
            // - Si estaPresente es FALSE: El servicio BORRA si existía.
            asistenciaService.registrarAsistencia(id, fecha, estaPresente);
        }

        return "redirect:/asistencias";
    }
}



