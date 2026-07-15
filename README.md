# Gym Manager

Aplicación web para administrar la operación diaria de un gimnasio desde un único lugar. Permite gestionar clientes, actividades, instructores, asistencias, cobros y reportes, con acceso autenticado y soporte para múltiples usuarios.

La interfaz es responsive y cuenta con temas claro y oscuro.

## Funcionalidades

- Dashboard con métricas de ingresos, estado de clientes, deudores y próximos vencimientos.
- Alta, edición, consulta y baja de clientes.
- Inscripción de clientes en una o varias actividades.
- Administración de actividades, horarios, cupos e instructores.
- Registro y consulta de asistencias.
- Gestión de pagos, deudas, recargos y métodos de pago.
- Generación de comprobantes imprimibles.
- Exportación de reportes personalizados a Excel.
- Configuración de los datos del gimnasio y del contenido de los tickets.
- Administración de usuarios con permisos diferenciados.
- Autenticación, control de sesiones y contraseñas cifradas.
- Diseño responsive con modo oscuro persistente.

## Tecnologías

### Backend

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Spring Security
- Hibernate
- MariaDB
- Apache POI para archivos Excel
- Maven

### Frontend

- Thymeleaf
- HTML, CSS y JavaScript
- Bootstrap 5
- Lucide Icons y Bootstrap Icons
- Chart.js y ApexCharts
- Tom Select

## Requisitos

Antes de ejecutar el proyecto necesitás:

- JDK 21
- MariaDB
- Maven 3.9 o el Maven Wrapper incluido
- Un navegador moderno

Podés comprobar las instalaciones con:

```bash
java -version
mvn -version
```

## Configuración local

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd Organizador
```

### 2. Crear la base de datos

Ingresá a MariaDB y creá la base utilizada por la aplicación:

```sql
CREATE DATABASE GymOrganization
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar la conexión

Actualizá `src/main/resources/application.properties` con los datos de tu instalación:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/GymOrganization
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
```

No utilices credenciales reales ni contraseñas de producción dentro de archivos versionados.

### 4. Configurar el administrador inicial

Al iniciar con una base vacía, la aplicación crea un usuario `admin`. Su contraseña se obtiene de la propiedad `app.admin.password` y dispone de un valor de desarrollo si no se configura.

Para definirla mediante una variable de entorno en PowerShell:

```powershell
$env:APP_ADMIN_PASSWORD="una-contraseña-segura"
```

En Linux o macOS:

```bash
export APP_ADMIN_PASSWORD="una-contraseña-segura"
```

La variable debe establecerse antes del primer inicio que crea al administrador.

## Ejecución

Con Maven instalado:

```bash
mvn spring-boot:run
```

Con Maven Wrapper en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Con Maven Wrapper en Linux o macOS:

```bash
./mvnw spring-boot:run
```

Luego abrí:

```text
http://localhost:8080
```

## Compilación

Para generar el archivo ejecutable:

```bash
mvn clean package
```

El resultado se crea dentro de `target/`. Podés ejecutarlo con:

```bash
java -jar target/gym-manager-0.0.1-SNAPSHOT.jar
```

Para activar el perfil de producción:

```bash
java -jar target/gym-manager-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Antes de usar ese perfil, configurá la conexión a la base de datos y las credenciales mediante secretos o variables del entorno de despliegue.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/com/gymmanager/gym_manager/
│   │   ├── config/          # Seguridad y configuración general
│   │   ├── controllers/     # Rutas y manejo de solicitudes
│   │   ├── entity/          # Entidades JPA y DTO
│   │   ├── initializers/    # Datos y configuración inicial
│   │   ├── repository/      # Acceso a MariaDB
│   │   └── services/        # Lógica de negocio y exportaciones
│   └── resources/
│       ├── static/          # CSS, JavaScript e imágenes
│       ├── templates/       # Vistas Thymeleaf
│       ├── application.properties
│       └── application-prod.properties
└── test/                    # Pruebas automatizadas
```

## Módulos principales

| Módulo | Descripción |
| --- | --- |
| Inicio | Métricas y resumen operativo del gimnasio |
| Clientes | Datos personales, inscripciones e historial |
| Actividades | Precios, cupos, horarios e instructores |
| Asistencias | Registro diario de presentes |
| Ingresos | Pagos, deudas, métodos y comprobantes |
| Reportes | Selección de datos y exportación a Excel |
| Configuración | Datos del gimnasio, tickets, pagos y cuenta |
| Usuarios | Administración de accesos para el rol administrador |

## Seguridad

- Las contraseñas se almacenan cifradas con BCrypt.
- Las rutas internas requieren autenticación.
- La administración de usuarios está restringida al rol `ADMIN`.
- Las sesiones expiran después de un período de inactividad.
- Los formularios protegidos utilizan tokens CSRF de Spring Security.

## Estado del proyecto

El proyecto se encuentra en desarrollo activo. Algunas funcionalidades y configuraciones pueden cambiar hasta alcanzar una versión estable.

