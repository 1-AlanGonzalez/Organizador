package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.DictaRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

@Controller
@RequestMapping("/instructores")
public class InstructorController {

    private final InstructorRepository instructorRepository;
    private final ActividadRepository  actividadRepository;
    private final DictaRepository      dictaRepository;
    private final SecurityUtils        securityUtils;

    public InstructorController(InstructorRepository instructorRepository,
                                ActividadRepository  actividadRepository,
                                DictaRepository      dictaRepository,
                                SecurityUtils        securityUtils) {
        this.instructorRepository = instructorRepository;
        this.actividadRepository  = actividadRepository;
        this.dictaRepository      = dictaRepository;
        this.securityUtils        = securityUtils;
    }

    // ── Listado ───────────────────────────────────────────────────────────────
    @GetMapping
    public String instructores(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("instructores", instructorRepository.findByUsuario(usuario));
        model.addAttribute("actividades",  actividadRepository.findByUsuario(usuario));
        model.addAttribute("title",     "Gym Manager | Instructores");
        model.addAttribute("header",    "Panel de control / Instructores");
        model.addAttribute("vista",     "instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Formulario: nuevo instructor ──────────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevoInstructor(Model model) {
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("title",     "Gym Manager | Nuevo Instructor");
        model.addAttribute("vista",     "instructores-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Formulario: editar instructor existente ───────────────────────────────
    @GetMapping("/editar/{id}")
    public String editarInstructor(@PathVariable Integer id, Model model,
                                   RedirectAttributes redirectAttributes) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);
        if (instructor == null) {
            redirectAttributes.addFlashAttribute("error", "Instructor no encontrado.");
            return "redirect:/instructores";
        }

        model.addAttribute("instructor", instructor);
        model.addAttribute("title",     "Gym Manager | Editar Instructor");
        model.addAttribute("vista",     "instructores-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Guardar (crear o actualizar) ──────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardarInstructor(@ModelAttribute Instructor instructor,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();

        // Validar DNI duplicado solo en creación
        if (instructor.getIdInstructor() == null &&
                instructorRepository.existsByDniAndUsuario(instructor.getDni(), usuario)) {
            redirectAttributes.addFlashAttribute("error", "El DNI ya está registrado.");
            return "redirect:/instructores/nuevo";
        }

        instructor.setUsuario(usuario);
        instructorRepository.save(instructor);
        redirectAttributes.addFlashAttribute("success", "Instructor guardado con éxito.");
        return "redirect:/instructores";
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @PostMapping("/eliminar/{id}")
    public String eliminarInstructor(@PathVariable Integer id,
                                     RedirectAttributes redirectAttributes) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);
        if (instructor == null) {
            redirectAttributes.addFlashAttribute("error", "Instructor no encontrado.");
            return "redirect:/instructores";
        }
        if (dictaRepository.existsByInstructor(instructor)) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar el instructor porque tiene actividades asignadas.");
            return "redirect:/instructores";
        }
        instructorRepository.delete(instructor);
        redirectAttributes.addFlashAttribute("success", "Instructor eliminado correctamente.");
        return "redirect:/instructores";
    }
}