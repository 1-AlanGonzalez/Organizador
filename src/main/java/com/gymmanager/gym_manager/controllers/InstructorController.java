package com.gymmanager.gym_manager.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;


@Controller
@RequestMapping("/instructores")
public class InstructorController {

    private final InstructorRepository instructorRepository;
    // Importo actividad para poder darle una o más al instructor al crearlo
    private final ActividadRepository actividadRepository;

    public InstructorController(InstructorRepository instructorRepository, ActividadRepository actividadRepository) {
        this.instructorRepository = instructorRepository;
        this.actividadRepository = actividadRepository;
    }


    @GetMapping
    public String instructores(Model model) {
        model.addAttribute("instructores", instructorRepository.findAll());
        model.addAttribute("actividades", actividadRepository.findAll()); 
        
        model.addAttribute("title", "Gym Manager | Instructores");
        model.addAttribute("header", "Panel de control / Instructores");
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("vista", "instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "instructores");
        System.out.println("Actividades: " + actividadRepository.findAll().size());

        return "layouts/main";
    }
    // Guardar instructor

    @PostMapping("/guardar")
    public String guardarInstructor(
            @ModelAttribute Instructor instructor,
            @RequestParam List<Long> actividadIds,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (instructorRepository.existsByDni(instructor.getDni())) {
                // ERROR: No redireccionamos. Cargamos el modelo y devolvemos la vista.
                prepararModelo(model);
                model.addAttribute("error", "El DNI ya está registrado para otro instructor.");
                model.addAttribute("abrirPanel", true); 
                return "layouts/main"; 
            }

            instructorRepository.save(instructor);
            
            // ÉXITO: Aquí sí redireccionamos para limpiar el formulario y refrescar la tabla.
            redirectAttributes.addFlashAttribute("success", "Instructor guardado con éxito.");
            return "redirect:/instructores";

        } catch (Exception e) {
            prepararModelo(model);
            model.addAttribute("error", "Error inesperado: " + e.getMessage());
            model.addAttribute("abrirPanelInstructor", true);
            return "layouts/main";
        }
    }

    private void prepararModelo(Model model) {
        model.addAttribute("instructores", instructorRepository.findAll());
        model.addAttribute("instructor", new Instructor());
    model.addAttribute("actividades", actividadRepository.findAll()); // 👈 CLAVE
        model.addAttribute("title", "Gym Manager | Instructores");
        model.addAttribute("header", "Panel de control / Instructores");
        model.addAttribute("vista", "instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "instructores");
    }
}
