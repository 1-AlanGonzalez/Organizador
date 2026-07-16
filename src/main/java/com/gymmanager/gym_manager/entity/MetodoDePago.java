package com.gymmanager.gym_manager.entity;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"usuario_id", "nombre"}
        )
)
public class MetodoDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_METODO")
    private Integer idMetodoDePago;

    @Column(nullable = false)
    @ColumnLabel("Metodo Utilizado")
    private String nombre;

    // AÑADIDO PARA CREAR CADA USUARIO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public MetodoDePago(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
    
}
