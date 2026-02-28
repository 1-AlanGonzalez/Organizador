package com.gymmanager.gym_manager.services;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.DictaRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

import jakarta.transaction.Transactional;

@Service
public class ActividadService {

    private final ActividadRepository  actividadRepository;
    private final InstructorRepository instructorRepository;
    private final DictaRepository      dictaRepository;

    public ActividadService(ActividadRepository  actividadRepository,
                            InstructorRepository instructorRepository,
                            DictaRepository      dictaRepository) {
        this.actividadRepository  = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.dictaRepository      = dictaRepository;
    }

    @Transactional
    public void guardarActividad(Actividad actividad, Integer instructorId,
                                  String dias, String horario, Usuario usuario) {

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        if (actividad.getIdActividad() == null) {
            actividad.setUsuario(usuario);
            actividad = actividadRepository.save(actividad);
        }

        Dicta dicta = dictaRepository
                .findByActividadAndInstructor(actividad, instructor)
                .orElse(new Dicta());

        dicta.setActividad(actividad);
        dicta.setInstructor(instructor);
        dicta.setDias(dias);
        dicta.setHorario(horario);
        dictaRepository.save(dicta);
    }
}