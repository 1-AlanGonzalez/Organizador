# Sistema De Gestión De Clientes Y Asistencias
## Modelo de Dominio

El sistema está compuesto por las siguientes entidades principales:

## Cliente

Representa a una persona que se inscribe en una o más actividades.

### Atributos:

- id (Long)

- nombre (String)

- apellido (String)

- telefono (String)

- dni (String)

### Responsabilidades:

- Almacenar información personal del cliente
- Se relaciona con Asistencia para el registro de cuándo asistió.
- Se relaciona con ClienteActividad para inscribirse a una o varias actividades

## Actividad

Representa una actividad disponible (ej: Funcional, Gym, Boxeo).

### Atributos:

id (Long)

nombre (String)

cupoMaximo (int)

precio (BigDecimal)

instructor (Instructor)

### Responsabilidades:

Definir las características de la actividad

Controlar el cupo máximo

Se relaciona con ClienteActividad para relacionarse con todos los clientes que se inscriben
Se relaciona con Asistencia para saber quién asistió
Tiene un instructor como atributo 
## Instructor

Representa a la persona encargada de dictar una actividad.

### Atributos:

id (Long)

nombre (String)

apellido (String)

dni (String)

telefono (String)

### Responsabilidades:

Identificar al instructor de una actividad

Permitir reutilización del instructor en múltiples actividades

## Asistencia

Registra la presencia de un cliente en una actividad en una fecha determinada.

### Atributos:

id (Long)

fecha (LocalDate)

cliente (Cliente)

actividad (Actividad)

### Responsabilidades:

Registrar asistencias diarias.

Se relaciona con Cliente para saber quién asistió
Se relaciona con Actividad para saber a qué actividad asistió


## Pago

Representa un pago realizado por un cliente respecto a una actividad.

### Atributos:

id (Long)

tipoPago (enum: EFECTIVO | TRANSFERENCIA)

monto (BigDecimal)

### Responsabilidades:

Registrar pagos por un cliente 

Asociar pagos a una inscripción concreta

Se relacionaría con ClienteActividad debido a que cada pago está asociado a una inscripción

## ClienteActividad (Entidad intermedia)

Modela la relación muchos a muchos entre Cliente y Actividad.

### Atributos:

id (Long)

cliente (Cliente)

actividad (Actividad)

fechaInscripcion (LocalDate)

costo (BigDecimal)

## Responsabilidades:

Entidad intermedia entre Cliente y Actividad, porque un cliente puede tener muchas actividades y una actividad puede tener muchos clientes.

Servir como referencia para pagos

Se relaciona con Cliente y Actividad

Se relaciona con Pago 


# Flujo de uso conceptual

Cliente se inscribe en Actividad → se crea un registro en ClienteActividad.

Cliente realiza pagos → se crean registros en Pago, referenciando ClienteActividad.

Cliente asiste a clases → se crean registros en Asistencia, asociando Cliente y Actividad.

Actividad tiene un Instructor asignado 