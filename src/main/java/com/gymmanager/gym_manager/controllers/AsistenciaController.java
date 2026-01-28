package com.gymmanager.gym_manager.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteActividadRepository;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {
    ActividadRepository actividadRepository; 
    ClienteActividadRepository clienteActividadRepository;
public AsistenciaController(ActividadRepository actividadRepository, ClienteActividadRepository clienteActividadRepository) {
        this.actividadRepository = actividadRepository;
        this.clienteActividadRepository = clienteActividadRepository;
    }
    @GetMapping
    public String asistencias(Model model) {
        model.addAttribute("title", "Gym Manager | Asistencias");
        model.addAttribute("header", "Panel de control / Asistencias");

        model.addAttribute("vista", "asistencias");
        model.addAttribute("fragmento", "contenido");

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

        // Fecha de hoy por defecto
        model.addAttribute("fechaHoy", LocalDate.now());
        
        // Listas para el formulario
        // AHORA SÍ FUNCIONARÁ porque actividadRepository ya no es null
        model.addAttribute("actividades", actividadRepository.findAll());
        
        // Traemos las suscripciones activas para armar la lista
        model.addAttribute("suscripciones", clienteActividadRepository.findAll()); 
        
        return "layouts/main";
    }

    // Endpoint para guardar (Solo el esqueleto para que el form no de error)
    @PostMapping("/guardar")
    public String guardarAsistencia(@RequestParam("fecha") String fecha) {
        // Aquí iría la lógica para guardar los presentes
        System.out.println("Guardando asistencia del día: " + fecha);
        return "redirect:/asistencias?exito=true";
    }
}



