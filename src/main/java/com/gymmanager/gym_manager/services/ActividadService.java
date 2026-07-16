package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


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
        List<AsignacionInstructor> asignaciones = validarYResolverAsignaciones(
                instructorIds, dias, horarios, usuario);
        Actividad actividad;

        if (idActividad != null) {
            actividad = actividadRepository.findByIdActividadAndUsuario(idActividad, usuario)
                        .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
            actividad.setNombre(nombre);
            actividad.setPrecio(precio);
            actividad.setPrecioDiario(precioDiario != null ? precioDiario : BigDecimal.ZERO);
            actividad.setCupoMaximo(cupoMaximo);

            actualizarAsignaciones(actividad, asignaciones);
            } else {
                actividad = new Actividad(nombre, cupoMaximo, precio,
                        precioDiario != null ? precioDiario : BigDecimal.ZERO);
                actividad.setUsuario(usuario);
                actividad = actividadRepository.save(actividad);
                for (AsignacionInstructor asignacion : asignaciones) {
                    actividad.agregarDictado(nuevoDictado(actividad, asignacion));
                }
            }

            actividadRepository.saveAndFlush(actividad);
    }

    private void actualizarAsignaciones(Actividad actividad,
                                         List<AsignacionInstructor> asignaciones) {
        List<Dicta> existentes = new ArrayList<>(actividad.getDictados());
        Set<Dicta> utilizados = Collections.newSetFromMap(new java.util.IdentityHashMap<>());

        for (AsignacionInstructor asignacion : asignaciones) {
            Dicta dicta = buscarReutilizable(existentes, utilizados, asignacion);
            if (dicta == null) {
                actividad.agregarDictado(nuevoDictado(actividad, asignacion));
                continue;
            }

            dicta.setInstructor(asignacion.instructor());
            dicta.setDias(asignacion.dias());
            dicta.setHorario(asignacion.horario());
            utilizados.add(dicta);
        }

        existentes.stream()
                .filter(dicta -> !utilizados.contains(dicta))
                .forEach(actividad::quitarDictado);
    }

    private Dicta buscarReutilizable(List<Dicta> existentes, Set<Dicta> utilizados,
                                     AsignacionInstructor asignacion) {
        return existentes.stream()
                .filter(dicta -> !utilizados.contains(dicta))
                .filter(dicta -> dicta.getInstructor().getIdInstructor()
                        .equals(asignacion.instructor().getIdInstructor()))
                .findFirst()
                .orElseGet(() -> existentes.stream()
                        .filter(dicta -> !utilizados.contains(dicta))
                        .findFirst()
                        .orElse(null));
    }

    private Dicta nuevoDictado(Actividad actividad, AsignacionInstructor asignacion) {
        return new Dicta(actividad, asignacion.instructor(),
                asignacion.dias(), asignacion.horario());
    }

    public Page<Actividad> buscarPagina(int pagina, int cantidad, String texto) {
        return actividadRepository.buscarPagina(
                securityUtils.getUsuarioActual(), texto,
                PageRequest.of(pagina, cantidad, Sort.by("nombre").ascending()));
    }

    private List<AsignacionInstructor> validarYResolverAsignaciones(
            List<Integer> instructorIds, List<String> dias, List<String> horarios, Usuario usuario) {
        if (instructorIds == null || dias == null || horarios == null
                || instructorIds.isEmpty()
                || instructorIds.size() != dias.size()
                || instructorIds.size() != horarios.size()) {
            throw new IllegalArgumentException("Las asignaciones de instructores y horarios están incompletas.");
        }

        Set<String> combinaciones = new HashSet<>();
        List<AsignacionInstructor> asignaciones = new ArrayList<>();
        for (int i = 0; i < instructorIds.size(); i++) {
            Integer instructorId = instructorIds.get(i);
            String diasNormalizados = normalizarTexto(dias.get(i));
            String horarioNormalizado = normalizarTexto(horarios.get(i));
            if (instructorId == null || diasNormalizados.isEmpty() || horarioNormalizado.isEmpty()) {
                throw new IllegalArgumentException("Cada asignación debe tener instructor, días y horario.");
            }

            String clave = instructorId + "|"
                    + diasNormalizados.toLowerCase(Locale.ROOT) + "|"
                    + horarioNormalizado.toLowerCase(Locale.ROOT);
            if (!combinaciones.add(clave)) {
                throw new IllegalArgumentException(
                        "El mismo instructor no puede repetirse con los mismos días y horario.");
            }

            Instructor instructor = instructorRepository
                    .findByIdInstructorAndUsuario(instructorId, usuario)
                    .orElseThrow(() -> new IllegalArgumentException("Instructor no encontrado."));
            asignaciones.add(new AsignacionInstructor(instructor, diasNormalizados, horarioNormalizado));
        }
        return asignaciones;
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
    }

    private record AsignacionInstructor(Instructor instructor, String dias, String horario) {
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
