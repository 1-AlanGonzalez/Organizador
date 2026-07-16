package com.gymmanager.gym_manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GymManagerApplicationTests {
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private MetodoDePagoRepository metodoDePagoRepository;

	@Test
	void inicializaAdministradorYMetodosDePagoEnUnaBaseNueva() {
		Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

		assertThat(admin.getRol()).isEqualTo("ROLE_ADMIN");
		assertThat(metodoDePagoRepository.findByUsuario(admin))
				.extracting("nombre")
				.containsExactlyInAnyOrder(
						"NO_ESPECIFICADO",
						"EFECTIVO",
						"TRANSFERENCIA",
						"TARJETA/CREDITO");
	}

}
