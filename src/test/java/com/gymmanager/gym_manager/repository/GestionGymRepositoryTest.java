package com.gymmanager.gym_manager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.entity.Usuario;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class GestionGymRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void creaClienteActividadEInstructorParaElMismoGimnasio() {
        Usuario gimnasio = crearUsuario("gym-entidades");

        Cliente cliente = new Cliente("Ana", "Pérez", "30111222", "1122334455");
        cliente.setUsuario(gimnasio);
        entityManager.persist(cliente);

        Actividad actividad = new Actividad(
                "Funcional", 20, new BigDecimal("18000"), new BigDecimal("2500"));
        actividad.setUsuario(gimnasio);
        entityManager.persist(actividad);

        Instructor instructor = new Instructor("Laura", "Gómez", "28999888", "1199887766");
        instructor.setUsuario(gimnasio);
        entityManager.persist(instructor);
        entityManager.flush();

        assertThat(cliente.getIdCliente()).isNotNull();
        assertThat(actividad.getIdActividad()).isNotNull();
        assertThat(instructor.getIdInstructor()).isNotNull();
        assertThat(clienteRepository.findByUsuario(gimnasio)).containsExactly(cliente);
    }

    @Test
    void cartelDeSieteDiasIncluyeSoloClientesConPagoPendienteDentroDelRango() {
        Usuario gimnasio = crearUsuario("gym-vencimientos");
        LocalDate hoy = LocalDate.now();

        Cliente venceHoy = crearClienteConPago(gimnasio, "Hoy", "40000001",
                hoy, EstadoPago.ADEUDA);
        Cliente venceEnSiete = crearClienteConPago(gimnasio, "En siete", "40000002",
                hoy.plusDays(7), EstadoPago.ADEUDA);
        crearClienteConPago(gimnasio, "En ocho", "40000003",
                hoy.plusDays(8), EstadoPago.ADEUDA);
        crearClienteConPago(gimnasio, "Ya pagó", "40000004",
                hoy.plusDays(3), EstadoPago.PAGADO);
        entityManager.flush();
        entityManager.clear();

        List<Cliente> resultado = clienteRepository.findClientesConVencimientoEntre(
                gimnasio, hoy, hoy.plusDays(7));

        assertThat(resultado)
                .extracting(Cliente::getDni)
                .containsExactlyInAnyOrder(venceHoy.getDni(), venceEnSiete.getDni());
    }

    private Usuario crearUsuario(String username) {
        Usuario usuario = new Usuario(username, "password-de-prueba");
        usuario.setNombreGimnasio("Gimnasio de prueba");
        entityManager.persist(usuario);
        return usuario;
    }

    private Cliente crearClienteConPago(Usuario usuario, String nombre, String dni,
                                        LocalDate vencimiento, EstadoPago estado) {
        Cliente cliente = new Cliente(nombre, "Prueba", dni, "1100000000");
        cliente.setUsuario(usuario);
        entityManager.persist(cliente);

        Actividad actividad = new Actividad(
                "Actividad " + dni, 30, new BigDecimal("15000"), new BigDecimal("2000"));
        actividad.setUsuario(usuario);
        entityManager.persist(actividad);

        ActividadCliente inscripcion = new ActividadCliente(
                LocalDate.now(), actividad.getPrecio(), cliente, actividad, TipoDeCobro.MENSUAL);
        cliente.agregarInscripcion(inscripcion);
        entityManager.persist(inscripcion);

        Pago pago = new Pago(actividad.getPrecio(), LocalDate.now(), vencimiento, inscripcion, null);
        pago.setEstado(estado);
        inscripcion.getPagos().add(pago);
        entityManager.persist(pago);
        return cliente;
    }
}
