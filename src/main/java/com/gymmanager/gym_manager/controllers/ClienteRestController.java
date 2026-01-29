package com.gymmanager.gym_manager.controllers;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.repository.ClienteRepository;

// El fin del ClienteRestController es exponer endpoints relacionados con la entidad Cliente.
// En este caso, se añade un endpoint para obtener la deuda total de un cliente específico.
public class ClienteRestController {
private final ClienteRepository clienteRepository;
    // Constructor con clienteRepository 
    public ClienteRestController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
    // Obtenemos la ID del cliente desde la URL y buscamos el cliente en la base de datos.
    // Luego, usamos el método totalAdeudado() de la entidad Cliente para calcular la deuda total.
    @GetMapping("/{id}/deuda")
    public ResponseEntity<BigDecimal> obtenerDeuda(@PathVariable Integer id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        // Usamos el método que ya tienes en tu entidad Cliente
        return ResponseEntity.ok(cliente.totalAdeudado());
    }
}
