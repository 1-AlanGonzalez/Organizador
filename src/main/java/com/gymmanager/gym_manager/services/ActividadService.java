package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;


import com.gymmanager.gym_manager.config.SecurityUtils;
import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;

import jakarta.transaction.Transactional;

@Service
public class ActividadService {

    private final ActividadRepository  actividadRepository;
    private final InstructorRepository instructorRepository;
    private final SecurityUtils securityUtils;

    public ActividadService(ActividadRepository  actividadRepository,
                            InstructorRepository instructorRepository,
                            SecurityUtils securityUtils) {
        this.actividadRepository  = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.securityUtils = securityUtils;
    }

    public List<Actividad> listarActividadesDeUsuario() {
        return actividadRepository.findByUsuario(securityUtils.getUsuarioActual());
    }

    @Transactional
    public void guardarActividad(Integer idActividad,
            String nombre,
            BigDecimal precio,
            BigDecimal precioDiario,
            Integer cupoMaximo,
            List<Integer> instructorIds,
            List<String>  dias,
            List<String>  horarios
            ) {

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
    }


    public Actividad obtenerActividadDeUsuario(Integer id) {
    return actividadRepository.findByIdActividadAndUsuario(id, securityUtils.getUsuarioActual())
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
}

    public List<Map<String, Object>> buildDictadosJson(Actividad actividad) {
    return actividad.getDictados().stream()
            .map(d -> Map.<String, Object>of(
                    "instructorId", d.getInstructor().getIdInstructor(),
                    "dias",         d.getDias()    != null ? d.getDias()    : "",
                    "horario",      d.getHorario() != null ? d.getHorario() : ""
            ))
            .toList();
    }


    public List<Map<String, Object>> buildInstructoresJson() {
        Usuario usuario = securityUtils.getUsuarioActual();
        return instructorRepository.findByUsuario(usuario).stream()
                .map(i -> Map.<String, Object>of(
                        "id",     i.getIdInstructor(),
                        "nombre", i.getNombre() + " " + i.getApellido()
                ))
                .toList();
    }

    public void eliminarActividad(Integer id) {
        Actividad actividad = obtenerActividadDeUsuario(id);
        actividadRepository.delete(actividad);
    }


}