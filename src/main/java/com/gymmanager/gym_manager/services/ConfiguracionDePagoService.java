package com.gymmanager.gym_manager.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gymmanager.gym_manager.entity.dto.MetodoPagoConfigDTO;
import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Usuario;
import com.gymmanager.gym_manager.repository.ConfiguracionPagoRepository;
import com.gymmanager.gym_manager.repository.MetodoDePagoRepository;
import com.gymmanager.gym_manager.repository.PagoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ConfiguracionDePagoService {

    private final ConfiguracionPagoRepository configuracionDePagoRepository;
    private final MetodoDePagoRepository  metodoDePagoRepository;
    private final PagoRepository          pagoRepository;

    public ConfiguracionDePagoService(ConfiguracionPagoRepository configuracionDePagoRepository,
                                      MetodoDePagoRepository  metodoDePagoRepository,
                                      PagoRepository          pagoRepository) {
        this.configuracionDePagoRepository = configuracionDePagoRepository;
        this. metodoDePagoRepository =  metodoDePagoRepository;
        this.pagoRepository = pagoRepository;
    }

    // ── Listar todos los métodos activos para mostrar en la vista ─────────────

    public List<MetodoPagoConfigDTO> listarActivos(Usuario usuario) {
        return configuracionDePagoRepository
                .findByActivoTrueAndMetodoDePago_Usuario(usuario) 
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Listar inactivos ──────────────────────────────────────────────────────


    public List<MetodoPagoConfigDTO> listarInactivos(Usuario usuario) {
        return configuracionDePagoRepository
                .findByActivoFalseAndMetodoDePago_Usuario(usuario)  // ← query a la DB
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Actualizar recargo de una ConfiguracionDePago existente ───────────────

       public void actualizarRecargo(Integer idConfiguracion, BigDecimal nuevoRecargo) {
        ConfiguracionDePago config = configuracionDePagoRepository.findById(idConfiguracion)
                .orElseThrow(() -> new RuntimeException(
                        "Configuración no encontrada: " + idConfiguracion));
        config.cambiarOAgregarRecargo(nuevoRecargo != null ? nuevoRecargo : BigDecimal.ZERO);
        configuracionDePagoRepository.save(config);
    }

    // ── Crear nuevo MetodoDePago + su ConfiguracionDePago activa ─────────────


        public void crearMetodoConRecargo(String nombre, BigDecimal recargo, Usuario usuario) {
                if (nombre == null || nombre.isBlank())
                throw new IllegalArgumentException("El nombre no puede estar vacío.");

                // Verifica duplicado solo dentro del mismo usuario — query a la DB
                boolean yaExiste = configuracionDePagoRepository
                        .findByActivoTrueAndMetodoDePago_Usuario(usuario)
                        .stream()
                        .anyMatch(c -> c.getMetodoDePago().getNombre().equalsIgnoreCase(nombre.trim()));

                if (yaExiste)
                throw new IllegalArgumentException("Ya existe un método con ese nombre: " + nombre.trim());

                MetodoDePago metodo = new MetodoDePago(nombre.trim());
                metodo.setUsuario(usuario);
                metodoDePagoRepository.save(metodo);

                configuracionDePagoRepository.save(new ConfiguracionDePago(
                        metodo,
                        recargo != null ? recargo : BigDecimal.ZERO,
                        Boolean.TRUE));
        }

    // ── Soft-delete: desactiva la ConfiguracionDePago activa del método ──────
        public void desactivarMetodo(Integer idMetodo) {
        MetodoDePago metodo = metodoDePagoRepository.findById(idMetodo)
                .orElseThrow(() -> new RuntimeException("Método no encontrado: " + idMetodo));

        ConfiguracionDePago config = configuracionDePagoRepository
                .findByMetodoDePagoAndActivoTrue(metodo)
                .orElseThrow(() -> new RuntimeException("El método no tiene configuración activa."));

        config.darDeBajaAlMetodo();
        configuracionDePagoRepository.save(config);
    }

    // ── Reactivar método ──────────────────────────────────────────────────────

    public void reactivarMetodo(Integer idMetodo) {
        MetodoDePago metodo = metodoDePagoRepository.findById(idMetodo)
                .orElseThrow(() -> new IllegalArgumentException("Método no encontrado: " + idMetodo));

        ConfiguracionDePago config = configuracionDePagoRepository
                .findByMetodoDePagoAndActivoFalse(metodo)
                .orElseThrow(() -> new IllegalArgumentException("El método no tiene configuración inactiva."));

        config.activarMetodo();
        configuracionDePagoRepository.save(config);
    }

     // ── Eliminar permanente ───────────────────────────────────────────────────
      public void eliminarMetodoPermanente(Integer idMetodo) {
        MetodoDePago metodo = metodoDePagoRepository.findById(idMetodo)
                .orElseThrow(() -> new IllegalArgumentException("Método no encontrado: " + idMetodo));

        if (configuracionDePagoRepository.existsByMetodoDePagoAndActivoTrue(metodo))
            throw new IllegalStateException("Desactivá el método antes de eliminarlo permanentemente.");

        pagoRepository.deleteByMetodoPago(metodo);

        configuracionDePagoRepository.findByMetodoDePagoAndActivoFalse(metodo)
                .ifPresent(configuracionDePagoRepository::delete);

        metodoDePagoRepository.delete(metodo);
    }

        // ── Helper ────────────────────────────────────────────────────────────────
    private MetodoPagoConfigDTO toDTO(ConfiguracionDePago c) {
        return new MetodoPagoConfigDTO(
                c.getMetodoDePago().getIdMetodoDePago(),
                c.getId(),
                c.getMetodoDePago().getNombre(),
                c.getPorcentajeRecargo());
    }
}