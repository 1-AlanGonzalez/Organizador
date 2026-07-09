package com.gymmanager.gym_manager.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.services.ActividadService;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/actividades")
public class ActividadController {
    private final ActividadService actividadService;


    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }



    // ── Listado ───────────────────────────────────────────────────────────────
    @GetMapping
    public String actividades(Model model) {
        model.addAttribute("actividades", actividadService.listarActividadesDeUsuario());
        model.addAttribute("title",     "Gym Manager | Actividades");
        model.addAttribute("header",    "Panel de control / Actividades");
        model.addAttribute("vista",     "actividades");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "actividades");
        return "layouts/main";
    }

    // ── Formulario: nueva actividad ───────────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevaActividad(Model model) {
        model.addAttribute("actividad",        new Actividad());
        model.addAttribute("instructoresJson", actividadService.buildInstructoresJson());
        model.addAttribute("dictadosJson",     List.of());          // sin filas previas
        model.addAttribute("title",     "Gym Manager | Nueva Actividad");
        model.addAttribute("vista",     "actividades-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "actividades");
        return "layouts/main";
    }

    // ── Formulario: editar actividad existente ────────────────────────────────
    @GetMapping("/editar/{id}")
    public String editarActividad(@PathVariable Integer id, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Actividad actividad = actividadService.obtenerActividadDeUsuario(id);

            model.addAttribute("actividad",        actividad);
            model.addAttribute("instructoresJson", actividadService.buildInstructoresJson());
            model.addAttribute("dictadosJson",     actividadService.buildDictadosJson(actividad));
            model.addAttribute("title",     "Gym Manager | Editar Actividad");
            model.addAttribute("vista",     "actividades-nuevo");
            model.addAttribute("fragmento", "contenido");
            model.addAttribute("active",    "actividades");
            return "layouts/main";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/actividades";
        }
    }

    // ── Crear o actualizar ────────────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardarActividad(
            @RequestParam(required = false) Integer idActividad,
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam(required = false) BigDecimal precioDiario,
            @RequestParam(required = false) Integer cupoMaximo,
            @RequestParam List<Integer> instructorIds,
            @RequestParam List<String>  dias,
            @RequestParam List<String>  horarios,
            RedirectAttributes redirectAttributes) {
        try {
            actividadService.guardarActividad(idActividad, nombre, precio, precioDiario, cupoMaximo, instructorIds, dias, horarios);
            redirectAttributes.addFlashAttribute("success",
                    idActividad != null ? "Actividad actualizada correctamente."
                                       : "Actividad creada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/actividades";
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @PostMapping("/eliminar/{id}")
    public String eliminarActividad(@PathVariable Integer id,
                                    RedirectAttributes redirectAttributes) {
        try {
            actividadService.eliminarActividad(id);
            redirectAttributes.addFlashAttribute("success", "Actividad eliminada correctamente");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar la actividad porque hay clientes inscriptos en ella.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/actividades";
    }
}