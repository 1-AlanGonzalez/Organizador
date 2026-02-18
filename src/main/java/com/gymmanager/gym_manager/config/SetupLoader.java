package com.gymmanager.gym_manager.config;

import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SetupLoader {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            // Solo creamos el usuario si no existe ninguno
            if (repo.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                // AQUÍ LA MAGIA: Encriptamos la contraseña antes de guardar
                admin.setPassword(encoder.encode("admin123"));

                repo.save(admin);
                System.out.println("Usuario ADMIN creado: admin / admin123");
            }
        };
    }
}