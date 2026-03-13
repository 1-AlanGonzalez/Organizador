package com.gymmanager.gym_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String nombreGimnasio;

    @Column
    private String direccion;

    @Column
    private String telefono;

    @Column
    private String mensajeTicket;

    @Column(nullable = false)
    private String rol = "ROLE_USER";

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }
}