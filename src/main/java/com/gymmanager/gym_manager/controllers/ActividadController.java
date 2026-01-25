package com.gymmanager.gym_manager.controllers;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.DictaRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/actividades")
public class ActividadController {
    private DictaRepository dictaRepository;
    private ActividadRepository actividadRepository;
    private InstructorRepository instructorRepository;
    public ActividadController(ActividadRepository actividadRepository, InstructorRepository instructorRepository, 
    DictaRepository dictaRepository
    ) {
        this.actividadRepository = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.dictaRepository = dictaRepository;
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

    // Al guardar la actividad necesito crear un nuevo Dicta para asignar el instructor, dias y horario
@PostMapping("/guardar")
// Transactional para que si algo falla en el medio, no quede nada guardado a medias
@Transactional
public String guardarActividad(
        @ModelAttribute Actividad actividad,
        @RequestParam Integer instructorId,
        @RequestParam String dias,
        @RequestParam String horario
) {
    Instructor instructor = instructorRepository.findById(instructorId)
            .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

    // 1️⃣ GUARDO PRIMERO LA ACTIVIDAD (si es nueva)
    if (actividad.getIdActividad() == null) {
        actividad = actividadRepository.save(actividad);
    }

    // 2️⃣ BUSCO SI YA EXISTE EL DICTA
    Dicta dicta = dictaRepository
            .findByActividadAndInstructor(actividad, instructor)
            .orElse(new Dicta());

    dicta.setActividad(actividad);
    dicta.setInstructor(instructor);
    dicta.setDias(dias);
    dicta.setHorario(horario);

    dictaRepository.save(dicta);

    return "redirect:/actividades";
}

@PostMapping("/eliminar/{id}")
public String eliminarActividad(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
) {
    Actividad actividad = actividadRepository.findById(id).orElse(null);

    if (actividad == null) {
        redirectAttributes.addFlashAttribute(
            "error", "Actividad no encontrada"
        );
        return "redirect:/actividades";
    }

    actividadRepository.delete(actividad);

    redirectAttributes.addFlashAttribute(
        "success", "Actividad eliminada correctamente"
    );

    return "redirect:/actividades";
}
}


