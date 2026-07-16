package com.gymmanager.gym_manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import com.gymmanager.gym_manager.entity.*;
import jakarta.persistence.EntityManager;

/** Escenario determinista, exclusivamente ficticio, para capturas y videos. */
@SpringBootTest
@ActiveProfiles("demo")
class DemoDataGenerator {
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "Demo2026!";
    private static final LocalDate TODAY = LocalDate.now();
    private static final String[][] CLIENTS = {
        {"Sofía", "Martínez", "41958263", "11 6123-4587", "sofia.martinez@example.com"},
        {"Mateo", "González", "38745129", "11 5034-9216", "mateo.gonzalez@example.com"},
        {"Valentina", "Rossi", "43216785", "11 6845-1372", "valentina.rossi@example.com"},
        {"Julián", "Fernández", "36590241", "11 4728-6503", "julian.fernandez@example.com"},
        {"Camila", "López", "40873156", "11 5931-8042", "camila.lopez@example.com"},
        {"Tomás", "Romero", "39428617", "11 6574-2198", "tomas.romero@example.com"},
        {"Martina", "Álvarez", "44710293", "11 4386-7154", "martina.alvarez@example.com"},
        {"Nicolás", "Pereyra", "37284190", "11 5267-3481", "nicolas.pereyra@example.com"},
        {"Lucía", "Benítez", "42569318", "11 6912-5073", "lucia.benitez@example.com"},
        {"Franco", "Acosta", "40175862", "11 4853-9621", "franco.acosta@example.com"},
        {"Emilia", "Navarro", "43820651", "11 6371-2845", "emilia.navarro@example.com"},
        {"Agustín", "Herrera", "35971428", "11 5149-7362", "agustin.herrera@example.com"},
        {"Delfina", "Suárez", "41638027", "11 6782-4519", "delfina.suarez@example.com"},
        {"Lautaro", "Medina", "38920574", "11 4637-8205", "lautaro.medina@example.com"},
        {"Micaela", "Castro", "42957136", "11 6028-1743", "micaela.castro@example.com"},
        {"Santiago", "Vega", "37416809", "11 5493-6817", "santiago.vega@example.com"},
        {"Renata", "Molina", "45203718", "11 6251-9034", "renata.molina@example.com"},
        {"Facundo", "Silva", "39681452", "11 4876-2359", "facundo.silva@example.com"},
        {"Josefina", "Torres", "43152680", "11 6530-7481", "josefina.torres@example.com"},
        {"Bruno", "Domínguez", "36840921", "11 5204-3968", "bruno.dominguez@example.com"},
        {"Malena", "Ibarra", "44581703", "11 6718-5420", "malena.ibarra@example.com"},
        {"Lucas", "Aguirre", "38205649", "11 4592-8176", "lucas.aguirre@example.com"},
        {"Pilar", "Sosa", "42093815", "11 6147-2803", "pilar.sosa@example.com"},
        {"Benjamín", "Cabrera", "40716293", "11 5380-6941", "benjamin.cabrera@example.com"}
    };

    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired PasswordEncoder encoder;

    @Test
    void generarEscenarioDePresentacion() throws Exception {
        validateSafeDestination();
        clearDemoDatabase();
        tx.executeWithoutResult(status -> generate());
        assertEquals((long) CLIENTS.length, jdbc.queryForObject("SELECT COUNT(*) FROM CLIENTE", Long.class));
        System.out.printf("Demo lista. Usuario: %s | Contraseña: %s | %d clientes ficticios.%n", USERNAME, PASSWORD, CLIENTS.length);
    }

    private void validateSafeDestination() throws Exception {
        String url = dataSource.getConnection().getMetaData().getURL().toLowerCase(Locale.ROOT);
        if (!url.contains("gymorganizationdemo"))
            throw new IllegalStateException("DEMO_DB_URL debe apuntar a GymOrganizationDemo. URL: " + url);
    }

    private void clearDemoDatabase() {
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            for (String table : List.of("ASISTENCIA", "PAGO", "INSTRUCTOR_ACTIVIDAD", "ACTIVIDAD_CLIENTE",
                    "CLIENTE", "INSTRUCTOR", "ACTIVIDAD", "CONFIGURACIONPAGO", "METODO_DE_PAGO", "usuarios"))
                jdbc.execute("TRUNCATE TABLE " + table);
        } finally { jdbc.execute("SET FOREIGN_KEY_CHECKS = 1"); }
    }

    private void generate() {
        Usuario owner = new Usuario();
        owner.setUsername(USERNAME); owner.setPassword(encoder.encode(PASSWORD));
        owner.setNombreGimnasio("Impulso Fitness Club");
        owner.setDireccion("Av. Cabildo 2450, Belgrano"); owner.setTelefono("11 4788-2040");
        owner.setRol("ROLE_USER"); em.persist(owner);

        MetodoDePago cash = paymentMethod("EFECTIVO", owner, "0");
        MetodoDePago transfer = paymentMethod("TRANSFERENCIA", owner, "0");
        MetodoDePago debit = paymentMethod("DÉBITO", owner, "3.5");
        MetodoDePago unspecified = paymentMethod("NO_ESPECIFICADO", owner, "0");
        List<Actividad> activities = List.of(
            activity("Musculación", 80, "28000", "4500", owner), activity("Cross Training", 24, "35000", "6000", owner),
            activity("Funcional", 20, "32000", "5500", owner), activity("Yoga", 18, "30000", "5000", owner),
            activity("Spinning", 16, "33000", "5500", owner), activity("Boxeo", 20, "36000", "6500", owner));
        Instructor[] instructors = {
            instructor("Carolina", "Méndez", "32918457", "11 6021-3487", owner),
            instructor("Diego", "Ramírez", "30157284", "11 4875-9204", owner),
            instructor("Marina", "Quiroga", "34726019", "11 6358-1742", owner),
            instructor("Leandro", "Núñez", "31584962", "11 5194-6830", owner),
            instructor("Paula", "Giménez", "33847105", "11 6720-4518", owner)};
        String[][] schedules = {{"Lun - Mié - Vie", "07:00"}, {"Mar - Jue", "19:00"}, {"Lun - Mié", "18:00"},
                {"Mar - Jue", "09:00"}, {"Sáb", "10:00"}, {"Lun - Vie", "20:00"}};
        for (int i = 0; i < activities.size(); i++)
            em.persist(new Dicta(activities.get(i), instructors[i % instructors.length], schedules[i][0], schedules[i][1]));

        Random random = new Random(20260716L);
        for (int i = 0; i < CLIENTS.length; i++) {
            String[] data = CLIENTS[i];
            Cliente client = new Cliente(data[0], data[1], data[2], data[3]);
            client.setEmail(data[4]); client.setUsuario(owner);
            client.setObservaciones(i == 4 ? "Prefiere entrenar por la mañana." : i == 14 ? "Apto médico presentado." : null);
            em.persist(client);
            int enrollmentCount = i % 5 == 0 ? 2 : 1;
            for (int e = 0; e < enrollmentCount; e++) {
                Actividad activity = activities.get((i + e * 2) % activities.size());
                ActividadCliente enrollment = new ActividadCliente(TODAY.minusMonths(2 + random.nextInt(9)), activity.getPrecio(), client, activity, TipoDeCobro.MENSUAL);
                if (i == 21) enrollment.darseDeBaja();
                em.persist(enrollment);
                for (int month = 5; month >= 1; month--) {
                    LocalDate generated = TODAY.minusMonths(month).withDayOfMonth(5 + random.nextInt(4));
                    MetodoDePago method = List.of(cash, transfer, debit).get(random.nextInt(3));
                    Pago paid = new Pago(enrollment.getCosto(), generated, generated.plusMonths(1), enrollment, method);
                    paid.setEstado(EstadoPago.PAGADO); paid.setObservaciones(method == transfer ? "Transferencia acreditada" : null); em.persist(paid);
                }
                Pago current = new Pago(enrollment.getCosto(), TODAY.withDayOfMonth(5), TODAY.plusMonths(1).withDayOfMonth(5), enrollment,
                        i % 6 == 0 ? unspecified : List.of(cash, transfer, debit).get(i % 3));
                current.setEstado(i % 6 == 0 ? EstadoPago.ADEUDA : EstadoPago.PAGADO); em.persist(current);
                if (i != 21) for (int day = 1; day <= 28; day += 3)
                    em.persist(new Asistencia(TODAY.minusDays(day), random.nextInt(10) > 1, enrollment));
            }
        }
        em.flush();
    }

    private MetodoDePago paymentMethod(String name, Usuario owner, String surcharge) {
        MetodoDePago value = new MetodoDePago(name); value.setUsuario(owner); em.persist(value);
        em.persist(new ConfiguracionDePago(value, new BigDecimal(surcharge), true)); return value;
    }
    private Actividad activity(String name, int capacity, String monthly, String daily, Usuario owner) {
        Actividad value = new Actividad(name, capacity, new BigDecimal(monthly), new BigDecimal(daily));
        value.setUsuario(owner); em.persist(value); return value;
    }
    private Instructor instructor(String name, String surname, String dni, String phone, Usuario owner) {
        Instructor value = new Instructor(name, surname, dni, phone); value.setUsuario(owner); em.persist(value); return value;
    }
}
