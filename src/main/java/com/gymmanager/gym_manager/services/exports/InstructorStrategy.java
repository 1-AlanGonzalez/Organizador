package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.InstructorRepository;

@Component
public class InstructorStrategy implements ExportStrategy {

    private final InstructorRepository instructorRepository;

    public InstructorStrategy(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "instructor";
    }

    @Override
    public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {

        List<Instructor> instructores = instructorRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();

        for (Instructor instructor : instructores) {

            List<Map<String, Object>> filasInstructor =
                    ExportMapper.mapearEntidad(instructor, request.getAtributos());

            filas.addAll(filasInstructor);
        }

        return filas;
    }
}
