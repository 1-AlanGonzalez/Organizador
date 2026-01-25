package com.gymmanager.gym_manager.controllers;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import com.gymmanager.gym_manager.entity.Actividad;

import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private ActividadRepository actividadRepository;
    private InstructorRepository instructorRepository;

    public ActividadController(ActividadRepository actividadRepository, InstructorRepository instructorRepository) {
        this.actividadRepository = actividadRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping
    public String actividades(Model model) {
        model.addAttribute("actividades", actividadRepository.findAll());
        model.addAttribute("instructores", instructorRepository.findAll());

        model.addAttribute("title", "Gym Manager | Actividades");
        model.addAttribute("header", "Panel de control / Actividades");
        model.addAttribute("actividad", new Actividad());

        model.addAttribute("vista", "actividades");
        model.addAttribute("fragmento", "contenido");

        model.addAttribute("active", "actividades");
        
        return "layouts/main";
    }

    // @PostMapping("/guardar")
    // public String guardadActividad(@ModelAttribute Actividad actividad) {
    //     actividadRepository.save(actividad);
    //     return "redirect:/actividades";
    // }
    /* 
    @PostMapping("/guardar")
        public String guardarActividad(
            // Para guardar la actividad junto con el instructor seleccionado
                @ModelAttribute Actividad actividad,
                @RequestParam(required = false) Integer instructorId
        ) {
            // Si se seleccionó un instructor, lo buscamos y lo asignamos a la actividad
            if (instructorId != null) {
                Instructor instructor = instructorRepository
                        .findById(instructorId)
                        .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

                actividad.setInstructor(instructor); 
            }

            actividadRepository.save(actividad);
            return "redirect:/actividades";
        }
        */
    @PostMapping("/guardar")
    public String guardarActividad(@ModelAttribute Actividad actividad) {
        actividadRepository.save(actividad);
        return "redirect:/actividades";
    }

}


