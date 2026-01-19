package com.gymmanager.gym_manager.entity;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "ACTIVIDAD_CLIENTE")
public class ActividadCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD_CLIENTE")
    private Integer idActividadCliente;
        
    @Column(name = "FECHA_DE_INSCRIPCION", nullable = false)
    private LocalDate fechaDeInscripcion;
    
    @Column(name = "COSTO", nullable = false)
    private BigDecimal costo;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "ID_ACTIVIDAD")
    private Actividad actividad;

    @OneToMany(mappedBy = "actividadCliente", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    @Transient
    private Set<Asistencia> asistencias = new HashSet<>();

    public ActividadCliente() { }

    public ActividadCliente(LocalDate fechaDeInscripcion, BigDecimal costo, Cliente cliente, Actividad actividad) {
        this.fechaDeInscripcion = fechaDeInscripcion;
        this.costo = costo;
        this.cliente = cliente;
        this.actividad = actividad;
    }

    /* ================== Getters y Setters ================== */

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

    /* ================== LÓGICA DE ACTIVIDADCLIENTE ================== */

    /* El nucleo central de todo va a ser esta entidad */
    /* Aca vamos a guardar las asistencias, los pagos y demas */
    /* Es el centro por que ya conoce cliente y actividad la logica es la siguiente: */
    /*Los pagos no son del cliente ,las asistencias no son de la actividad, son de esa inscripción puntual */
    /* Ya que si juan se inscribe a Boxeo, entonces es juan asiste a su inscripcion de boxeo tal, donde el historial de pago es tal... */

    public void tomarAsistencia(LocalDate fecha, Boolean presente){
        Asistencia asistencia = new Asistencia(fecha,presente,cliente,actividad);
        asistencias.add(asistencia);
    }

    /* Como juan se inscribio a boxeo en el mes, Esta inscripcion primero q se genera el pago y luego tambien guarda como dijimos historial de pagos */

    public BigDecimal calcularMontoMensual(){
        return actividad.getPrecio();
    }

    public Pago generarPagoMensual(int mes, int anio){
        Boolean yaExiiste = pagos.stream().anyMatch(p->p.getMes() == mes && p.getAnio() == anio);
        if(yaExiiste){
            throw new RuntimeException("Ya existe un pago para ese mes"); /* Esto posiblemente esa un metodo privado aparte */
        }
        BigDecimal monto = this.calcularMontoMensual();
        Pago pagoMensual = new Pago(mes, anio, monto ,this);
        pagos.add(pagoMensual);
        return pagoMensual;
    }

    public Boolean tienePagosAdeudados(){
        return pagos.stream().anyMatch(p-> p.getEstado() == EstadoPago.ADEUDA);
    }

    public Boolean adeudaMes(int mes,int anio){
        return pagos.stream().anyMatch(p->p.getMes() == mes && p.getAnio() == anio && p.getEstado() == EstadoPago.ADEUDA);
    }


    public BigDecimal calcularAdeudado() {
        return pagos.stream().filter(pago -> pago.getEstado() == EstadoPago.ADEUDA).map(Pago::getMontoAPagar).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void pagarTodo(){
        pagos.stream().filter(p->p.getEstado() == EstadoPago.ADEUDA).forEach(p->p.pagar());
    }

    public void pagarElMes(int mes, int anio){
        Pago pago = pagos.stream().filter(p -> p.getMes() == mes && p.getAnio() == anio).findFirst().orElseThrow(() ->
            new RuntimeException("No existe pago para ese mes")  /* Posible errores creado por nosotros tanto este como el de abajo en una carpeta de errores lo mismo de posibles metodos privados para la consulta */
        );

        if (pago.getEstado() == EstadoPago.PAGADO) {
            throw new RuntimeException("Ese mes ya está pagado");
    }

        pago.pagar();
    }

}
