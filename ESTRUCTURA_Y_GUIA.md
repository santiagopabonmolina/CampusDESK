# 🏢 CampusDESK — Guía de Arquitectura, Carpetas y Archivos

> **Documento Técnico de Referencia — TechNova Solutions**  
> **Desarrolladores:** Diego Parra, Carlos Lindarte, Santiago Pabón.

---

## 1. Visión General de la Arquitectura

El backend de **CampusDESK** está construido sobre una arquitectura limpia por capas (**Layered Architecture**) en **Spring Boot**, aplicando principios de programación orientada a objetos, inyección de dependencias (`@RequiredArgsConstructor`, `@Autowired`), inversión de control e interfaces desacopladas.

```
src/main/java/com/spring/
├── Application.java               # Entrada principal de Spring Boot
├── config/                        # Configuraciones generales (Swagger / OpenAPI)
├── controller/                    # Capa Controladores REST (HTTP / JSON)
├── dto/                           # Data Transfer Objects
│   ├── request/                   # DTOs de entrada (Payloads JSON recibidos)
│   └── response/                  # DTOs de salida (Respuestas JSON enviadas)
├── entity/                        # Entidades JPA (Mapeo a tablas PostgreSQL)
│   └── enums/                     # Constantes de dominio fuertemente tipadas
├── exception/                     # Manejo global y centralizado de errores
├── repository/                    # Acceso a datos con Spring Data JPA
├── security/                      # Seguridad, autenticación y autorización
│   ├── config/                    # Configuración de Spring Security, filtros y CORS
│   └── jwt/                       # Utilidades criptográficas y filtros JWT
└── service/                       # Capa de lógica y reglas del negocio
```

---

## 2. Responsabilidad de Cada Carpeta

| Carpeta / Paquete | Responsabilidad en la Arquitectura |
| :--- | :--- |
| **`com.spring`** | Raíz del proyecto. Contiene la clase principal que arranca el contexto de Spring. |
| **`com.spring.config`** | Configuración de componentes y bibliotecas externas (OpenAPI 3 / Swagger con JWT BearerAuth). |
| **`com.spring.controller`** | Expone la API REST. Recibe solicitudes HTTP, valida entradas con `@Valid`, delega la lógica a los servicios y retorna códigos HTTP semánticos (200, 201, 403, 404, 409). |
| **`com.spring.dto.request`** | Modelos de entrada para deserializar los JSONs de clientes en operaciones `POST`, `PUT` y `PATCH`. Aíslan la entidad de la entrada del usuario. |
| **`com.spring.dto.response`** | Modelos de salida devueltos como JSON. Evitan bucles de serialización infinita (`LazyInitializationException`), aplanan relaciones y protegen datos sensibles como contraseñas. |
| **`com.spring.entity`** | Entidades mapeadas con Jakarta Persistence (JPA/Hibernate) hacia las tablas de PostgreSQL (`users`, `role`, `tickets`, `comments`, `ticket_history`). |
| **`com.spring.entity.enums`** | Enums Java que restringen los valores válidos de roles, categorías, prioridades y estados. |
| **`com.spring.exception`** | Manejador global (`@RestControllerAdvice`) que intercepta excepciones en cualquier capa y devuelve respuestas JSON limpias y homogéneas. |
| **`com.spring.repository`** | Interfaces que extienden `JpaRepository`. Generan consultas SQL optimizadas automáticamente mediante Spring Data JPA. |
| **`com.spring.security.config`** | Configuración de la cadena de filtros de seguridad (`SecurityFilterChain`), codificador BCrypt, reglas CORS y carga de usuarios para Spring Security. |
| **`com.spring.security.jwt`** | Filtro de intercepción (`OncePerRequestFilter`) y utilidades para generar, firmar y validar tokens JWT. |
| **`com.spring.service`** | Núcleo del negocio. Implementa las reglas de transición del ciclo de vida, transaccionalidad atómica (`@Transactional`) y control de acceso a recursos. |
| **`Fronted/`** | Cliente web modular desarrollado exclusivamente en HTML5, CSS3 y Vanilla JavaScript (ES Modules). |

---

## 3. Detalle Archivo por Archivo (Backend Java)

### 3.1. Punto de Entrada
* **`Application.java`**:
  * Clase principal anotada con `@SpringBootApplication`.
  * Arranca el servidor embebido Tomcat en el puerto `8080`, escanea componentes y habilita la configuración automática de Spring.

---

### 3.2. Configuración (`com.spring.config`)
* **`OpenApiConfig.java`**:
  * Define el Bean `OpenAPI` para Swagger UI (`/swagger-ui.html`).
  * Configura el esquema de seguridad `BearerAuth` para permitir autenticación por JWT interactiva con el botón **Authorize**.

---

### 3.3. Controladores REST (`com.spring.controller`)
* **`AuthController.java`**:
  * Gestiona `/api/auth/register` (crea usuarios y asigna automáticamente rol `USER`) y `/api/auth/login` (autentica credenciales con BCrypt y retorna el token JWT).
* **`TicketController.java`**:
  * Controlador central del sistema (`/api/tickets`).
  * Endpoints para crear ticket, listar tickets filtrados por rol y estado, ver detalle, editar tickets abiertos, asignar técnico (`@PreAuthorize("hasRole('ADMIN')")`), cambiar estado y gestionar comentarios (`/{id}/comments`).
* **`UserController.java`**:
  * Expone `/api/users/technicians` (para listar técnicos asignables) y `/api/users` (listado completo de personal exclusivo para `ADMIN`).
* **`DashboardController.java`**:
  * Endpoint `/api/reports/summary`. Devuelve conteos consolidados de tickets por estado para el panel de métricas.
* **`TicketHistoryController.java`**:
  * Endpoint de solo lectura `/api/tickets/{ticketId}/history`. Retorna la línea de tiempo de auditoría de un ticket específico.
* **`CommentController.java`**:
  * Controlador complementario para gestión de comentarios directos en `/api/comments`.
* **`RoleController.java`**:
  * Endpoints administrativos protegidos (`/api/roles`) para consultar y registrar roles.

---

### 3.4. DTOs de Entrada (`com.spring.dto.request`)
* **`RegisterRequest.java`**:
  * Payload de registro: `firstName`, `lastName`, `email`, `password` (validado con `@Size(min = 6, max = 150)`).
* **`LoginRequest.java`**:
  * Payload de inicio de sesión: `email` y `password`.
* **`TicketRequest.java`**:
  * Payload para crear o modificar incidencias: `title`, `description`, `category`, `priority`, `requesterId`, `technicianId`.
* **`CommentRequest.java`**:
  * Payload para agregar notas: `content`, `ticketId`, `authorId`.
* **`RoleRequest.java`**:
  * Payload para crear roles: `name`.

---

### 3.5. DTOs de Salida (`com.spring.dto.response`)
* **`JwtResponse.java`**:
  * Respuesta de autenticación con `token`, `type = "Bearer"`, `userId`, `email`, `firstName`, `lastName` y `role`.
* **`TicketResponse.java`**:
  * Salida aplanada del ticket con datos del solicitante y técnico asignado, sin exponer contraseñas ni entidades crudas.
* **`CommentResponse.java`**:
  * Datos formateados del comentario (`id`, `content`, `authorId`, `ticketId`, `createdAt`).
* **`TicketHistoryResponse.java`**:
  * Registro inmutable de cambio de estado (`previousStatus`, `newStatus`, `changedByEmail`, `changedAt`).
* **`DashboardResponse.java`**:
  * Objeto con contadores totales (`totalTickets`, `opened`, `assigned`, `inProcess`, `resolved`, `closed`).
* **`RoleResponse.java`**:
  * Representación simple de un rol registrado (`id`, `name`).

---

### 3.6. Entidades Persistentes (`com.spring.entity`)
* **`User.java`**:
  * Mapeada a la tabla `users`. Contiene datos personales, estado activo, fecha de registro y relación `@ManyToMany` con roles. La contraseña tiene `@JsonProperty(access = WRITE_ONLY)` para evitar filtraciones en JSON.
* **`Ticket.java`**:
  * Mapeada a la tabla `tickets`. Representa la incidencia con título, descripción, categoría, prioridad, estado y relaciones `@ManyToOne` con solicitante (`requester`) y técnico (`technicianAssigned`).
* **`Role.java`**:
  * Mapeada a la tabla `role`. Almacena los roles disponibles (`ADMIN`, `TECHNICIAN`, `USER`).
* **`Comment.java`**:
  * Mapeada a la tabla `comments`. Almacena observaciones y notas cronológicas con referencias a su autor y ticket.
* **`TicketHistory.java`**:
  * Mapeada a la tabla `ticket_history`. Registro inmutable de transiciones de estado para auditoría obligatoria.

---

### 3.7. Enumeraciones (`com.spring.entity.enums`)
* **`RoleName.java`**: `ADMIN`, `TECHNICIAN`, `USER`.
* **`TicketState.java`**: `OPENED`, `ASSIGNED`, `IN_PROCESS`, `RESOLVED`, `CLOSED`.
* **`TicketPriority.java`**: `LOW`, `MEDIUM`, `HIGH`, `CRITIC`.
* **`TicketCategory.java`**: `HARDWARE`, `SOFTWARE`, `NETWORK`, `ACCESS`, `OTHER`.

---

### 3.8. Manejo de Errores (`com.spring.exception`)
* **`GlobalExceptionHandler.java`**:
  * Captura errores de validación (`MethodArgumentNotValidException`), conflictos de negocio y excepciones de servidor, retornando respuestas estructuradas con formato JSON y código HTTP apropiado.

---

### 3.9. Capa de Repositorios (`com.spring.repository`)
* **`UserRepository.java`**: Búsquedas por correo (`findByEmail`, `existsByEmail`) y filtrado de técnicos activos.
* **`TicketRepository.java`**: Consultas por solicitante, técnico, estado, prioridad y conteos para el Dashboard.
* **`RoleRepository.java`**: Búsqueda de roles por nombre (`findByName`).
* **`CommentRepository.java`**: Búsqueda cronológica de comentarios por ID de ticket.
* **`TicketHistoryRepository.java`**: Búsqueda cronológica del historial de cambios de un ticket.

---

### 3.10. Seguridad y JWT (`com.spring.security`)
* **`SecurityConfig.java`**:
  * Configura el `SecurityFilterChain`: política `SessionCreationPolicy.STATELESS`, desactivación de CSRF para API REST, configuración CORS para orígenes del frontend, y protección de endpoints con `@EnableMethodSecurity`.
* **`CustomUserDetailsService.java`**:
  * Implementa `UserDetailsService`. Carga el usuario desde PostgreSQL por correo y transforma sus roles en `GrantedAuthority` (`ROLE_ADMIN`, etc.).
* **`JwtTokenProvider.java`**:
  * Genera tokens JWT firmados con HMAC-SHA256, extrae claims (`sub`, `role`, `userId`) y valida expiración y firma.
* **`JwtAuthenticationFilter.java`**:
  * Intercepta cada solicitud HTTP, extrae la cabecera `Authorization: Bearer <token>`, valida el token y carga la autenticación en el `SecurityContextHolder`.

---

### 3.11. Capa de Servicios (`com.spring.service`)
* **`UserService.java`**: Listado de técnicos activos y consulta de todos los usuarios registrados.
* **`TicketService.java`**:
  * Corazón del sistema: creación de tickets (`OPENED`), filtrado por roles (`ADMIN` todos, `TECH` asignados, `USER` propios), asignación de técnicos (`ASSIGNED`) y validación estricta de transiciones de estado (`IN_PROCESS` ➔ `RESOLVED` ➔ `CLOSED`).
* **`CommentService.java`**: Creación y validación de comentarios (rechaza comentarios si el ticket está en estado `CLOSED`).
* **`TicketHistoryService.java`**: Registro atómico y transaccional obligatorio de cada cambio de estado.
* **`DashboardService.java`**: Conteo de tickets agrupados por estado para el panel de métricas.
* **`RoleService.java`**: Creación y listado de roles del sistema.

---

## 4. Frontend Modular (`Fronted/`)

Desarrollado en **Vanilla JavaScript** con arquitectura modular ES6:

* **`index.html` + `js/index.js`**: Pantalla de autenticación (Login y Registro dinámico).
* **`dashboard.html` + `js/dashboard.js`**: Panel de métricas e indicadores de rendimiento según el rol.
* **`tickets.html` + `js/tickets.js`**: Bandeja de solicitudes, filtros por estado y modal de creación de tickets.
* **`ticket-detail.html` + `js/ticket-detail.js`**: Vista detallada con Stepper del ciclo de vida, comentarios en tiempo real, timeline de auditoría y botones de acción desbloqueados según el rol del usuario autenticado.
* **`admin.html` + `js/admin.js`**: Directorio de personal y usuarios exclusivo para Administradores.
* **`js/api.js`**: Centralizador de llamadas HTTP con `fetch()` y cabeceras JWT automáticas.
* **`js/auth.js`**: Manejador de tokens en `sessionStorage`, decodificación de roles y guards de navegación (`checkAuth()`, `checkAdminAccess()`).
* **`js/ui.js`**: Funciones auxiliares para Toasts visuales, modales, spinners y formateo de fechas.

---

## 5. Infraestructura y Base de Datos (Docker Compose)

El archivo [`docker-compose.yml`](file:///home/diego/CampusDESK/docker-compose.yml) levanta:

1. **`dev_postgres` (PostgreSQL 17):**
   * **Puerto:** `5438:5432`
   * **Base de datos:** `campusDESK`
   * **Credenciales:** Usuario `dev_user`, Contraseña `DevTeamSecurePass2026!`
   * **Volumen de Scripts:** `./mis_scripts:/docker-entrypoint-initdb.d:ro` (ejecuta automáticamente [`mis_scripts/init-database.sql`](file:///home/diego/CampusDESK/mis_scripts/init-database.sql) en el primer arranque).
2. **`dev_pgadmin` (pgAdmin 4):**
   * **Puerto:** `5055:80` (Interfaz gráfica para administrar PostgreSQL).
   * **Credenciales:** `team@devlocal.com` / `PgAdminTeamPass2026!`
