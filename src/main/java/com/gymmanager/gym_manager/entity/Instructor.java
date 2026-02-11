package com.gymmanager.gym_manager.entity;


import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.JoinTable;
// import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "INSTRUCTOR")
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "ID_INSTRUCTOR")
    private Integer idInstructor;
    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;
    @Column(name = "APELLIDO", nullable = false, length = 50)
    private String apellido;
    @Column(name = "DNI", nullable = false)
    private String dni;
    @Column(name = "TELEFONO", nullable = false, length = 20)
    private String telefono;

    // Muchos a muchos entre instructor y actividad
    @OneToMany(mappedBy = "instructor")
    private Set<Dicta> dictados = new HashSet<>();

    public Instructor(String nombre, String apellido, String dni, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono; }
    
    
}
