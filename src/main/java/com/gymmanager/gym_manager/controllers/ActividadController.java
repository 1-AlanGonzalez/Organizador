package com.gymmanager.gym_manager.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;
import com.gymmanager.gym_manager.services.ActividadService;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private final ActividadRepository  actividadRepository;
    private final InstructorRepository instructorRepository;
    private final ActividadService     actividadService;  
    private final SecurityUtils        securityUtils;

    public ActividadController(ActividadRepository  actividadRepository,
                               InstructorRepository instructorRepository,
                               ActividadService     actividadService,
                               SecurityUtils        securityUtils) {
        this.actividadRepository  = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.actividadService     = actividadService;
        this.securityUtils        = securityUtils;
    }

    @GetMapping
    public String actividades(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();
        model.addAttribute("actividades",  actividadRepository.findByUsuario(usuario));
        model.addAttribute("instructores", instructorRepository.findByUsuario(usuario));
        model.addAttribute("actividad",    new Actividad());
        model.addAttribute("title",     "Gym Manager | Actividades");
        model.addAttribute("header",    "Panel de control / Actividades");
        model.addAttribute("vista",     "actividades");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "actividades");
        return "layouts/main";
    }

    // ← punto 9: sin @Transactional — eso le corresponde al service
    @PostMapping("/guardar")
    public String guardarActividad(
            @ModelAttribute Actividad actividad,
            @RequestParam Integer instructorId,
            @RequestParam String dias,
            @RequestParam String horario,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = securityUtils.getUsuarioActual();
            actividadService.guardarActividad(actividad, instructorId, dias, horario, usuario);
            redirectAttributes.addFlashAttribute("success", "Actividad guardada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/actividades";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarActividad(@PathVariable Integer id,
                                    RedirectAttributes redirectAttributes) {
        Actividad actividad = actividadRepository.findById(id).orElse(null);
        if (actividad == null) {
            redirectAttributes.addFlashAttribute("error", "Actividad no encontrada");
            return "redirect:/actividades";
        }
        try {
            actividadRepository.delete(actividad);
            redirectAttributes.addFlashAttribute("success", "Actividad eliminada correctamente");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar la actividad porque hay clientes inscriptos en ella.");
        }
        return "redirect:/actividades";
    }
}