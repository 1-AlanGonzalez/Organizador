package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ACTIVIDAD")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD")
    private Integer idActividad;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    @ColumnLabel("Nombre de Actividad")
    private String nombre;

    @Column(name = "CUPO_MAXIMO", nullable = true)
    @ColumnLabel("Cupo Maximo")
    private Integer cupoMaximo;

    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    @ColumnLabel("Precio")
    private BigDecimal precio;

    @Column(name = "PRECIO_DIARIO", nullable = false, precision = 10, scale = 2)
    @ColumnLabel("Precio Diario")
    private BigDecimal precioDiario;

    @OneToMany(
        mappedBy = "actividad",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Dicta> dictados = new HashSet<>();

    // Agrego una lista de inscripciones para poder manejar el cupo máximo
    @OneToMany(mappedBy = "actividad")
    private Set<ActividadCliente> inscripciones = new HashSet<>();


    // AÑADIDO PARA CREAR CADA USUARIO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Actividad(String nombre, Integer cupoMaximo, BigDecimal precio, BigDecimal precioDiario) {
        this.nombre = nombre;
        this.cupoMaximo = cupoMaximo;
        this.precio = precio;
        this.precioDiario = precioDiario;
    }
    public void agregarDictado(Dicta dicta) {
    dictados.add(dicta);
    dicta.setActividad(this);
    }

    public void quitarDictado(Dicta dicta) {
        dictados.remove(dicta);
        dicta.setActividad(null);
    }

    /* ================== LÓGICA DE ACTIVIDAD ================== */


    
   
}

