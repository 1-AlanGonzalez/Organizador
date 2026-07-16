package com.gymmanager.gym_manager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import com.gymmanager.gym_manager.entity.Actividad;
import com.gymmanager.gym_manager.entity.ActividadCliente;
import com.gymmanager.gym_manager.entity.Asistencia;
import com.gymmanager.gym_manager.entity.Cliente;
import com.gymmanager.gym_manager.entity.ConfiguracionDePago;
import com.gymmanager.gym_manager.entity.Dicta;
import com.gymmanager.gym_manager.entity.EstadoPago;
import com.gymmanager.gym_manager.entity.Instructor;
import com.gymmanager.gym_manager.entity.MetodoDePago;
import com.gymmanager.gym_manager.entity.Pago;
import com.gymmanager.gym_manager.entity.TipoDeCobro;
import com.gymmanager.gym_manager.entity.Usuario;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("loadtest")
class LoadTestDataGenerator {

    private static final String PASSWORD = "loadtest123";
    private static final long RANDOM_SEED = 20260715L;

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager entityManager;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${loadtest.gyms:2}")
    private int cantidadGimnasios;

    @Value("${loadtest.clients-per-gym:1000}")
    private int clientesPorGimnasio;

    @Value("${loadtest.activities-per-gym:10}")
    private int actividadesPorGimnasio;

    @Value("${loadtest.instructors-per-gym:15}")
    private int instructoresPorGimnasio;

    @Value("${loadtest.history-months:6}")
    private int mesesDeHistorial;

    @Test
    void generarDatosParaPruebaDeCarga() throws Exception {
        validarDestinoSeguro();
        validarCantidades();
        limpiarBaseDeCarga();

        String passwordCodificada = passwordEncoder.encode(PASSWORD);
        for (int numeroGym = 1; numeroGym <= cantidadGimnasios; numeroGym++) {
            int gymActual = numeroGym;
            transactionTemplate.executeWithoutResult(status ->
                    generarGimnasio(gymActual, passwordCodificada));
        }

        Long clientesGenerados = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM CLIENTE", Long.class);
        long esperados = (long) cantidadGimnasios * clientesPorGimnasio;
        assertTrue(clientesGenerados != null && clientesGenerados == esperados,
                "La cantidad de clientes generada no coincide con la esperada.");

        System.out.printf(
                "Carga lista: %d gimnasios, %d clientes. Usuarios gym_load_1..%d, contraseña %s.%n",
                cantidadGimnasios, clientesGenerados, cantidadGimnasios, PASSWORD);
    }

    private void validarDestinoSeguro() throws Exception {
        String url = dataSource.getConnection().getMetaData().getURL();
        String urlNormalizada = url.toLowerCase(Locale.ROOT);
        if (!urlNormalizada.contains("gymorganizationloadtest")) {
            throw new IllegalStateException(
                    "Generación cancelada: LOADTEST_DB_URL debe apuntar a GymOrganizationLoadTest. URL: " + url);
        }
    }

    private void validarCantidades() {
        if (cantidadGimnasios < 1 || clientesPorGimnasio < 1
                || actividadesPorGimnasio < 3 || instructoresPorGimnasio < 1
                || mesesDeHistorial < 1) {
            throw new IllegalArgumentException("Las cantidades de loadtest deben ser positivas y debe haber al menos 3 actividades.");
        }
    }

    private void limpiarBaseDeCarga() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            for (String tabla : List.of(
                    "ASISTENCIA", "PAGO", "INSTRUCTOR_ACTIVIDAD", "ACTIVIDAD_CLIENTE",
                    "CLIENTE", "INSTRUCTOR", "ACTIVIDAD", "CONFIGURACIONPAGO",
                    "METODO_DE_PAGO", "usuarios")) {
                jdbcTemplate.execute("TRUNCATE TABLE " + tabla);
            }
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void generarGimnasio(int numeroGym, String passwordCodificada) {
        Random random = new Random(RANDOM_SEED + numeroGym);
        Usuario usuario = new Usuario();
        usuario.setUsername("gym_load_" + numeroGym);
        usuario.setPassword(passwordCodificada);
        usuario.setNombreGimnasio("Gimnasio de carga " + numeroGym);
        usuario.setDireccion("Calle de prueba " + numeroGym);
        usuario.setTelefono("11000000" + numeroGym);
        usuario.setRol("ROLE_USER");
        entityManager.persist(usuario);

        MetodoDePago efectivo = crearMetodo("EFECTIVO", usuario, BigDecimal.ZERO);
        crearMetodo("TRANSFERENCIA", usuario, BigDecimal.ZERO);
        MetodoDePago sinEspecificar = crearMetodo("NO_ESPECIFICADO", usuario, BigDecimal.ZERO);

        List<Actividad> actividades = new ArrayList<>();
        for (int i = 1; i <= actividadesPorGimnasio; i++) {
            Actividad actividad = new Actividad(
                    "Actividad " + i,
                    100_000,
                    BigDecimal.valueOf(15_000L + i * 1_000L),
                    BigDecimal.valueOf(2_000L + i * 100L));
            actividad.setUsuario(usuario);
            entityManager.persist(actividad);
            actividades.add(actividad);
        }

        List<Instructor> instructores = new ArrayList<>();
        for (int i = 1; i <= instructoresPorGimnasio; i++) {
            Instructor instructor = new Instructor(
                    "Instructor" + i,
                    "Carga" + numeroGym,
                    numeroGym + String.format("%07d", i),
                    "11555" + String.format("%05d", i));
            instructor.setUsuario(usuario);
            entityManager.persist(instructor);
            instructores.add(instructor);
        }

        for (int i = 0; i < actividades.size(); i++) {
            Instructor instructor = instructores.get(i % instructores.size());
            entityManager.persist(new Dicta(
                    actividades.get(i), instructor, "Lun - Mié", (8 + i) + ":00"));
        }

        LocalDate hoy = LocalDate.now();
        for (int numeroCliente = 1; numeroCliente <= clientesPorGimnasio; numeroCliente++) {
            Cliente cliente = crearCliente(numeroGym, numeroCliente, usuario);
            entityManager.persist(cliente);

            List<Actividad> mezcladas = new ArrayList<>(actividades);
            Collections.shuffle(mezcladas, random);
            int cantidadInscripciones = 1 + random.nextInt(Math.min(3, mezcladas.size()));

            for (int i = 0; i < cantidadInscripciones; i++) {
                Actividad actividad = mezcladas.get(i);
                LocalDate fechaInscripcion = hoy.minusMonths(random.nextInt(12)).minusDays(random.nextInt(28));
                ActividadCliente inscripcion = new ActividadCliente(
                        fechaInscripcion, actividad.getPrecio(), cliente, actividad, TipoDeCobro.MENSUAL);
                entityManager.persist(inscripcion);

                generarPagos(inscripcion, efectivo, sinEspecificar, hoy, random);
                generarAsistencias(inscripcion, hoy, random);
            }

            if (numeroCliente % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
    }

    private MetodoDePago crearMetodo(String nombre, Usuario usuario, BigDecimal recargo) {
        MetodoDePago metodo = new MetodoDePago(nombre);
        metodo.setUsuario(usuario);
        entityManager.persist(metodo);
        entityManager.persist(new ConfiguracionDePago(metodo, recargo, true));
        return metodo;
    }

    private Cliente crearCliente(int numeroGym, int numeroCliente, Usuario usuario) {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente" + numeroCliente);
        cliente.setApellido("Carga" + numeroGym);
        cliente.setDni(numeroGym + String.format("%07d", numeroCliente));
        cliente.setTelefono("116" + String.format("%07d", numeroCliente));
        cliente.setEmail("cliente" + numeroCliente + ".gym" + numeroGym + "@loadtest.local");
        cliente.setUsuario(usuario);
        return cliente;
    }

    private void generarPagos(ActividadCliente inscripcion, MetodoDePago efectivo,
                              MetodoDePago sinEspecificar, LocalDate hoy, Random random) {
        for (int mes = mesesDeHistorial; mes >= 1; mes--) {
            LocalDate fecha = hoy.minusMonths(mes);
            Pago pago = new Pago(inscripcion.getCosto(), fecha, fecha.plusMonths(1), inscripcion, efectivo);
            pago.setEstado(EstadoPago.PAGADO);
            entityManager.persist(pago);
        }

        Pago pagoActual = new Pago(inscripcion.getCosto(), hoy.minusDays(random.nextInt(20)),
                hoy.plusDays(10), inscripcion, random.nextBoolean() ? efectivo : sinEspecificar);
        pagoActual.setEstado(random.nextInt(4) == 0 ? EstadoPago.ADEUDA : EstadoPago.PAGADO);
        entityManager.persist(pagoActual);
    }

    private void generarAsistencias(ActividadCliente inscripcion, LocalDate hoy, Random random) {
        for (int dia = 1; dia <= 12; dia++) {
            LocalDate fecha = hoy.minusDays(dia * 3L);
            entityManager.persist(new Asistencia(fecha, random.nextInt(10) < 8, inscripcion));
        }
    }
}
