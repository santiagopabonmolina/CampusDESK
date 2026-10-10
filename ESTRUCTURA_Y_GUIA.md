# CampusDESK - Guía de Arquitectura, Carpetas y Archivos

Este documento detalla la responsabilidad de cada carpeta y de cada archivo en el proyecto **CampusDESK** (excluyendo los que ya están previamente desarrollados: `User`, `Ticket`, los Enums y `Application`), junto con la guía completa de código para implementar:
* `UserRepository`
* `TicketRepository`
* `TicketRequest`
* `TicketResponse`
* `TicketController` (con soporte para `TicketService`)

---

## 1. Estructura General de Carpetas y Responsabilidades

El proyecto está diseñado bajo una arquitectura limpia en capas con **Spring Boot**:

```
src/main/java/com/spring/
├── Application.java               # Punto de entrada de la aplicación Spring Boot (HECHO)
├── controller/                    # Capa de controladores REST (expone endpoints HTTP)
├── dto/
│   ├── request/                   # DTOs de entrada (cuerpos JSON de peticiones)
│   └── response/                  # DTOs de salida (respuestas JSON hacia los clientes)
├── entity/                        # Entidades JPA mapeadas a PostgreSQL
│   └── enums/                     # Enumeraciones de dominio (RoleName, TicketState, etc.) (HECHO)
├── exception/                     # Manejo global y centralizado de excepciones
├── repository/                    # Interfaces Spring Data JPA para acceso a base de datos
├── security/
│   ├── config/                    # Configuración de Spring Security, filtros y CORS
│   └── jwt/                       # Proveedor y filtros de tokens JWT
└── service/                       # Capa de lógica de negocio y transacciones
```

### Tabla de Responsabilidades por Carpeta

| Carpeta / Paquete | Responsabilidad Principal |
| :--- | :--- |
| `com.spring.entity` | Modelos de persistencia mapeados a tablas de PostgreSQL con JPA/Hibernate. Representan el estado y relaciones (`User`, `Ticket`, `Role`, `Comment`, `TicketHistory`). |
| `com.spring.entity.enums` | Valores constantes fuertemente tipados (`RoleName`, `TicketPriority`, `TicketCategory`, `TicketState`). |
| `com.spring.repository` | Interfaces que extienden `JpaRepository`. Proveen operaciones CRUD y consultas derivadas automáticas sin necesidad de escribir SQL manual. |
| `com.spring.service` | Orquesta la lógica del negocio, validaciones de reglas, transaccionalidad (`@Transactional`) y transforma datos entre entidades y DTOs. |
| `com.spring.controller` | Endpoints REST HTTP. Recibe peticiones HTTP, deserializa JSON a DTOs Request, invoca la capa de servicio y devuelve DTOs Response con códigos de estado HTTP semánticos (200, 201, 204, 404, etc.). |
| `com.spring.dto.request` | Clases Java que reciben la información que el cliente envía en peticiones `POST`/`PUT`/`PATCH`. Aíslan la entidad de la entrada del usuario. |
| `com.spring.dto.response` | Clases Java devueltas al cliente en formato JSON. Evitan bucles de serialización infinita (`LazyInitialization` o referencias bidireccionales) y ocultan datos sensibles (como contraseñas). |
| `com.spring.security.config` | Configuración de seguridad con `SecurityFilterChain`: control de accesos por roles, políticas de sesiones sin estado (Stateless), protección CSRF y CORS. |
| `com.spring.security.jwt` | Lógica de generación, firma, verificación de expiración y extracción de credenciales desde tokens JWT en las cabeceras HTTP. |
| `com.spring.exception` | Interceptores con `@RestControllerAdvice` para capturar errores como `EntityNotFoundException` o validaciones y retornar respuestas JSON estructuradas con formato amigable. |

---

## 2. Detalle de Archivos Pendientes (Qué va en cada uno)

### 2.1. Entidades (`com.spring.entity`)

* **`Role.java`**:
  * Representa los roles disponibles (`ADMIN`, `TECHNICIAN`, `USER`).
  * Debe contener `@Id`, `@GeneratedValue` y el campo `name` mapeado al enum `RoleName` con `@Enumerated(EnumType.STRING)`.
* **`Comment.java`**:
  * Comentarios y notas realizados sobre un ticket.
  * Debe contener `id`, `content` (TEXT), relación `@ManyToOne` con `Ticket`, relación `@ManyToOne` con `User` (el autor) y `createDate`.
* **`TicketHistory.java`**:
  * Registro de auditoría y trazabilidad de cada cambio en un ticket.
  * Debe contener `id`, relación con `Ticket`, usuario que ejecutó la acción, `action` (ej: "ASIGNADO", "CAMBIO_ESTADO"), estados anterior y posterior (`TicketState`), y `timestamp`.

---

### 2.2. Repositorios (`com.spring.repository`)

* **`UserRepository.java`**: Consultas para autenticación y usuarios (`findByEmail`, `existsByEmail`).
* **`TicketRepository.java`**: Consultas filtradas por solicitante, técnico, estado, prioridad y conteos para métricas.
* **`RoleRepository.java`**: `findByName(RoleName name)` para vincular roles al crear usuarios.
* **`CommentRepository.java`**: `findByTicketIdOrderByCreateDateAsc(Long ticketId)` para listar comentarios en orden temporal.
* **`TicketHistoryRepository.java`**: `findByTicketIdOrderByTimestampDesc(Long ticketId)` para ver la auditoría del ticket.

---

### 2.3. Capa de Servicios (`com.spring.service`)

* **`UserService.java`**: Registro de usuarios, consulta de perfil, listado de técnicos activos, cambio de estado activo/inactivo.
* **`TicketService.java`**: Flujo de vida del ticket:
  * Crear ticket (asignando estado inicial `OPENED`).
  * Asignar técnico al ticket (cambia a `ASSIGNED`).
  * Cambiar de estado (`IN_PROCESS`, `RESOLVED`, `CLOSED`).
  * Consultar tickets con filtros (por solicitante, técnico o estado).
* **`CommentService.java`**: Agregar comentarios a un ticket validando permisos del autor.
* **`DashboardService.java`**: Calcular contadores totales (tickets abiertos, resueltos, cerrados, distribución por prioridad y categoría).

---

### 2.4. Data Transfer Objects (DTOs)

* **`dto/request/TicketRequest.java`**: Payload para crear o modificar un ticket (`title`, `description`, `category`, `priority`, `requesterId`).
* **`dto/request/CommentRequest.java`**: Payload para agregar comentario (`ticketId`, `content`).
* **`dto/request/LoginRequest.java`**: Credenciales de inicio de sesión (`email`, `password`).
* **`dto/request/RegisterRequest.java`**: Datos para registrar un nuevo usuario (`firstName`, `lastName`, `email`, `password`, `roles`).
* **`dto/response/TicketResponse.java`**: Salida detallada y plana del ticket sin exponer entidades completas ni relaciones circulares.
* **`dto/response/JwtResponse.java`**: Devuelve token JWT, tipo Bearer y datos básicos del usuario autenticado.
* **`dto/response/DashboardResponse.java`**: Estadísticas numéricas para la pantalla de inicio del administrador.

---

### 2.5. Controladores REST (`com.spring.controller`)

* **`TicketController.java`**: Endpoints `/api/tickets` (CRUD y transiciones de tickets).
* **`AuthController.java`**: Endpoints `/api/auth/login` y `/api/auth/register`.
* **`CommentController.java`**: Endpoints `/api/comments` para añadir comentarios.
* **`DashboardController.java`**: Endpoints `/api/dashboard/stats` para ver analíticas.

---

### 2.6. Seguridad (`com.spring.security`)

* **`SecurityConfig.java`**: Cadena de filtros `SecurityFilterChain`, codificador `BCryptPasswordEncoder`, rutas públicas (`/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`) y rutas aseguradas.
* **`JwtTokenProvider.java`**: Utilidad criptográfica para generar y verificar tokens JWT.
* **`JwtAuthenticationFilter.java`**: Filtro que intercepta el encabezado `Authorization: Bearer <token>`, valida el token y autentica la solicitud en el `SecurityContext`.

---

### 2.7. Excepciones (`com.spring.exception`)

* **`GlobalExceptionHandler.java`**: `@RestControllerAdvice` para transformar excepciones en respuestas JSON con código HTTP, timestamp y mensaje descriptivo.

---

## 3. Implementación Paso a Paso de los Componentes Solicitados

### Paso 1: `UserRepository.java`
**Ruta:** `src/main/java/com/spring/repository/UserRepository.java`

```java
package com.spring.repository;

import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por correo electrónico (clave para autenticación)
    Optional<User> findByEmail(String email);

    // Verificar existencia de correo antes de registrar
    boolean existsByEmail(String email);

    // Listar solo usuarios activos
    List<User> findByActiveTrue();

    // Obtener usuarios que posean un rol específico (ej: técnicos disponibles)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findByRoleNameAndActiveTrue(@Param("roleName") RoleName roleName);
}
```

---

### Paso 2: `TicketRepository.java`
**Ruta:** `src/main/java/com/spring/repository/TicketRepository.java`

```java
package com.spring.repository;

import com.spring.entity.Ticket;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Tickets creados por un usuario en específico
    List<Ticket> findByRequesterId(Long requesterId);

    // Tickets asignados a un técnico específico
    List<Ticket> findByTechnicianAssignedId(Long technicianId);

    // Filtrar tickets por estado actual
    List<Ticket> findByState(TicketState state);

    // Filtrar por prioridad
    List<Ticket> findByPriority(TicketPriority priority);

    // Filtrar por categoría
    List<Ticket> findByCategory(TicketCategory category);

    // Conteo por estado (ideal para estadísticas del Dashboard)
    long countByState(TicketState state);

    // Conteo por prioridad
    long countByPriority(TicketPriority priority);
}
```

---

### Paso 3: `TicketRequest.java`
**Ruta:** `src/main/java/com/spring/dto/request/TicketRequest.java`

```java
package com.spring.dto.request;

import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {

    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;

    // ID del usuario que solicita el ticket
    private Long requesterId;

    // ID del técnico asignado (opcional al crear)
    private Long technicianId;
}
```

---

### Paso 4: `TicketResponse.java`
**Ruta:** `src/main/java/com/spring/dto/response/TicketResponse.java`

El DTO de respuesta aplana la entidad, evitando devolver la contraseña del usuario o colecciones pesadas no requeridas por la vista:

```java
package com.spring.dto.response;

import com.spring.entity.Ticket;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketState state;

    // Datos simplificados del solicitante
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;

    // Datos simplificados del técnico asignado (si existe)
    private Long technicianId;
    private String technicianName;

    private LocalDateTime createDate;
    private LocalDateTime resolutionDate;
    private LocalDateTime closeDate;

    // Método helper de mapeo Entity -> DTO
    public static TicketResponse fromEntity(Ticket ticket) {
        if (ticket == null) return null;

        String reqName = null;
        String reqEmail = null;
        if (ticket.getRequester() != null) {
            reqName = ticket.getRequester().getFirstName() + " " + ticket.getRequester().getLastName();
            reqEmail = ticket.getRequester().getEmail();
        }

        String techName = null;
        Long techId = null;
        if (ticket.getTechnicianAssigned() != null) {
            techId = ticket.getTechnicianAssigned().getId();
            techName = ticket.getTechnicianAssigned().getFirstName() + " " + ticket.getTechnicianAssigned().getLastName();
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .state(ticket.getState())
                .requesterId(ticket.getRequester() != null ? ticket.getRequester().getId() : null)
                .requesterName(reqName)
                .requesterEmail(reqEmail)
                .technicianId(techId)
                .technicianName(techName)
                .createDate(ticket.getCreateDate())
                .resolutionDate(ticket.getResolutionDate())
                .closeDate(ticket.getCloseDate())
                .build();
    }
}
```

---

### Paso 5: `TicketController.java`
**Ruta:** `src/main/java/com/spring/controller/TicketController.java`

El controlador expone los endpoints REST y delega la lógica de negocio a `TicketService`:

```java
package com.spring.controller;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketState;
import com.spring.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // 1. Crear nuevo ticket
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Obtener todos los tickets (con filtro opcional por estado)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(@RequestParam(required = false) TicketState state) {
        List<TicketResponse> tickets = ticketService.getAllTickets(state);
        return ResponseEntity.ok(tickets);
    }

    // 3. Obtener ticket por ID
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    // 4. Listar tickets creados por un solicitante
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(ticketService.getTicketsByRequester(requesterId));
    }

    // 5. Listar tickets asignados a un técnico
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId));
    }

    // 6. Asignar técnico a un ticket
    @PatchMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @PathVariable Long technicianId) {
        TicketResponse updated = ticketService.assignTechnician(id, technicianId);
        return ResponseEntity.ok(updated);
    }

    // 7. Cambiar estado de un ticket
    @PatchMapping("/{id}/state")
    public ResponseEntity<TicketResponse> updateTicketState(
            @PathVariable Long id,
            @RequestParam TicketState newState) {
        TicketResponse updated = ticketService.updateState(id, newState);
        return ResponseEntity.ok(updated);
    }

    // 8. Actualizar información general de un ticket
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequest request) {
        TicketResponse updated = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(updated);
    }

    // 9. Eliminar ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. Complemento Recomendado: `TicketService.java`

Para que `TicketController` funcione perfectamente sin errores de compilación, en `src/main/java/com/spring/service/TicketService.java` puedes definir los métodos de la interfaz o la clase de servicio:

```java
package com.spring.service;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketState;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketRequest request);
    List<TicketResponse> getAllTickets(TicketState state);
    TicketResponse getTicketById(Long id);
    List<TicketResponse> getTicketsByRequester(Long requesterId);
    List<TicketResponse> getTicketsByTechnician(Long technicianId);
    TicketResponse assignTechnician(Long ticketId, Long technicianId);
    TicketResponse updateState(Long ticketId, TicketState newState);
    TicketResponse updateTicket(Long ticketId, TicketRequest request);
    void deleteTicket(Long ticketId);
}
```
