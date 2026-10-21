# CampusDESK - Guía Completa de Implementación: 5 Ejercicios de Examen (README 2)

Este documento es una guía técnica, pedagógica y práctica diseñada para la preparación y defensa de examen sobre el proyecto **CampusDESK** (Spring Boot 3, Spring Data JPA, Spring Security con JWT, PostgreSQL y arquitectura en capas).

Para cada uno de los 5 ejercicios solicitados se detalla de forma exhaustiva:
1. **¿Dónde se edita?** (Ubicación exacta, capa de arquitectura y rutas de archivos).
2. **¿Qué se pone?** (Código fuente completo, limpio y listo para producción en Java y SQL).
3. **¿Cómo se añade?** (Paso a paso: importaciones, anotaciones, inyección de dependencias y cableado).
4. **¿Por qué se hace así?** (Fundamento teórico, conceptos de JPA/Hibernate, reglas de negocio, validaciones y seguridad RBAC para defender ante un jurado).

---

## Tabla de Contenido
1. [Ejercicio 1: Sistema de Etiquetas (Tags) y Categorización Dinámica](#ejercicio-1-sistema-de-etiquetas-tags-y-categorización-dinámica)
2. [Ejercicio 2: Reanudación y Reapertura de Tickets Resueltos](#ejercicio-2-reanudación-y-reapertura-de-tickets-resueltos)
3. [Ejercicio 3: Transferencia de Tickets entre Departamentos / Áreas](#ejercicio-3-transferencia-de-tickets-entre-departamentos--áreas)
4. [Ejercicio 4: Bitácora de Registro de Tiempo Técnico (Worklog)](#ejercicio-4-bitácora-de-registro-de-tiempo-técnico-worklog)
5. [Ejercicio 5: Plantillas de Solución Rápida (Canned Responses)](#ejercicio-5-plantillas-de-solución-rápida-canned-responses)
6. [Resumen Rápido de Comandos y Pruebas con cURL](#resumen-rápido-de-comandos-y-pruebas-con-curl)

---

# Ejercicio 1: Sistema de Etiquetas (Tags) y Categorización Dinámica

### Enfoque Técnico
Relaciones `@ManyToMany`, mapeo de colecciones con `@JoinTable`, consultas JPQL con filtros por listas y normalización de cadenas.

---

### 1. ¿Dónde se edita?
| Capa | Archivo / Ruta | Tipo de Operación |
| :--- | :--- | :--- |
| **Entidad** | `src/main/java/com/spring/entity/Tag.java` | **Crear archivo nuevo** |
| **Entidad** | `src/main/java/com/spring/entity/Ticket.java` | **Modificar archivo existente** |
| **Repositorio** | `src/main/java/com/spring/repository/TagRepository.java` | **Crear archivo nuevo** |
| **Repositorio** | `src/main/java/com/spring/repository/TicketRepository.java` | **Modificar archivo existente** |
| **DTO Response** | `src/main/java/com/spring/dto/response/TagResponse.java` | **Crear archivo nuevo** |
| **Servicio** | `src/main/java/com/spring/service/TicketService.java` | **Modificar archivo existente** |
| **Controlador** | `src/main/java/com/spring/controller/TicketController.java` | **Modificar archivo existente** |
| **Base de Datos**| `scripts/01-schema.sql` (opcional si JPA no usa `ddl-auto=update`) | **Añadir tablas `tag` y `ticket_tag`** |

---

### 2. ¿Qué se pone?

#### A) Nueva Entidad: `Tag.java`
**Ruta:** `src/main/java/com/spring/entity/Tag.java`
```java
package com.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Ticket> tickets = new HashSet<>();

    public Tag(String nombre) {
        this.nombre = nombre;
    }
}
```

#### B) Modificación en Entidad: `Ticket.java`
**Ruta:** `src/main/java/com/spring/entity/Ticket.java`
Añadir el campo `tags`:
```java
    // Mapeo ManyToMany hacia la entidad Tag con tabla intermedia ticket_tag
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "ticket_tag",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
```

#### C) Nuevo Repositorio: `TagRepository.java`
**Ruta:** `src/main/java/com/spring/repository/TagRepository.java`
```java
package com.spring.repository;

import com.spring.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
```

#### D) Modificación en `TicketRepository.java`
**Ruta:** `src/main/java/com/spring/repository/TicketRepository.java`
Añadir la consulta JPQL para filtrar por nombre de etiqueta:
```java
    @Query("SELECT DISTINCT t FROM Ticket t JOIN t.tags tag WHERE UPPER(tag.nombre) = UPPER(:nombre)")
    List<Ticket> findByTagNombreIgnoreCase(@Param("nombre") String nombre);
```

#### E) Nuevo DTO: `TagResponse.java`
**Ruta:** `src/main/java/com/spring/dto/response/TagResponse.java`
```java
package com.spring.dto.response;

import com.spring.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String nombre;

    public static TagResponse fromEntity(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .nombre(tag.getNombre())
                .build();
    }
}
```

#### F) Métodos a añadir en `TicketService.java`
**Ruta:** `src/main/java/com/spring/service/TicketService.java`
1. Inyectar `TagRepository`:
```java
    private final TagRepository tagRepository;
```
*(Lombok con `@RequiredArgsConstructor` generará automáticamente el constructor con este nuevo campo final).*

2. Añadir los métodos de negocio:
```java
    /**
     * Asocia una lista de etiquetas a un ticket existente.
     * Solo permitido si el usuario es ADMIN o el TECHNICIAN asignado.
     */
    @Transactional
    public TicketResponse addTagsToTicket(Long ticketId, List<String> tagNames, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + ticketId));

        // Validación de permisos: ADMIN o el TECHNICIAN asignado
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isAssignedTech = ticket.getTechnicianAssigned() != null &&
                ticket.getTechnicianAssigned().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdmin && !isAssignedTech) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Solo un administrador o el técnico asignado pueden agregar etiquetas a este ticket");
        }

        if (tagNames == null || tagNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista de etiquetas no puede estar vacía");
        }

        for (String rawName : tagNames) {
            if (rawName == null || rawName.trim().isEmpty()) continue;

            // 1. Normalización estricta en mayúsculas sin espacios redundantes
            String normalized = rawName.trim().toUpperCase();

            // 2. Si la etiqueta no existe, se crea automáticamente; si existe, se vincula
            Tag tag = tagRepository.findByNombreIgnoreCase(normalized)
                    .orElseGet(() -> tagRepository.save(Tag.builder().nombre(normalized).build()));

            ticket.getTags().add(tag);
        }

        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(saved);
    }

    /**
     * Retorna tickets asociados a una etiqueta respetando la visibilidad por rol.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTag(String nombre, Authentication authentication) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro 'nombre' de la etiqueta es obligatorio");
        }

        String normalized = nombre.trim().toUpperCase();
        List<Ticket> matchingTickets = ticketRepository.findByTagNombreIgnoreCase(normalized);

        // Si no hay autenticación, retorna todo (o restringe según configuración)
        if (authentication == null) {
            return matchingTickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
        }

        String email = authentication.getName();
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

        // Filtrado por visibilidad de rol:
        // ADMIN: ve todos
        // TECHNICIAN: solo si está asignado a él
        // USER: solo si es el creador (requester)
        return matchingTickets.stream()
                .filter(ticket -> {
                    if (isAdmin) return true;
                    if (isTechnician) {
                        return ticket.getTechnicianAssigned() != null &&
                                ticket.getTechnicianAssigned().getEmail().equalsIgnoreCase(email);
                    }
                    return ticket.getRequester() != null &&
                            ticket.getRequester().getEmail().equalsIgnoreCase(email);
                })
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }
```

#### G) Endpoints a añadir en `TicketController.java`
**Ruta:** `src/main/java/com/spring/controller/TicketController.java`
```java
    @Operation(summary = "Asociar etiquetas a un ticket", description = "Asocia una lista de tags al ticket. Solo ADMIN o el técnico asignado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etiquetas asociadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Lista de etiquetas inválida"),
            @ApiResponse(responseCode = "403", description = "No autorizado para etiquetar este ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PostMapping("/{id}/tags")
    public ResponseEntity<TicketResponse> addTags(
            @PathVariable Long id,
            @RequestBody List<String> tags,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.addTagsToTicket(id, tags, authentication));
    }

    @Operation(summary = "Consultar tickets por etiqueta", description = "Lista los tickets con la etiqueta indicada, respetando la visibilidad del rol.")
    @GetMapping("/por-tag")
    public ResponseEntity<List<TicketResponse>> getTicketsByTag(
            @RequestParam("nombre") String nombre,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsByTag(nombre, authentication));
    }
```

---

### 3. ¿Cómo se añade? (Paso a Paso)
1. **Crear `Tag.java`**: En `src/main/java/com/spring/entity/`, crea el archivo con la definición de la entidad. Fíjate en el uso de `@Table(name = "tag")` y la restricción `unique = true` en `nombre`.
2. **Abrir `Ticket.java`**: Añade el atributo `private Set<Tag> tags = new HashSet<>();` encima del método `@PrePersist`. Asegúrate de importar `java.util.Set` y `java.util.HashSet`.
3. **Crear `TagRepository.java`**: En `src/main/java/com/spring/repository/`, crea la interfaz extendiendo `JpaRepository<Tag, Long>`.
4. **Abrir `TicketRepository.java`**: Añade la consulta `@Query` con el `JOIN t.tags`.
5. **Abrir `TicketService.java`**:
   - Declara el campo `private final TagRepository tagRepository;` en la parte superior junto a los otros repositorios.
   - Añade los dos métodos `addTagsToTicket` y `getTicketsByTag`.
6. **Abrir `TicketController.java`**: Añade los dos métodos anotados con `@PostMapping("/{id}/tags")` y `@GetMapping("/por-tag")`.
7. **Compilar y Verificar**: Ejecuta en la terminal `./gradlew compileJava` para verificar que compila sin errores.

---

### 4. ¿Por qué se hace así? (Explicación para Defensa)
1. **¿Por qué `Set<Tag>` en lugar de `List<Tag>`?**
   - En relaciones `@ManyToMany`, si utilizas `List`, Hibernate no puede verificar eficientemente la unicidad de elementos y corre el riesgo de lanzar `MultipleBagFetchException` cuando se hacen fetch joins. Un `Set` modela matemáticamente un conjunto sin elementos duplicados y optimiza las operaciones de inserción y borrado en la tabla intermedia.
2. **¿Por qué `cascade = {CascadeType.PERSIST, CascadeType.MERGE}` y NO `CascadeType.REMOVE`?**
   - Si se eliminara un ticket, **nunca** queremos que se borre la etiqueta general de la base de datos, ya que otros tickets podrían estar usando esa misma etiqueta (ej. `URGENTE`).
3. **¿Por qué normalizar con `.trim().toUpperCase()`?**
   - Evita la fragmentación de datos. Si un técnico escribe `"vpn"` y otro escribe `" VPN "`, el sistema crearía dos registros distintos en la base de datos si no se normalizan. Al pasar a mayúsculas y limpiar espacios, ambos apuntan a la misma fila `VPN`.
4. **¿Por qué `JOIN t.tags tag` con `DISTINCT` en la consulta JPQL?**
   - Porque un join a una colección puede multiplicar las filas devueltas en SQL. `DISTINCT` asegura que el resultado contenga tickets únicos sin duplicados.
5. **Seguridad RBAC granular**:
   - Se valida tanto el rol global (`ROLE_ADMIN`) como la propiedad del recurso (`isAssignedTech`), impidiendo que un técnico modifique tickets asignados a sus compañeros.

---

# Ejercicio 2: Reanudación y Reapertura de Tickets Resueltos

### Enfoque Técnico
Transiciones de estado condicionales, cálculo de ventanas de tiempo con `java.time.temporal.ChronoUnit`, consistencia en el historial de auditoría y excepciones HTTP 400 vs 409.

---

### 1. ¿Dónde se edita?
| Capa | Archivo / Ruta | Tipo de Operación |
| :--- | :--- | :--- |
| **Entidad** | `src/main/java/com/spring/entity/Ticket.java` | **Modificar archivo existente** (añadir `reaperturasCount`) |
| **DTO Request** | `src/main/java/com/spring/dto/request/ReopenTicketRequest.java` | **Crear archivo nuevo** |
| **Repositorio** | `src/main/java/com/spring/repository/TicketRepository.java` | **Modificar archivo existente** |
| **Servicio** | `src/main/java/com/spring/service/TicketService.java` | **Modificar archivo existente** |
| **Controlador** | `src/main/java/com/spring/controller/TicketController.java` | **Modificar archivo existente** |

---

### 2. ¿Qué se pone?

#### A) Modificación en `Ticket.java`
**Ruta:** `src/main/java/com/spring/entity/Ticket.java`
Añadir el atributo contador de reaperturas:
```java
    // Contador de reaperturas acumuladas para este ticket
    @Builder.Default
    @Column(name = "reaperturas_count", nullable = false)
    private Integer reaperturasCount = 0;
```

#### B) Nuevo DTO: `ReopenTicketRequest.java`
**Ruta:** `src/main/java/com/spring/dto/request/ReopenTicketRequest.java`
```java
package com.spring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReopenTicketRequest {

    @NotBlank(message = "El motivo de la reapertura es obligatorio")
    @Size(max = 250, message = "El motivo no puede exceder los 250 caracteres")
    private String motivo;
}
```

#### C) Modificación en `TicketRepository.java`
**Ruta:** `src/main/java/com/spring/repository/TicketRepository.java`
Añadir consulta de tickets con reaperturas:
```java
    List<Ticket> findByReaperturasCountGreaterThan(int minCount);
```

#### D) Métodos a añadir en `TicketService.java`
**Ruta:** `src/main/java/com/spring/service/TicketService.java`
```java
import com.spring.dto.request.ReopenTicketRequest;
import java.time.temporal.ChronoUnit;

    /**
     * Reabre un ticket en estado RESUELTO dentro de las 48 horas posteriores.
     * Solo permitido para el solicitante (requester) original.
     */
    @Transactional
    public TicketResponse reopenTicket(Long id, ReopenTicketRequest request, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + id));

        // 1. Solo el USER creador (requester) puede ejecutar esta acción
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para reabrir un ticket");
        }
        String currentUserEmail = authentication.getName();
        if (ticket.getRequester() == null || !currentUserEmail.equalsIgnoreCase(ticket.getRequester().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el usuario solicitante que creó el ticket puede solicitar su reapertura");
        }

        // 2. El ticket DEBE estar en estado RESOLVED (Resuelta)
        if (ticket.getState() != TicketState.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Solo se pueden reabrir tickets en estado RESUELTA. Estado actual: " + ticket.getState());
        }

        // 3. Restricción temporal: máximo 48 horas posteriores a la fecha de resolución
        LocalDateTime resolutionDate = ticket.getResolutionDate();
        if (resolutionDate == null) {
            // Si por alguna inconsistencia de datos no tuviera fecha, se usa la fecha actual
            resolutionDate = ticket.getCreateDate();
        }

        long hoursSinceResolution = ChronoUnit.HOURS.between(resolutionDate, LocalDateTime.now());
        if (hoursSinceResolution > 48) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Han transcurrido " + hoursSinceResolution + " horas desde la resolución. Superado el límite de 48 horas, debe crearse un nuevo ticket.");
        }

        // 4. Transición de estado: vuelve a EN_PROCESO
        TicketState previousState = ticket.getState();
        ticket.setState(TicketState.IN_PROCESS);

        // 5. Incrementar contador de reaperturas
        int currentCount = (ticket.getReaperturasCount() != null) ? ticket.getReaperturasCount() : 0;
        ticket.setReaperturasCount(currentCount + 1);

        // 6. Registrar en el historial de cambios con el motivo obligatorio
        User requesterUser = ticket.getRequester();
        ticketHistoryService.recordChange(ticket, previousState, TicketState.IN_PROCESS, requesterUser);

        // 7. Guardar el ticket actualizado
        Ticket savedTicket = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(savedTicket);
    }

    /**
     * Lista los tickets que tienen al menos una reapertura. Solo ADMIN.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getReopenedTickets() {
        return ticketRepository.findByReaperturasCountGreaterThan(0).stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }
```

#### E) Endpoints a añadir en `TicketController.java`
**Ruta:** `src/main/java/com/spring/controller/TicketController.java`
```java
    @Operation(summary = "Reabrir ticket resuelto", description = "Permite al solicitante original reabrir un ticket resuelto dentro de las 48 horas posteriores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket reabierto exitosamente pasando a EN_PROCESO"),
            @ApiResponse(responseCode = "400", description = "Ticket fuera del límite de 48h o no está en estado RESUELTA"),
            @ApiResponse(responseCode = "403", description = "Solo el solicitante original puede reabrir el ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PostMapping("/{id}/reabrir")
    public ResponseEntity<TicketResponse> reopenTicket(
            @PathVariable Long id,
            @Valid @RequestBody ReopenTicketRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.reopenTicket(id, request, authentication));
    }

    @Operation(summary = "Listar tickets reabiertos", description = "Exclusivo de ADMIN. Retorna tickets con reaperturasCount > 0.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reabiertos")
    public ResponseEntity<List<TicketResponse>> getReopenedTickets() {
        return ResponseEntity.ok(ticketService.getReopenedTickets());
    }
```

---

### 3. ¿Cómo se añade? (Paso a Paso)
1. **Editar `Ticket.java`**: Abre `src/main/java/com/spring/entity/Ticket.java` y añade el campo `reaperturasCount` con `@Column(name = "reaperturas_count", nullable = false)`.
2. **Crear `ReopenTicketRequest.java`**: Crea el archivo en el paquete `com.spring.dto.request` con las validaciones `@NotBlank` y `@Size(max = 250)`.
3. **Modificar `TicketRepository.java`**: Añade la firma `List<Ticket> findByReaperturasCountGreaterThan(int minCount);`.
4. **Modificar `TicketService.java`**: Añade los métodos `reopenTicket` y `getReopenedTickets`. Asegúrate de importar `java.time.temporal.ChronoUnit`.
5. **Modificar `TicketController.java`**: Añade los endpoints `POST /{id}/reabrir` y `GET /reabiertos` (este último protegido con `@PreAuthorize("hasRole('ADMIN')")`).

---

### 4. ¿Por qué se hace así? (Explicación para Defensa)
1. **Uso de `ChronoUnit.HOURS.between`**:
   - La API de fechas de Java 8 (`java.time`) es inmutable y segura ante concurrencia. `ChronoUnit.HOURS.between(start, end)` calcula la diferencia entera de horas completas de forma precisa sin depender de timestamps de milisegundos propensos a errores de zona horaria.
2. **Acuerdo de Nivel de Servicio (SLA) de 48 Horas**:
   - En marcos de trabajo como ITIL, una incidencia resuelta tiene un periodo de gracia para que el usuario confirme si la solución realmente funcionó. Si transcurren más de 48 horas, se considera un nuevo incidente y no la continuación de la falla anterior para no desvirtuar las métricas de resolución técnica.
3. **Auditoría e Integridad Transaccional**:
   - Se utiliza `@Transactional`. Si la grabación en el historial de `TicketHistory` falla, toda la reapertura hace rollback. De esta forma, nunca habrá un ticket reabierto sin su correspondiente justificación y registro de auditoría.
4. **Control de Propiedad**:
   - `currentUserEmail.equalsIgnoreCase(ticket.getRequester().getEmail())` garantiza que un técnico o un tercero no pueda falsificar la inconformidad del usuario reportante.

---

# Ejercicio 3: Transferencia de Tickets entre Departamentos / Áreas

### Enfoque Técnico
Enumeraciones dinámicas, desvinculación segura de llaves foráneas (`assignedTechnician = null`), retroceso de máquina de estados a `OPENED` y auditoría de comentarios automáticos generados por el sistema.

---

### 1. ¿Dónde se edita?
| Capa | Archivo / Ruta | Tipo de Operación |
| :--- | :--- | :--- |
| **DTO Request** | `src/main/java/com/spring/dto/request/TransferTicketRequest.java` | **Crear archivo nuevo** |
| **Servicio** | `src/main/java/com/spring/service/TicketService.java` | **Modificar archivo existente** |
| **Controlador** | `src/main/java/com/spring/controller/TicketController.java` | **Modificar archivo existente** |

---

### 2. ¿Qué se pone?

#### A) Nuevo DTO: `TransferTicketRequest.java`
**Ruta:** `src/main/java/com/spring/dto/request/TransferTicketRequest.java`
```java
package com.spring.dto.request;

import com.spring.entity.enums.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTicketRequest {

    @NotNull(message = "La nueva categoría es obligatoria")
    private TicketCategory nuevaCategoria;

    @NotBlank(message = "La justificación de la transferencia es obligatoria")
    @Size(max = 500, message = "La justificación no puede exceder los 500 caracteres")
    private String justificacion;
}
```

#### B) Método a añadir en `TicketService.java`
**Ruta:** `src/main/java/com/spring/service/TicketService.java`
```java
import com.spring.dto.request.TransferTicketRequest;
import com.spring.entity.Comment;

    /**
     * Transfiere un ticket a una nueva categoría/área.
     * Desvincula al técnico asignado, retrocede el estado a OPENED y registra una nota en comentarios.
     * Permitido para ADMIN o el TECHNICIAN asignado actualmente.
     */
    @Transactional
    public TicketResponse transferTicket(Long id, TransferTicketRequest request, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + id));

        // 1. Validación de permisos: ADMIN o el TECHNICIAN asignado actualmente
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isCurrentTech = ticket.getTechnicianAssigned() != null &&
                ticket.getTechnicianAssigned().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdmin && !isCurrentTech) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Solo un administrador o el técnico asignado pueden transferir este ticket a otra área");
        }

        // 2. Validación de regla: La nueva categoría no debe ser idéntica a la actual (400 Bad Request)
        if (ticket.getCategory() == request.getNuevaCategoria()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La nueva categoría (" + request.getNuevaCategoria() + ") no puede ser idéntica a la categoría actual del ticket.");
        }

        TicketCategory oldCategory = ticket.getCategory();
        TicketState previousState = ticket.getState();
        User previousTech = ticket.getTechnicianAssigned();

        // 3. Reglas de Negocio al transferir:
        // a) Cambiar a la nueva categoría
        ticket.setCategory(request.getNuevaCategoria());

        // b) Desvincular al técnico actualmente asignado (tecnicoAsignado = null)
        ticket.setTechnicianAssigned(null);

        // c) Retroceder obligatoriamente el estado a OPENED (ABIERTA)
        ticket.setState(TicketState.OPENED);

        // 4. Obtener usuario que realiza la acción
        User actor = userRepository.findByEmail(authentication.getName())
                .orElse(previousTech != null ? previousTech : ticket.getRequester());

        // 5. Registrar nota automática del sistema en los comentarios
        String noteContent = String.format(
                "[SISTEMA - TRANSFERENCIA DE ÁREA] El ticket fue transferido de %s a %s. " +
                "Técnico previo desvinculado: %s. Motivo: %s",
                oldCategory,
                request.getNuevaCategoria(),
                (previousTech != null ? previousTech.getName() : "Sin asignar"),
                request.getJustificacion()
        );

        Comment autoComment = Comment.builder()
                .ticket(ticket)
                .author(actor)
                .content(noteContent)
                .createdAt(LocalDateTime.now())
                .build();
        ticket.getComments().add(autoComment);

        // 6. Registrar en el historial de cambios de estado (OPENED)
        ticketHistoryService.recordChange(ticket, previousState, TicketState.OPENED, actor);

        // 7. Persistir y retornar DTO
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(saved);
    }
```

#### C) Endpoint a añadir en `TicketController.java`
**Ruta:** `src/main/java/com/spring/controller/TicketController.java`
```java
    @Operation(summary = "Transferir ticket entre departamentos/categorías", 
               description = "Cambia la categoría del ticket, desvincula al técnico y retrocede el estado a OPENED. Permitido para ADMIN o TECHNICIAN asignado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket transferido exitosamente"),
            @ApiResponse(responseCode = "400", description = "La categoría nueva es idéntica a la actual o datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PutMapping("/{id}/transferir")
    public ResponseEntity<TicketResponse> transferTicket(
            @PathVariable Long id,
            @Valid @RequestBody TransferTicketRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.transferTicket(id, request, authentication));
    }
```

---

### 3. ¿Cómo se añade? (Paso a Paso)
1. **Crear `TransferTicketRequest.java`**: En `src/main/java/com/spring/dto/request/`, crea la clase con las anotaciones de validación `@NotNull` y `@NotBlank`.
2. **Modificar `TicketService.java`**: Añade el método `transferTicket` asegurándote de desvincular el técnico (`ticket.setTechnicianAssigned(null)`), cambiar el estado a `TicketState.OPENED` y añadir el comentario en la colección `ticket.getComments().add(...)`.
3. **Modificar `TicketController.java`**: Añade el endpoint `@PutMapping("/{id}/transferir")` inyectando el objeto `Authentication`.

---

### 4. ¿Por qué se hace así? (Explicación para Defensa)
1. **¿Por qué desvincular al técnico (`technicianAssigned = null`)?**
   - Si un ticket clasificado como `REDES` se transfiere a `HARDWARE`, el técnico de redes no tiene las competencias ni la responsabilidad para resolver el problema de hardware. Dejarlo asignado generaría métricas falsas y cuellos de botella.
2. **¿Por qué retroceder el estado a `OPENED`?**
   - Al entrar a un nuevo departamento, el ticket vuelve a la cola general de asignación. El coordinador del nuevo departamento debe poder revisarlo y asignárselo a un especialista adecuado de su equipo.
3. **Validación semántica (HTTP 400 Bad Request)**:
   - Transferir un ticket a la misma categoría actual es una operación sin sentido que saturaría el historial y los comentarios. Lanzar `400 Bad Request` previene peticiones erróneas o duplicadas desde el cliente.
4. **Trazabilidad y Transparencia**:
   - La inserción de un comentario del sistema junto con el historial permite que el usuario solicitante comprenda por qué su ticket cambió de área y qué técnico lo atendió previamente.

---

# Ejercicio 4: Bitácora de Registro de Tiempo Técnico (Worklog)

### Enfoque Técnico
Operaciones de agregación en Spring Data JPA con `SUM` y `COALESCE`, control granular de permisos (solo técnico asignado en `IN_PROCESS`) y validación de rangos numéricos con `@Min` y `@Max`.

---

### 1. ¿Dónde se edita?
| Capa | Archivo / Ruta | Tipo de Operación |
| :--- | :--- | :--- |
| **Entidad** | `src/main/java/com/spring/entity/TicketWorklog.java` | **Crear archivo nuevo** |
| **Repositorio**| `src/main/java/com/spring/repository/TicketWorklogRepository.java` | **Crear archivo nuevo** |
| **DTO Request**| `src/main/java/com/spring/dto/request/WorklogRequest.java` | **Crear archivo nuevo** |
| **DTO Response**| `src/main/java/com/spring/dto/response/WorklogResponse.java` | **Crear archivo nuevo** |
| **DTO Response**| `src/main/java/com/spring/dto/response/TicketTotalTimeResponse.java`| **Crear archivo nuevo** |
| **Servicio** | `src/main/java/com/spring/service/TicketService.java` | **Modificar archivo existente** |
| **Controlador**| `src/main/java/com/spring/controller/TicketController.java` | **Modificar archivo existente** |

---

### 2. ¿Qué se pone?

#### A) Nueva Entidad: `TicketWorklog.java`
**Ruta:** `src/main/java/com/spring/entity/TicketWorklog.java`
```java
package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_worklog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketWorklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "minutos_dedicados", nullable = false)
    private Integer minutosDedicados;

    @Column(name = "descripcion_actividad", nullable = false, columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }
}
```

#### B) Nuevo Repositorio: `TicketWorklogRepository.java`
**Ruta:** `src/main/java/com/spring/repository/TicketWorklogRepository.java`
```java
package com.spring.repository;

import com.spring.entity.TicketWorklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketWorklogRepository extends JpaRepository<TicketWorklog, Long> {

    List<TicketWorklog> findByTicketIdOrderByFechaRegistroDesc(Long ticketId);

    // Consulta de agregación SUM con COALESCE para evitar devolver nulo si no hay registros
    @Query("SELECT COALESCE(SUM(w.minutosDedicados), 0) FROM TicketWorklog w WHERE w.ticket.id = :ticketId")
    Integer sumMinutosByTicketId(@Param("ticketId") Long ticketId);
}
```

#### C) Nuevo DTO: `WorklogRequest.java`
**Ruta:** `src/main/java/com/spring/dto/request/WorklogRequest.java`
```java
package com.spring.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorklogRequest {

    @NotNull(message = "Los minutos dedicados son obligatorios")
    @Min(value = 5, message = "El tiempo mínimo por sesión de trabajo es de 5 minutos")
    @Max(value = 480, message = "El tiempo máximo por sesión de trabajo es de 480 minutos (8 horas)")
    private Integer minutosDedicados;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcionActividad;
}
```

#### D) Nuevo DTO: `WorklogResponse.java`
**Ruta:** `src/main/java/com/spring/dto/response/WorklogResponse.java`
```java
package com.spring.dto.response;

import com.spring.entity.TicketWorklog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorklogResponse {
    private Long id;
    private Integer minutosDedicados;
    private String descripcionActividad;
    private LocalDateTime fechaRegistro;
    private Long technicianId;
    private String technicianName;

    public static WorklogResponse fromEntity(TicketWorklog worklog) {
        return WorklogResponse.builder()
                .id(worklog.getId())
                .minutosDedicados(worklog.getMinutosDedicados())
                .descripcionActividad(worklog.getDescripcionActividad())
                .fechaRegistro(worklog.getFechaRegistro())
                .technicianId(worklog.getTechnician() != null ? worklog.getTechnician().getId() : null)
                .technicianName(worklog.getTechnician() != null ? worklog.getTechnician().getName() : "Desconocido")
                .build();
    }
}
```

#### E) Nuevo DTO: `TicketTotalTimeResponse.java`
**Ruta:** `src/main/java/com/spring/dto/response/TicketTotalTimeResponse.java`
```java
package com.spring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTotalTimeResponse {
    private Long ticketId;
    private Integer totalMinutos;
    private Double totalHoras;
    private List<WorklogResponse> registros;
}
```

#### F) Modificación en `TicketService.java`
**Ruta:** `src/main/java/com/spring/service/TicketService.java`
1. Inyectar `TicketWorklogRepository`:
```java
    private final TicketWorklogRepository ticketWorklogRepository;
```

2. Métodos de servicio:
```java
    /**
     * Registra un bloque de tiempo de trabajo técnico en el ticket.
     * Solo permitido para el técnico asignado y cuando el ticket está EN_PROCESO.
     */
    @Transactional
    public WorklogResponse addWorklog(Long ticketId, WorklogRequest request, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + ticketId));

        // 1. El ticket DEBE estar en estado EN_PROCESO
        if (ticket.getState() != TicketState.IN_PROCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Solo se puede registrar tiempo de trabajo en tickets que estén en estado EN_PROCESO. Estado actual: " + ticket.getState());
        }

        // 2. Solo el TECHNICIAN asignado puede registrar tiempo
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe estar autenticado");
        }

        String email = authentication.getName();
        if (ticket.getTechnicianAssigned() == null || !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Solo el técnico asignado a este ticket tiene autorización para registrar horas de trabajo");
        }

        User technician = ticket.getTechnicianAssigned();

        // 3. Crear y guardar el registro
        TicketWorklog worklog = TicketWorklog.builder()
                .ticket(ticket)
                .technician(technician)
                .minutosDedicados(request.getMinutosDedicados())
                .descripcionActividad(request.getDescripcionActividad())
                .build();

        TicketWorklog saved = ticketWorklogRepository.save(worklog);
        return WorklogResponse.fromEntity(saved);
    }

    /**
     * Consulta el tiempo total acumulado y el detalle de worklogs de un ticket.
     */
    @Transactional(readOnly = true)
    public TicketTotalTimeResponse getTicketTotalTime(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + ticketId);
        }

        Integer totalMinutos = ticketWorklogRepository.sumMinutosByTicketId(ticketId);
        List<WorklogResponse> registros = ticketWorklogRepository.findByTicketIdOrderByFechaRegistroDesc(ticketId).stream()
                .map(WorklogResponse::fromEntity)
                .collect(Collectors.toList());

        double totalHoras = Math.round((totalMinutos / 60.0) * 100.0) / 100.0;

        return TicketTotalTimeResponse.builder()
                .ticketId(ticketId)
                .totalMinutos(totalMinutos)
                .totalHoras(totalHoras)
                .registros(registros)
                .build();
    }
```

#### G) Endpoints en `TicketController.java`
**Ruta:** `src/main/java/com/spring/controller/TicketController.java`
```java
    @Operation(summary = "Registrar tiempo de trabajo técnico (Worklog)", 
               description = "Registra una sesión de trabajo de entre 5 y 480 minutos. Exclusivo para el técnico asignado en estado EN_PROCESO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Worklog registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Minutos fuera de rango (5 a 480) o descripción vacía"),
            @ApiResponse(responseCode = "403", description = "No es el técnico asignado"),
            @ApiResponse(responseCode = "409", description = "El ticket no está en estado EN_PROCESO")
    })
    @PostMapping("/{id}/worklogs")
    public ResponseEntity<WorklogResponse> addWorklog(
            @PathVariable Long id,
            @Valid @RequestBody WorklogRequest request,
            Authentication authentication) {
        return new ResponseEntity<>(ticketService.addWorklog(id, request, authentication), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar tiempo total acumulado del ticket", 
               description = "Retorna el total de minutos calculados mediante SUM y el desglose de intervenciones técnicas.")
    @GetMapping("/{id}/tiempo-total")
    public ResponseEntity<TicketTotalTimeResponse> getTotalTime(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketTotalTime(id));
    }
```

---

### 3. ¿Cómo se añade? (Paso a Paso)
1. **Crear `TicketWorklog.java`**: En `src/main/java/com/spring/entity/`, crea la entidad con sus relaciones `@ManyToOne` a `Ticket` y `User`.
2. **Crear `TicketWorklogRepository.java`**: En `src/main/java/com/spring/repository/`, implementa la query `@Query("SELECT COALESCE(SUM(w.minutosDedicados), 0) ...")`.
3. **Crear DTOs**: Crea `WorklogRequest`, `WorklogResponse` y `TicketTotalTimeResponse` en sus respectivas carpetas DTO.
4. **Modificar `TicketService.java`**: Inyecta `TicketWorklogRepository` y añade los métodos de registro y consulta de tiempo total.
5. **Modificar `TicketController.java`**: Añade los endpoints `POST /{id}/worklogs` y `GET /{id}/tiempo-total`.

---

### 4. ¿Por qué se hace así? (Explicación para Defensa)
1. **Operación `SUM` con `COALESCE` en Base de Datos**:
   - En lugar de traer cientos de filas a la memoria RAM de la JVM para sumarlas con Streams (`list.stream().mapToInt(...)`), se delega la agregación al motor PostgreSQL mediante `SUM(w.minutosDedicados)`. `COALESCE(SUM(...), 0)` asegura que si no hay registros, devuelva `0` en vez de `null`, evitando un potencial `NullPointerException`.
2. **Validación Temprana (Fail-Fast)**:
   - Con `@Min(5)` y `@Max(480)` en el DTO, Spring valida en el Controller mediante `@Valid` antes de iniciar la transacción de base de datos.
3. **Integridad del Estado `EN_PROCESO`**:
   - Un técnico no debe imputar tiempo sobre tickets que aún no ha iniciado (`ASSIGNED`) o que ya están concluidos (`RESOLVED` o `CLOSED`). Esto asegura la coherencia del ciclo de vida ITIL.

---

# Ejercicio 5: Plantillas de Solución Rápida (Canned Responses)

### Enfoque Técnico
CRUD auxiliar con filtros de pertenencia (aislamiento de datos por usuario autenticado), inyección automática en la colección de comentarios del ticket y restricciones de unicidad compuestas.

---

### 1. ¿Dónde se edita?
| Capa | Archivo / Ruta | Tipo de Operación |
| :--- | :--- | :--- |
| **Entidad** | `src/main/java/com/spring/entity/QuickResponse.java` | **Crear archivo nuevo** |
| **Repositorio**| `src/main/java/com/spring/repository/QuickResponseRepository.java` | **Crear archivo nuevo** |
| **DTO Request**| `src/main/java/com/spring/dto/request/QuickResponseRequest.java` | **Crear archivo nuevo** |
| **DTO Response**| `src/main/java/com/spring/dto/response/QuickResponseDTO.java` | **Crear archivo nuevo** |
| **Servicio** | `src/main/java/com/spring/service/QuickResponseService.java` | **Crear archivo nuevo** |
| **Controlador**| `src/main/java/com/spring/controller/QuickResponseController.java` | **Crear archivo nuevo** |
| **Controlador**| `src/main/java/com/spring/controller/TicketController.java` | **Modificar archivo existente** (para endpoint de aplicación rápida) |

---

### 2. ¿Qué se pone?

#### A) Nueva Entidad: `QuickResponse.java`
**Ruta:** `src/main/java/com/spring/entity/QuickResponse.java`
```java
package com.spring.entity;

import com.spring.entity.enums.TicketCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quick_response", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quick_response_user_titulo", columnNames = {"technician_id", "titulo"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;
}
```

#### B) Nuevo Repositorio: `QuickResponseRepository.java`
**Ruta:** `src/main/java/com/spring/repository/QuickResponseRepository.java`
```java
package com.spring.repository;

import com.spring.entity.QuickResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuickResponseRepository extends JpaRepository<QuickResponse, Long> {

    List<QuickResponse> findByTechnicianId(Long technicianId);

    Optional<QuickResponse> findByIdAndTechnicianId(Long id, Long technicianId);

    boolean existsByTechnicianIdAndTituloIgnoreCase(Long technicianId, String titulo);
}
```

#### C) Nuevo DTO: `QuickResponseRequest.java`
**Ruta:** `src/main/java/com/spring/dto/request/QuickResponseRequest.java`
```java
package com.spring.dto.request;

import com.spring.entity.enums.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickResponseRequest {

    @NotBlank(message = "El título de la plantilla es obligatorio")
    @Size(max = 120, message = "El título no puede superar los 120 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido de la plantilla es obligatorio")
    private String contenido;

    @NotNull(message = "La categoría asociada es obligatoria")
    private TicketCategory categoria;
}
```

#### D) Nuevo DTO: `QuickResponseDTO.java`
**Ruta:** `src/main/java/com/spring/dto/response/QuickResponseDTO.java`
```java
package com.spring.dto.response;

import com.spring.entity.QuickResponse;
import com.spring.entity.enums.TicketCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickResponseDTO {
    private Long id;
    private String titulo;
    private String contenido;
    private TicketCategory categoria;
    private Long technicianId;

    public static QuickResponseDTO fromEntity(QuickResponse entity) {
        return QuickResponseDTO.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .contenido(entity.getContenido())
                .categoria(entity.getCategoria())
                .technicianId(entity.getTechnician() != null ? entity.getTechnician().getId() : null)
                .build();
    }
}
```

#### E) Nuevo Servicio: `QuickResponseService.java`
**Ruta:** `src/main/java/com/spring/service/QuickResponseService.java`
```java
package com.spring.service;

import com.spring.dto.request.QuickResponseRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.dto.response.QuickResponseDTO;
import com.spring.entity.Comment;
import com.spring.entity.QuickResponse;
import com.spring.entity.Ticket;
import com.spring.entity.User;
import com.spring.entity.enums.TicketState;
import com.spring.repository.CommentRepository;
import com.spring.repository.QuickResponseRepository;
import com.spring.repository.TicketRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuickResponseService {

    private final QuickResponseRepository quickResponseRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    /**
     * Crea una plantilla para el técnico autenticado.
     */
    @Transactional
    public QuickResponseDTO createTemplate(QuickResponseRequest request, Authentication authentication) {
        User technician = getAuthenticatedUser(authentication);

        // Validación de unicidad de título por técnico
        if (quickResponseRepository.existsByTechnicianIdAndTituloIgnoreCase(technician.getId(), request.getTitulo().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Ya tienes una plantilla registrada con el título: " + request.getTitulo());
        }

        QuickResponse template = QuickResponse.builder()
                .titulo(request.getTitulo().trim())
                .contenido(request.getContenido().trim())
                .categoria(request.getCategoria())
                .technician(technician)
                .build();

        return QuickResponseDTO.fromEntity(quickResponseRepository.save(template));
    }

    /**
     * Lista las plantillas del técnico autenticado.
     */
    @Transactional(readOnly = true)
    public List<QuickResponseDTO> getMyTemplates(Authentication authentication) {
        User technician = getAuthenticatedUser(authentication);
        return quickResponseRepository.findByTechnicianId(technician.getId()).stream()
                .map(QuickResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Aplica la plantilla publicándola como comentario en el ticket.
     * Valida pertenencia de la plantilla, estado EN_PROCESO del ticket y asignación del técnico.
     */
    @Transactional
    public CommentResponse applyTemplateToTicket(Long ticketId, Long templateId, Authentication authentication) {
        User technician = getAuthenticatedUser(authentication);

        // 1. Validar ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + ticketId));

        // El ticket debe estar en estado EN_PROCESO
        if (ticket.getState() != TicketState.IN_PROCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Solo se pueden aplicar plantillas a tickets que estén en estado EN_PROCESO. Estado actual: " + ticket.getState());
        }

        // El técnico autenticado DEBE ser el técnico asignado a dicho ticket
        if (ticket.getTechnicianAssigned() == null || 
                !technician.getId().equals(ticket.getTechnicianAssigned().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "No puedes aplicar respuestas rápidas a un ticket que no tienes asignado.");
        }

        // 2. Validar que la plantilla pertenezca al técnico autenticado
        QuickResponse template = quickResponseRepository.findByIdAndTechnicianId(templateId, technician.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "La plantilla no existe o no te pertenece."));

        // 3. Crear el comentario formal con el contenido de la plantilla
        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(technician)
                .content(template.getContenido())
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.fromEntity(savedComment);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para realizar esta operación");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario autenticado no encontrado en base de datos"));
    }
}
```

#### F) Nuevo Controlador: `QuickResponseController.java`
**Ruta:** `src/main/java/com/spring/controller/QuickResponseController.java`
```java
package com.spring.controller;

import com.spring.dto.request.QuickResponseRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.dto.response.QuickResponseDTO;
import com.spring.service.QuickResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plantillas Técnicas (Quick Responses)", description = "Gestión de plantillas predefinidas para técnicos de soporte")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuickResponseController {

    private final QuickResponseService quickResponseService;

    @Operation(summary = "Crear plantilla de respuesta rápida", description = "Crea una nueva plantilla para el técnico autenticado.")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/tecnicos/plantillas")
    public ResponseEntity<QuickResponseDTO> createTemplate(
            @Valid @RequestBody QuickResponseRequest request,
            Authentication authentication) {
        return new ResponseEntity<>(quickResponseService.createTemplate(request, authentication), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar mis plantillas", description = "Lista exclusivamente las plantillas creadas por el técnico autenticado.")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @GetMapping("/tecnicos/plantillas")
    public ResponseEntity<List<QuickResponseDTO>> getMyTemplates(Authentication authentication) {
        return ResponseEntity.ok(quickResponseService.getMyTemplates(authentication));
    }

    @Operation(summary = "Aplicar plantilla rápida a ticket", description = "Inserta el contenido de la plantilla directamente como comentario en el ticket.")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/tickets/{id}/aplicar-plantilla/{plantillaId}")
    public ResponseEntity<CommentResponse> applyTemplate(
            @PathVariable Long id,
            @PathVariable Long plantillaId,
            Authentication authentication) {
        return new ResponseEntity<>(quickResponseService.applyTemplateToTicket(id, plantillaId, authentication), HttpStatus.CREATED);
    }
}
```

---

### 3. ¿Cómo se añade? (Paso a Paso)
1. **Crear `QuickResponse.java`**: En `src/main/java/com/spring/entity/`, declara la entidad con su `@UniqueConstraint` para que no se repitan títulos por técnico.
2. **Crear `QuickResponseRepository.java`**: En `src/main/java/com/spring/repository/`, implementa los métodos de búsqueda por `technicianId`.
3. **Crear DTOs**: Añade `QuickResponseRequest` y `QuickResponseDTO`.
4. **Crear `QuickResponseService.java`**: Inyecta los repositorios necesarios y desarrolla la lógica de creación y aplicación.
5. **Crear `QuickResponseController.java`**: Expone las rutas `/api/tecnicos/plantillas` y `/api/tickets/{id}/aplicar-plantilla/{plantillaId}`.
6. **Compilar**: Ejecuta `./gradlew compileJava` para verificar compilación exitosa.

---

### 4. ¿Por qué se hace así? (Explicación para Defensa)
1. **Aislamiento Multi-Inquilino por Usuario**:
   - Cada técnico gestiona su propio catálogo. Si el técnico A crea una plantilla *"Reinicio de Módem"*, el técnico B también puede crear una llamada *"Reinicio de Módem"* con sus propias palabras. Por eso la restricción `UNIQUE` es compuesta: `(technician_id, titulo)`.
2. **Validación Cruzada de Pertenencia**:
   - `findByIdAndTechnicianId(templateId, technician.getId())` evita vulnerabilidades de tipo IDOR (Insecure Direct Object Reference): un técnico malicioso no puede adivinar el ID de la plantilla de otro técnico y aplicarla.
3. **Reutilización del Sistema de Comentarios**:
   - Al aplicar la plantilla, se crea un `Comment` estándar en la base de datos. De esta forma, el frontend muestra la respuesta rápida exactamente igual que cualquier otro comentario, sin necesidad de alterar los componentes de visualización existentes.

---

# Resumen Rápido de Comandos y Pruebas con cURL

### 1. Compilación y Construcción del Proyecto
```powershell
# En Windows (PowerShell)
.\gradlew.bat compileJava

# Ejecutar el backend
.\gradlew.bat bootRun
```

### 2. Probar Ejercicio 1 (Tags)
```bash
# Asociar tags a ticket #1
curl -X POST http://localhost:8050/api/tickets/1/tags \
  -H "Authorization: Bearer <TOKEN_TECNICO>" \
  -H "Content-Type: application/json" \
  -d '["URGENTE", "VPN", "REMOTO"]'

# Filtrar tickets por tag
curl -X GET "http://localhost:8050/api/tickets/por-tag?nombre=VPN" \
  -H "Authorization: Bearer <TOKEN>"
```

### 3. Probar Ejercicio 2 (Reapertura de Ticket)
```bash
# Reabrir ticket resuelto
curl -X POST http://localhost:8050/api/tickets/1/reabrir \
  -H "Authorization: Bearer <TOKEN_SOLICITANTE>" \
  -H "Content-Type: application/json" \
  -d '{"motivo": "El problema de red persiste tras reiniciar el equipo"}'

# Consultar reabiertos (solo Admin)
curl -X GET http://localhost:8050/api/tickets/reabiertos \
  -H "Authorization: Bearer <TOKEN_ADMIN>"
```

### 4. Probar Ejercicio 3 (Transferencia)
```bash
# Transferir ticket a SOFTWARE
curl -X PUT http://localhost:8050/api/tickets/1/transferir \
  -H "Authorization: Bearer <TOKEN_TECNICO>" \
  -H "Content-Type: application/json" \
  -d '{"nuevaCategoria": "SOFTWARE", "justificacion": "No es fallo de tarjeta de red, sino de configuración de proxy en Windows"}'
```

### 5. Probar Ejercicio 4 (Worklog de Tiempo)
```bash
# Registrar 45 minutos de trabajo
curl -X POST http://localhost:8050/api/tickets/1/worklogs \
  -H "Authorization: Bearer <TOKEN_TECNICO>" \
  -H "Content-Type: application/json" \
  -d '{"minutosDedicados": 45, "descripcionActividad": "Actualización de controladores de red y diagnóstico de conexión Wi-Fi"}'

# Consultar tiempo total acumulado
curl -X GET http://localhost:8050/api/tickets/1/tiempo-total \
  -H "Authorization: Bearer <TOKEN>"
```

### 6. Probar Ejercicio 5 (Plantillas Rápidas)
```bash
# Crear plantilla
curl -X POST http://localhost:8050/api/tecnicos/plantillas \
  -H "Authorization: Bearer <TOKEN_TECNICO>" \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Limpieza de Caché DNS", "contenido": "Por favor abre la terminal CMD y ejecuta ipconfig /flushdns, luego reinicia el navegador.", "categoria": "NETWORK"}'

# Aplicar plantilla #1 al ticket #1
curl -X POST http://localhost:8050/api/tickets/1/aplicar-plantilla/1 \
  -H "Authorization: Bearer <TOKEN_TECNICO>"
```
