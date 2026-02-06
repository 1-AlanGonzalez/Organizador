package com.gymmanager.gym_manager.entity;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
    name = "ACTIVIDAD_CLIENTE",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ID_CLIENTE", "ID_ACTIVIDAD"})
    }
)
public class ActividadCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD_CLIENTE")
    private Integer idActividadCliente;
    @Column(name = "FECHA_DE_INSCRIPCION", nullable = false)
    private LocalDate fechaDeInscripcion;
    
    @Column(name = "COSTO", nullable = false)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoInscripcion estado = EstadoInscripcion.ACTIVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_DE_COBRO", nullable = false)
    private TipoDeCobro tipoDecobro;
    
    @ManyToOne
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD")
    private Actividad actividad;

    @OneToMany(mappedBy = "actividadCliente", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    @OneToMany(mappedBy = "actividadCliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Asistencia> asistencias = new HashSet<>();

    public ActividadCliente() { }

    public ActividadCliente(LocalDate fechaDeInscripcion, BigDecimal costo, Cliente cliente, 
        Actividad actividad, TipoDeCobro tipoDeCobro) {
        this.fechaDeInscripcion = fechaDeInscripcion;
        this.costo = costo;
        this.cliente = cliente;
        this.actividad = actividad;
        this.tipoDecobro = tipoDeCobro;
    }

    /* ================== Getters y Setters ================== */


public void setEstado(EstadoInscripcion estado) {
    this.estado = estado;
}

public Set<Pago> getPagos() {
    return pagos;
}

public Set<Asistencia> getAsistencias() {
    return asistencias;
}
    public Integer getIdActividadCliente() {
        return idActividadCliente;
    }

    public void setIdActividadCliente(Integer idActividadCliente) {
        this.idActividadCliente = idActividadCliente;
    }

    public LocalDate getFechaDeInscripcion() {
        return fechaDeInscripcion;
    }

    public void setFechaDeInscripcion(LocalDate fechaDeInscripcion) {
        this.fechaDeInscripcion = fechaDeInscripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public EstadoInscripcion getEstado(){
        return estado;
    }

    public TipoDeCobro getTipoDeCobro(){
        return tipoDecobro;
    }

    public void setTipoDeCobro(TipoDeCobro tipoDeCobroNuevo){
        this.tipoDecobro = tipoDeCobroNuevo;
    }

    /* ================== LÓGICA DE ACTIVIDADCLIENTE ================== */

    

    public void activar(){
        estado = EstadoInscripcion.ACTIVA;
    }

    public void darseDeBaja(){
        if(this.estado == EstadoInscripcion.BAJA){
        throw new RuntimeException("La inscripción ya está dada de baja");
        }
        this.estado = EstadoInscripcion.BAJA;
    }

    /* El nucleo central de todo va a ser esta entidad */
    /* Aca vamos a guardar las asistencias, los pagos y demas */
    /* Es el centro por que ya conoce cliente y actividad la logica es la siguiente: */
    /*Los pagos no son del cliente ,las asistencias no son de la actividad, son de esa inscripción puntual */
    /* Ya que si juan se inscribe a Boxeo, entonces es juan asiste a su inscripcion de boxeo tal, donde el historial de pago es tal... */


    /* Sistema de tomar asistencia simple */
    /* Cosas a agregar a futuro :  */
    /* asistencias por mes,porcentaje de asistencia, faltas consecutivas (a charlar para saber si lo agregamos) */

    public void tomarAsistencia(LocalDate fecha, Boolean presente) {

        if (estado == EstadoInscripcion.BAJA) {
            throw new RuntimeException("No se puede tomar asistencia a una inscripción dada de baja");
        }

        boolean yaExiste = asistencias.stream()
                .anyMatch(a -> a.getFecha().equals(fecha));

        if (yaExiste) {
            throw new RuntimeException("La asistencia ya fue registrada para este día");
        }

        Asistencia asistencia = new Asistencia(fecha,presente,this);

        this.asistencias.add(asistencia);
    }
    /* Como juan se inscribio a boxeo en el mes, Esta inscripcion primero q se genera el pago y luego tambien guarda como dijimos historial de pagos */

    public BigDecimal calcularMontoMensual(){
        return actividad.getPrecio();
    }

    public BigDecimal calcularMontoDiario(){
        return actividad.getPrecioDiario();
    }

    public Pago generarPagoMensual(LocalDate fechaBase){
        if (estado == EstadoInscripcion.BAJA) {
            throw new RuntimeException("No se pueden generar pagos para una inscripción dada de baja");
        }

        Boolean existePagoActivo = pagos.stream().anyMatch(p->p.getEstado() == EstadoPago.ADEUDA && !p.estaVencido());
        if(existePagoActivo){
            throw new RuntimeException("Ya existe un pago activo para esta inscripcion");
        }

        BigDecimal monto = calcularMontoMensual();
        Pago pago = new Pago(monto,fechaBase,fechaBase.plusMonths(1), this);
        pagos.add(pago);
        return pago;
    }

    public Pago generarPagoDiario() {
        if (estado == EstadoInscripcion.BAJA) {
            throw new RuntimeException("No se pueden generar pagos para una inscripción dada de baja");
        }

        Boolean existePagoActivo = pagos.stream().anyMatch(p->p.getEstado() == EstadoPago.ADEUDA && !p.estaVencido());
        if(existePagoActivo){
            throw new RuntimeException("Ya existe un pago activo para esta inscripcion");
        }

        BigDecimal monto = calcularMontoDiario();
        Pago pago = new Pago(monto,LocalDate.now(), LocalDate.now(),this);
        pagos.add(pago);
        return pago;
    }

    public Pago generarPago(LocalDate fechaBase) {

        if (estado == EstadoInscripcion.BAJA) {
            throw new RuntimeException("No se pueden generar pagos para una inscripción dada de baja");
        }

        boolean existePagoActivo = pagos.stream()
        .anyMatch(p -> p.getEstado() == EstadoPago.ADEUDA && !p.estaVencido());

        if (existePagoActivo) {
        throw new RuntimeException("Ya existe un pago activo para esta inscripción");
        }

        Pago pago;
    
        if (tipoDecobro == TipoDeCobro.MENSUAL) {
        pago = generarPagoMensual(fechaBase);
        } else {
        pago = generarPagoDiario();
        }

        pagos.add(pago);
        return pago;
    }


    public Boolean tieneAdeudaVencida(){
        if (pagos == null || pagos.isEmpty()) {
            return false;
        }
        return pagos.stream().anyMatch(Pago::estaVencido);
    }


    public BigDecimal calcularAdeudado() {
        return pagos.stream().filter(p->p.estaVencido()).map(Pago::getMontoAPagar).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void pagarTodo(){
        pagos.stream().filter(p->p.getEstado() == EstadoPago.ADEUDA).forEach(p->p.pagar());
    }

    public void pagarElMes(Pago pago){
        if(!pagos.contains(pago)){
            throw new RuntimeException("El pago no pertenece a esta inscripción");
        }
        pago.pagar();
    }
    // Obtiene la fecha del último pago realizado
    public LocalDate getFechaUltimoPago() {
    return pagos.stream()
                .max(Comparator.comparing(Pago::getFechaGeneracion))
                .map(Pago::getFechaGeneracion)
                .orElse(null); // si no tiene pagos devuelve null
}
}
