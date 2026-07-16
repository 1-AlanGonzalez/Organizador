package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ActividadRepository;
import com.gymmanager.gym_manager.repository.ClienteRepository;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.InstructorRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.UsuarioRepository;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final InstructorRepository instructorRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfiguracionDePagoService configuracionDePagoService;

    public UsuarioAdminService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            ActividadRepository actividadRepository,
            InstructorRepository instructorRepository,
            MetodoDePagoRepository metodoDePagoRepository,
            ConfiguracionPagoRepository configuracionPagoRepository,
            PasswordEncoder passwordEncoder,
            ConfiguracionDePagoService configuracionDePagoService
            ) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.actividadRepository = actividadRepository;
        this.instructorRepository = instructorRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
        this.configuracionPagoRepository = configuracionPagoRepository;
        this.configuracionDePagoService = configuracionDePagoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void eliminarGimnasio(Integer usuarioId, Integer administradorId) {
        if (administradorId.equals(usuarioId)) {
            throw new IllegalArgumentException("No podés eliminar tu propio usuario.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        if ("ROLE_ADMIN".equals(usuario.getRol())) {
            throw new IllegalArgumentException("No se puede eliminar una cuenta administrativa.");
        }

        // Los clientes eliminan en cascada inscripciones, pagos y asistencias.
        clienteRepository.deleteAll(clienteRepository.findByUsuario(usuario));
        clienteRepository.flush();

        // Las actividades eliminan en cascada sus asignaciones con instructores.
        actividadRepository.deleteAll(actividadRepository.findByUsuario(usuario));
        actividadRepository.flush();

        instructorRepository.deleteAll(instructorRepository.findByUsuario(usuario));
        instructorRepository.flush();

        List<MetodoDePago> metodos = metodoDePagoRepository.findByUsuario(usuario);
        for (MetodoDePago metodo : metodos) {
            configuracionPagoRepository.findByMetodoDePagoAndActivoTrue(metodo)
                    .ifPresent(configuracionPagoRepository::delete);
            configuracionPagoRepository.findByMetodoDePagoAndActivoFalse(metodo)
                    .ifPresent(configuracionPagoRepository::delete);
        }
        configuracionPagoRepository.flush();
        metodoDePagoRepository.deleteAll(metodos);
        metodoDePagoRepository.flush();

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void crearGimnasio(
            String username,
            String password,
            String nombreGimnasio
    ) {
        String usernameNormalizado = username.trim();

        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya existe."
            );
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usernameNormalizado);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombreGimnasio(nombreGimnasio);
        usuario.setRol("ROLE_USER");

        usuarioRepository.save(usuario);

        configuracionDePagoService.crearMetodoConRecargo(
                "NO_ESPECIFICADO", BigDecimal.ZERO, usuario
        );
        configuracionDePagoService.crearMetodoConRecargo(
                "EFECTIVO", BigDecimal.ZERO, usuario
        );
        configuracionDePagoService.crearMetodoConRecargo(
                "TRANSFERENCIA", BigDecimal.ZERO, usuario
        );
        configuracionDePagoService.crearMetodoConRecargo(
                "TARJETA/CREDITO", BigDecimal.valueOf(15), usuario
        );
    }

}
