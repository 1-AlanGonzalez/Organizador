package com.gymmanager.gym_manager.services.exports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.dto.EntidadRequestDTO;
import com.gymmanager.gym_manager.repository.ClienteRepository;

// Component = “Esta clase es un bean, manejala vos.”
@Component
public class ClienteExportStrategy implements ExportStrategy {
    private final ClienteRepository clienteRepository;

    public ClienteExportStrategy(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public String getNombreEntidad() {
        return "cliente";
    }

    @Override
public List<Map<String, Object>> exportar(EntidadRequestDTO request, LocalDate fecha) {
    
    List<Cliente> clientesBD = clienteRepository.findAll();
    System.out.println("Atributos clientes: " + request.getAtributos());
    System.out.println("Total clientes: " + clientesBD.size());
    List<Map<String, Object>> filas = new ArrayList<>();

    for (Cliente cliente : clientesBD) {
        System.out.println("Procesando clientes: " + cliente.getIdCliente());
        cliente.setEstadoPago(cliente.adeuda() ? "ADEUDA" : "PAGADO");
        List<Map<String, Object>> filasCliente =
                ExportMapper.mapearEntidad(cliente, request.getAtributos());
        System.out.println("Filas generadas: " + filasCliente);
        filas.addAll(filasCliente);
    }
            System.out.println("Total filas cliente: " + filas.size());

    return filas;
}

}
