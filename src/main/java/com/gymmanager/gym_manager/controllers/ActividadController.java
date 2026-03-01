package com.gymmanager.gym_manager.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
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

        // ── Devuelve datos de una actividad como JSON para prellenar el modal ─────
    @GetMapping("/editar/{id}")
    @ResponseBody
    public ActividadEditDTO obtenerParaEditar(@PathVariable Integer id) {
        Actividad a = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        Integer instructorId = null;
        String  dias         = "";
        String  horario      = "";

        // dictados es Set<Dicta> — no tiene .get(), usamos stream
        if (a.getDictados() != null && !a.getDictados().isEmpty()) {
            Dicta dicta = a.getDictados().stream().findFirst().get();
            instructorId = dicta.getInstructor().getIdInstructor();
            dias         = dicta.getDias()    != null ? dicta.getDias()    : "";
            horario      = dicta.getHorario() != null ? dicta.getHorario() : "";
        }

        return new ActividadEditDTO(
                a.getIdActividad(),
                a.getNombre(),
                a.getPrecio(),
                a.getPrecioDiario(),
                a.getCupoMaximo(),
                instructorId,
                dias,
                horario
        );
    }

    public record ActividadEditDTO(
            Integer    id,
            String     nombre,
            java.math.BigDecimal precio,
            java.math.BigDecimal precioDiario,
            Integer    cupoMaximo,
            Integer    instructorId,
            String     dias,
            String     horario
    ) {}


    // ── Crear o actualizar ────────────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardarActividad(
            @ModelAttribute Actividad actividad,
            @RequestParam Integer instructorId,
            @RequestParam String dias,
            @RequestParam String horario,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = securityUtils.getUsuarioActual();

            // Si tiene ID es una edición: preservar el usuario original
            if (actividad.getIdActividad() != null) {
                Actividad existente = actividadRepository.findById(actividad.getIdActividad())
                        .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
                actividad.setUsuario(existente.getUsuario());
            }

            actividadService.guardarActividad(actividad, instructorId, dias, horario, usuario);
            redirectAttributes.addFlashAttribute("success",
                    actividad.getIdActividad() != null
                            ? "Actividad actualizada correctamente."
                            : "Actividad creada correctamente.");
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