package com.gymmanager.gym_manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public String instructores(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "") String q,
                               Model model) {
        Usuario usuario = securityUtils.getUsuarioActual();
        String texto = q.trim();
        Page<Instructor> pagina = instructorRepository.buscarPagina(
                usuario, texto,
                PageRequest.of(Math.max(page, 0), 20, Sort.by("apellido", "nombre").ascending()));

        model.addAttribute("instructores", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", texto);
        model.addAttribute("actividades",  actividadRepository.findByUsuario(usuario));
        model.addAttribute("title",     "Gym Manager | Instructores");
        model.addAttribute("header",    "Panel de control / Instructores");
        model.addAttribute("vista",     "instructores/instructores");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Formulario: nuevo instructor ──────────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevoInstructor(Model model) {
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("title",     "Gym Manager | Nuevo Instructor");
        model.addAttribute("vista",     "instructores/instructor-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Formulario: editar instructor existente ───────────────────────────────
    @GetMapping("/editar/{id}")
    public String editarInstructor(@PathVariable Integer id, Model model,
                                   RedirectAttributes redirectAttributes) {
        Instructor instructor = instructorRepository.findByIdInstructorAndUsuario(
                id,
                securityUtils.getUsuarioActual()
        ).orElse(null);
        if (instructor == null) {
            redirectAttributes.addFlashAttribute("error", "Instructor no encontrado.");
            return "redirect:/instructores";
        }

        model.addAttribute("instructor", instructor);
        model.addAttribute("title",     "Gym Manager | Editar Instructor");
        model.addAttribute("vista",     "instructores/instructor-nuevo");
        model.addAttribute("fragmento", "contenido");
        model.addAttribute("active",    "instructores");
        return "layouts/main";
    }

    // ── Guardar (crear o actualizar) ──────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardarInstructor(@ModelAttribute Instructor instructor,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = securityUtils.getUsuarioActual();

        if (instructor.getIdInstructor() == null) {
            return crearInstructor(instructor, usuario, redirectAttributes);
        }

        return actualizarInstructor(instructor, usuario, redirectAttributes);
    }

    private String crearInstructor(Instructor datosFormulario,
                                   Usuario usuario,
                                   RedirectAttributes redirectAttributes) {
        if (instructorRepository.existsByDniAndUsuario(
                datosFormulario.getDni(), usuario)) {
            redirectAttributes.addFlashAttribute(
                    "error", "El DNI ya está registrado."
            );
            return "redirect:/instructores/nuevo";
        }

        Instructor nuevoInstructor = new Instructor();
        copiarDatosPermitidos(datosFormulario, nuevoInstructor);
        nuevoInstructor.setUsuario(usuario);

        instructorRepository.save(nuevoInstructor);

        redirectAttributes.addFlashAttribute(
                "success", "Instructor guardado con éxito."
        );
        return "redirect:/instructores";
    }

    private String actualizarInstructor(Instructor datosFormulario,
                                        Usuario usuario,
                                        RedirectAttributes redirectAttributes) {
        Instructor instructorDb = instructorRepository
                .findByIdInstructorAndUsuario(
                        datosFormulario.getIdInstructor(),
                        usuario
                )
                .orElse(null);

        if (instructorDb == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Instructor no encontrado."
            );
            return "redirect:/instructores";
        }

        copiarDatosPermitidos(datosFormulario, instructorDb);
        instructorRepository.save(instructorDb);

        redirectAttributes.addFlashAttribute(
                "success", "Instructor actualizado con éxito."
        );
        return "redirect:/instructores";
    }

    private void copiarDatosPermitidos(Instructor origen, Instructor destino) {
        destino.setNombre(origen.getNombre());
        destino.setApellido(origen.getApellido());
        destino.setDni(origen.getDni());
        destino.setTelefono(origen.getTelefono());
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    @PostMapping("/eliminar/{id}")
    public String eliminarInstructor(@PathVariable Integer id,
                                     RedirectAttributes redirectAttributes) {
        Instructor instructor = instructorRepository.findByIdInstructorAndUsuario(
                id,
                securityUtils.getUsuarioActual()
        ).orElse(null);
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
