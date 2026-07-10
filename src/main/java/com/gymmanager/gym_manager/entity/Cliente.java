package com.gymmanager.gym_manager.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CLIENTE")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Integer idCliente;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    @ColumnLabel("Nombre")
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 50)
    @ColumnLabel("Apellido")
    private String apellido;

    @Column(name = "DNI", nullable = false, length = 15)
    @ColumnLabel("DNI")
    private String dni;

    @Column(name = "TELEFONO", nullable = true, length = 20)
    @ColumnLabel("Telefono")
    private String telefono;

    @Column(name = "EMAIL", nullable = true, length = 100)
    @ColumnLabel("Email")
    private String email;

    @Column(name = "OBSERVACIONES", nullable = true, columnDefinition = "TEXT")
    @ColumnLabel("Observaciones")
    private String observaciones;

    // Relación con ActividadCliente
    // orphanRemoval=true significa que si se elimina la inscripcion del cliente, se elimina de la base de datos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActividadCliente> inscripciones = new HashSet<>();

    // AÑADIDO PARA CREAR CADA USUARIO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Transient
    @ColumnLabel("Estado de Pago")
    private String estadoPago;

    public Cliente(String nombre, String apellido, String dni, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
    }

    // Añado un GETTER de inscripciones activas para no tener esta lógica en el frontend
    public Set<ActividadCliente> getInscripcionesActivas() {
        if (inscripciones == null) return Set.of();

        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .collect(Collectors.toSet());
    }
    /* ================== LÓGICA DEL CLIENTE ================== */


    public void agregarInscripcion(ActividadCliente inscripcion){
        inscripciones.add(inscripcion);
        inscripcion.activar();
        inscripcion.setCliente(this);
    }

    public void darseDeBajaAInscripcion(ActividadCliente inscripcion){
        if(!inscripciones.contains(inscripcion)){
            throw new RuntimeException("La inscripcion que quiere darse de baja no esta en sus actividades");
        }

        inscripcion.darseDeBaja(); 
    }

    public boolean adeuda() {
        
        if (inscripciones == null || inscripciones.isEmpty()) {
            return false;
        }
        return inscripciones.stream().anyMatch(i -> i.tieneAdeudaVencida());}


    public boolean tieneVencidos() {
            if (inscripciones == null || inscripciones.isEmpty()) return false;
            return inscripciones.stream()
                    .flatMap(i -> i.getPagos().stream())
                    .anyMatch(p -> p.getEstado() == EstadoPago.VENCIDO);
        }
    // Usé el mismo criterio que se usa en la tabla de Pagos en ingresos... Así la tabla de clientes directamente filtra por estado ADEUDA
    // y me muestra en la tabla los que adeudan y los que no..
    // return inscripciones.stream()
    //     .flatMap(insc -> insc.getPagos().stream())
    //     .anyMatch(p -> p.getEstado() == EstadoPago.ADEUDA);

    

    public BigDecimal totalAdeudado() {
        return inscripciones.stream().map(ActividadCliente::calcularAdeudado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void pagarTodo(){
        inscripciones.stream().forEach(ActividadCliente::pagarTodo);
    }
    
    // Agregado hoy 13/2

    public LocalDate getFechaUltimaAsistencia() {
        // Si no tiene inscripciones no existe.
        if (inscripciones == null || inscripciones.isEmpty()) {
            return null;
        }
        /*
        Filtra solo inscripciones ACTIVAS
        Junta todas las asistencias de todas esas inscripciones
        Extrae solo la fecha
        Busca la más reciente
        Si no hay ninguna → devuelve null
         */
        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .flatMap(i -> i.getAsistencias().stream())
                .map(Asistencia::getFecha)
                .max(LocalDate::compareTo)
                .orElse(null);
    }
}

