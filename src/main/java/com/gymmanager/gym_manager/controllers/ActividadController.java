package com.gymmanager.gym_manager.controllers;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private ActividadRepository actividadRepository;
    private InstructorRepository instructorRepository;

    public ActividadController(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
        
    }

    @GetMapping
    public String actividades(Model model) {
        model.addAttribute("actividades", actividadRepository.findAll());

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

    @PostMapping("/guardar")
        public String guardarActividad(
                @ModelAttribute Actividad actividad,
                @RequestParam(required = false) Long instructorId
        ) {
            if (instructorId != null) {
                Instructor instructor = instructorRepository
                        .findById(instructorId)
                        .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

                actividad.setInstructor(instructor); 
            }

            actividadRepository.save(actividad);
            return "redirect:/actividades";
        }

    // Vista para el selector de actividades del cliente:
    // @GetMapping
    // public String vista(Model model) {

    //     model.addAttribute("actividades", actividadRepository.findAll());
    //     model.addAttribute("cliente", new Cliente());

    //     return "layouts/main";
    // }
}


