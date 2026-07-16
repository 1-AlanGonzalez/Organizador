package com.gymmanager.gym_manager.initializers;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.UsuarioRepository;


// el CommandLineRunner:
//Es una interfaz de Spring Boot.
//Sirve para ejecutar código automáticamente cuando la aplicación arranca
//Se ejecuta una sola vez por arranque

//Spring Boot hace esto:
// Crea el contexto de Spring
// Inyecta los repositories, services, controllers
// Busca clases que implementen CommandLineRunner
// Ejecuta el método run()
// Recién ahí levanta la web (localhost:8080)

//El component:
//Esta clase es parte del sistema, creala automáticamente

//Esta clase la usamos para crear configuraciones iniciales (como los recargos de pago) 
//solo si no existen, así el sistema siempre arranca con reglas válidas.”

@Component
public class ConfiguracionPagoInitializer implements  CommandLineRunner {
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final ConfiguracionPagoRepository configuracionPagoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConfiguracionPagoInitializer(MetodoDePagoRepository metodoDePagoRepository,
                                            ConfiguracionPagoRepository configuracionPagoRepository,
                                            UsuarioRepository usuarioRepository) {
    this.metodoDePagoRepository = metodoDePagoRepository;
    this.configuracionPagoRepository = configuracionPagoRepository;
    this.usuarioRepository = usuarioRepository;
    }

    //Este método se ejecuta SOLO al iniciar la aplicación
    @Override
    public void run(String... args) {
        Usuario admin = usuarioRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            System.out.println("[ConfiguracionPagoInitializer] Usuario admin no encontrado, saltando inicialización.");
            return;
        }

        crearSiNoExiste("NO_ESPECIFICADO", BigDecimal.ZERO, admin);
        crearSiNoExiste("EFECTIVO", BigDecimal.ZERO, admin);
        crearSiNoExiste("TRANSFERENCIA", BigDecimal.ZERO, admin);
        crearSiNoExiste("TARJETA/CREDITO", BigDecimal.valueOf(15), admin);


    }

    private void crearSiNoExiste(String nombre, BigDecimal porcentaje, Usuario usuario) {
        // Buscamos por nombre Y usuario para no mezclar datos entre usuarios
        MetodoDePago metodo = metodoDePagoRepository
                .findByNombreAndUsuario(nombre, usuario)
                .orElseGet(() -> {
                    MetodoDePago nuevo = new MetodoDePago(nombre);
                    nuevo.setUsuario(usuario);  
                    return metodoDePagoRepository.save(nuevo);
                });

    boolean existeConfig = configuracionPagoRepository.existsByMetodoDePago(metodo);
    if (!existeConfig) {
        ConfiguracionDePago config = new ConfiguracionDePago();
        config.setMetodoDePago(metodo);
        config.cambiarOAgregarRecargo(porcentaje);
        config.activarMetodo();

        configuracionPagoRepository.save(config);
        System.out.println("[Init] Método creado: " + nombre + " para usuario: " + usuario.getUsername());

    }
}
}

