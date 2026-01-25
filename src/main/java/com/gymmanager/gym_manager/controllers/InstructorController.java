package com.gymmanager.gym_manager.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.DictaRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;


@Controller
@RequestMapping("/instructores")
public class InstructorController {

    private final InstructorRepository instructorRepository;
    // Importo actividad para poder darle una o más al instructor al crearlo
    private final ActividadRepository actividadRepository;

    private final DictaRepository dictaRepository; /* Esto nuevo */

    public InstructorController(
            InstructorRepository instructorRepository,
            ActividadRepository actividadRepository,
            DictaRepository dictaRepository) {

        this.instructorRepository = instructorRepository;
        this.actividadRepository = actividadRepository;
        this.dictaRepository = dictaRepository;
    }


    @GetMapping
    public String instructores(Model model) {
        model.addAttribute("instructores", instructorRepository.findAll());
        model.addAttribute("actividades", actividadRepository.findAll()); 
        model.addAttribute("instructor", new Instructor());

        model.addAttribute("title", "Gym Manager | Instructores");
        model.addAttribute("header", "Panel de control / Instructores");
        model.addAttribute("vista", "instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "instructores");


        return "layouts/main";
    }
    // Guardar instructor
/* 
  @PostMapping("/guardar")
    public String guardarInstructor(
            @ModelAttribute Instructor instructor,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            boolean dniExiste = instructorRepository.existsByDni(instructor.getDni());

            if (instructor.getIdInstructor() == null && dniExiste) {
                prepararModelo(model);
                model.addAttribute("error", "El DNI ya está registrado.");
                model.addAttribute("abrirPanel", true);
                return "layouts/main";
            }

            instructorRepository.save(instructor); // CREA o EDITA

            redirectAttributes.addFlashAttribute("success", "Instructor guardado con éxito");
            return "redirect:/instructores";

        } catch (Exception e) {
            prepararModelo(model);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("abrirPanel", true);
            return "layouts/main";
        }
    }
    // EndPoint para eliminar instructor
    // Eliminar instructor
@PostMapping("/eliminar/{id}")
public String eliminarInstructor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
// Buscar el instructor por ID
    Instructor instructor = instructorRepository.findById(id).orElse(null);

    // Verificar si el instructor existe
    if (instructor == null) {
        redirectAttributes.addFlashAttribute("error", "Instructor no encontrado.");
        return "redirect:/instructores";
    }
    // Verificar si el instructor tiene actividades asignadas
    // Si tiene actividades, no permitir la eliminación
    if (!instructor.getActividades().isEmpty()) {
        redirectAttributes.addFlashAttribute(
            "error",
            "No se puede eliminar el instructor porque tiene actividades asignadas."
        );
        return "redirect:/instructores";
    }
    // Eliminar el instructor
    instructorRepository.delete(instructor);
    redirectAttributes.addFlashAttribute("success", "Instructor eliminado correctamente.");
    return "redirect:/instructores";
}
    
    private void prepararModelo(Model model) {
        model.addAttribute("instructores", instructorRepository.findAll());
        model.addAttribute("actividades", actividadRepository.findAll());
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("title", "Gym Manager | Instructores");
        model.addAttribute("header", "Panel de control / Instructores");
        model.addAttribute("vista", "instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active", "instructores");
    }
        */

    @PostMapping("/guardar")
    public String guardarInstructor(
        @ModelAttribute Instructor instructor,
        RedirectAttributes redirectAttributes) {

    boolean dniExiste = instructorRepository.existsByDni(instructor.getDni());

    if (instructor.getIdInstructor() == null && dniExiste) {
        redirectAttributes.addFlashAttribute("error", "El DNI ya está registrado.");
        return "redirect:/instructores";
    }

    instructorRepository.save(instructor);
    redirectAttributes.addFlashAttribute("success", "Instructor guardado con éxito");
    return "redirect:/instructores";
    }


    @PostMapping("/eliminar/{id}")
    public String eliminarInstructor(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes) {

    Instructor instructor = instructorRepository.findById(id).orElse(null);

    if (instructor == null) {
        redirectAttributes.addFlashAttribute("error", "Instructor no encontrado.");
        return "redirect:/instructores";
    }

    // ✔ la relación real está en Dicta
    if (dictaRepository.existsByInstructor(instructor)) {
        redirectAttributes.addFlashAttribute(
            "error",
            "No se puede eliminar el instructor porque tiene actividades asignadas."
        );
        return "redirect:/instructores";
    }

    instructorRepository.delete(instructor);
    redirectAttributes.addFlashAttribute(
        "success",
        "Instructor eliminado correctamente."
    );

    return "redirect:/instructores";
}
} 

