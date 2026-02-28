package com.gymmanager.gym_manager.config;

import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Order(1)
public class SetupLoader {

    // Lee la contraseña de una variable de entorno.
    // Si no existe la variable, usa "admin123" como fallback SOLO para desarrollo.
    // En producción: export APP_ADMIN_PASSWORD=ContraseñaSegura
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode(adminPassword));
                admin.setNombreGimnasio("Administrador");
                admin.setRol("ROLE_ADMIN");  // ← el único ADMIN del sistema
                repo.save(admin);
            }
        };
    }
}