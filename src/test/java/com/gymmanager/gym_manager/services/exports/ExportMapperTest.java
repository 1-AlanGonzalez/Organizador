package com.gymmanager.gym_manager.services.exports;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.Instructor;

class ExportMapperTest {

    @Test
    void noRepiteInstructorCuandoDictaLaActividadEnVariosHorarios() {
        Actividad actividad = new Actividad(
                "Funcional", 20, new BigDecimal("15000"), new BigDecimal("2000"));
        Instructor instructor = new Instructor("Ana", "Pérez", "123", "456");
        actividad.agregarDictado(new Dicta(actividad, instructor, "Lunes", "18:00"));
        actividad.agregarDictado(new Dicta(actividad, instructor, "Miércoles", "20:00"));

        List<Map<String, Object>> filas = ExportMapper.mapearEntidad(
                actividad, List.of("nombre", "dictados.instructor.nombre"));

        assertThat(filas).hasSize(1);
        assertThat(filas.get(0))
                .containsEntry("Nombre de Actividad", "Funcional")
                .containsEntry("Nombre de Instructor", "Ana");
    }

    @Test
    void combinaValoresMultiplesSinMultiplicarFilas() {
        Actividad actividad = new Actividad(
                "Boxeo", 15, new BigDecimal("18000"), new BigDecimal("2500"));
        Instructor ana = new Instructor("Ana", "Pérez", "123", "456");
        Instructor leo = new Instructor("Leo", "Gómez", "789", "012");
        actividad.agregarDictado(new Dicta(actividad, ana, "Lunes", "18:00"));
        actividad.agregarDictado(new Dicta(actividad, leo, "Martes", "20:00"));

        List<Map<String, Object>> filas = ExportMapper.mapearEntidad(
                actividad, List.of("dictados.horario", "dictados.instructor.nombre"));

        assertThat(filas).hasSize(1);
        assertThat(String.valueOf(filas.get(0).get("Horario de Dictado")))
                .contains("18:00", "20:00");
        assertThat(String.valueOf(filas.get(0).get("Nombre de Instructor")))
                .contains("Ana", "Leo");
    }
}
