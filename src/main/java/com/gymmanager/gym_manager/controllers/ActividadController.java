package com.gymmanager.gym_manager.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private final ActividadRepository  actividadRepository;
    private final InstructorRepository instructorRepository;
    private final SecurityUtils        securityUtils;

    public ActividadController(ActividadRepository  actividadRepository,
                               InstructorRepository instructorRepository,
                               SecurityUtils        securityUtils) {
        this.actividadRepository  = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.securityUtils        = securityUtils;
    }

    // ── Helper: construye instructoresJson para el JS ─────────────────────────
    private List<Map<String, Object>> buildInstructoresJson(Usuario usuario) {
        return instructorRepository.findByUsuario(usuario).stream()
                .map(i -> Map.<String, Object>of(
                        "id",     i.getIdInstructor(),
                        "nombre", i.getNombre() + " " + i.getApellido()
                ))
                .toList();
    }

    // ── Listado ───────────────────────────────────────────────────────────────
    @GetMapping
    public String actividades(Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("actividades",      actividadRepository.findByUsuario(usuario));
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
        Usuario usuario = securityUtils.getUsuarioActual();

        model.addAttribute("actividad",        new Actividad());
        model.addAttribute("instructoresJson", buildInstructoresJson(usuario));
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
        Usuario usuario = securityUtils.getUsuarioActual();

        Actividad actividad = actividadRepository.findById(id).orElse(null);
        if (actividad == null) {
            redirectAttributes.addFlashAttribute("error", "Actividad no encontrada.");
            return "redirect:/actividades";
        }

        // Serializar dictados como maps simples para que el JS pueda pre-rellenar las filas
        List<Map<String, Object>> dictadosJson = actividad.getDictados().stream()
                .map(d -> Map.<String, Object>of(
                        "instructorId", d.getInstructor().getIdInstructor(),
                        "dias",         d.getDias()    != null ? d.getDias()    : "",
                        "horario",      d.getHorario() != null ? d.getHorario() : ""
                ))
                .toList();

        model.addAttribute("actividad",        actividad);
        model.addAttribute("instructoresJson", buildInstructoresJson(usuario));
        model.addAttribute("dictadosJson",     dictadosJson);
        model.addAttribute("title",     "Gym Manager | Editar Actividad");
        model.addAttribute("vista",     "actividades-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "actividades");
        return "layouts/main";
    }

    // ── Crear o actualizar ────────────────────────────────────────────────────
    @PostMapping("/guardar")
    @Transactional
    public String guardarActividad(
            @RequestParam(required = false) Integer idActividad,
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam(required = false) BigDecimal precioDiario,
            @RequestParam Integer cupoMaximo,
            @RequestParam List<Integer> instructorIds,
            @RequestParam List<String>  dias,
            @RequestParam List<String>  horarios,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = securityUtils.getUsuarioActual();
            Actividad actividad;

            if (idActividad != null) {
                actividad = actividadRepository.findById(idActividad)
                        .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
                actividad.setNombre(nombre);
                actividad.setPrecio(precio);
                actividad.setPrecioDiario(precioDiario != null ? precioDiario : BigDecimal.ZERO);
                actividad.setCupoMaximo(cupoMaximo);

                actividad.getDictados().clear();
                actividadRepository.saveAndFlush(actividad);
            } else {
                actividad = new Actividad(nombre, cupoMaximo, precio,
                        precioDiario != null ? precioDiario : BigDecimal.ZERO);
                actividad.setUsuario(usuario);
                actividad = actividadRepository.save(actividad);
            }

            for (int i = 0; i < instructorIds.size(); i++) {
                Instructor instructor = instructorRepository.findById(instructorIds.get(i))
                        .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));
                actividad.getDictados().add(
                        new Dicta(actividad, instructor, dias.get(i), horarios.get(i)));
            }

            actividadRepository.save(actividad);
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

    // ── DTOs (se mantienen por si se usan en otro lugar) ─────────────────────
    public record DictaDTO(Integer instructorId, String dias, String horario) {}

    public record ActividadEditDTO(
            Integer        id,
            String         nombre,
            BigDecimal     precio,
            BigDecimal     precioDiario,
            Integer        cupoMaximo,
            List<DictaDTO> instructores
    ) {}
}