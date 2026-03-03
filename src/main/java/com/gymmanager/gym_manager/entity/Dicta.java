package com.gymmanager.gym_manager.entity;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "INSTRUCTOR_ACTIVIDAD",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ID_ACTIVIDAD", "ID_INSTRUCTOR"})
    }
)
public class Dicta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSTRUCTOR_ACTIVIDAD")
    private Integer idInstructorActividad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_ACTIVIDAD", nullable = false)
    private Actividad actividad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_INSTRUCTOR", nullable = false)
    private Instructor instructor;

    @Column(name = "DIAS", nullable = false, length = 50)
    @ColumnLabel("Dias de Dictado")
    private String dias;

    @Column(name = "HORARIO", nullable = false, length = 50)
    @ColumnLabel("Horario de Dictado")
    private String horario;

    public Dicta(Actividad actividad, Instructor instructor, String dias, String horario) {
        this.actividad = actividad;
        this.instructor = instructor;
        this.dias = dias;
        this.horario = horario;
    }

}


