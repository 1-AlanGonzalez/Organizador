package com.gymmanager.gym_manager.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;

@Component
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    public SecurityUtils(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado no encontrado en DB: " + auth.getName()));
    }
}
