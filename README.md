# 🏢 CampusDESK — Plataforma de Gestión de Incidencias Tecnológicas

> **Proyecto empresarial desarrollado para TechNova Solutions**  
> Solución centralizada para la gestión, seguimiento y resolución del ciclo de vida de incidencias tecnológicas corporativas.

---

## 👥 Integrantes del Equipo de Desarrollo

* **Diego Parra**
* **Carlos Lindarte**
* **Santiago Pabón**

---

## 📌 Descripción del Proyecto

**CampusDESK** es una aplicación web empresarial diseñada para optimizar los canales de soporte técnico de **TechNova Solutions** (más de 200 colaboradores). Reemplaza la gestión informal por correo y mensajería con una plataforma estructurada que garantiza:

* Trazabilidad completa y ciclo de vida inmutable de tickets.
* Asignación organizada y controlada de técnicos por parte de administradores.
* Historial de auditoría cronológico registrado automáticamente por evento.
* Control de acceso basado en roles (**RBAC**) con aislamiento estricto de datos.
* Dashboard con métricas agregadas en tiempo real respaldadas en PostgreSQL.

---

## 🛠️ Tecnologías Utilizadas

### Backend
* **Java 21** con **Spring Boot 4.1.1**
* **Spring Data JPA / Hibernate** (Persistencia y mapeo relacional)
* **Spring Security** (Autenticación y autorización basada en métodos y roles)
* **JWT (JSON Web Tokens)** con algoritmo HMAC-SHA256
* **BCrypt Password Encoder** (Cifrado unidireccional de contraseñas)
* **Spring Validation** (Validación de DTOs con Bean Validation)
* **Springdoc OpenAPI 3.1.0 (Swagger UI)** (Documentación interactiva con soporte BearerAuth)
* **PostgreSQL 16** (Base de datos relacional)
* **Gradle 9.7** (Gestor de dependencias y construcción)

### Frontend
* **HTML5 semántico**
* **CSS3 moderno** (Diseño responsivo, CSS Custom Properties / Variables, Glassmorphism)
* **JavaScript Vanilla (ES6+ Modules)** (Sin frameworks: React, Angular, Vue, Bootstrap o Tailwind prohibidos según requerimientos)
* **Fetch API & Async/Await** para consumo de la API REST

---

## 🏛️ Arquitectura del Sistema

El proyecto implementa una arquitectura por capas desacoplada con separación estricta de responsabilidades:

```
CampusDESK/
├── src/main/java/com/spring/
│   ├── config/          # Configuración OpenAPI / Swagger
│   ├── controller/      # Controladores REST HTTP (Endpoints públicos y protegidos)
│   ├── dto/             # Data Transfer Objects (Request y Response con validaciones)
│   ├── entity/          # Entidades persistentes JPA (User, Role, Ticket, Comment, History)
│   ├── exception/       # GlobalExceptionHandler centralizado
│   ├── repository/      # Interfaces Spring Data JPA para acceso a PostgreSQL
│   ├── security/        # Filtros JWT, CustomUserDetailsService y SecurityConfig
│   └── service/         # Lógica y reglas de negocio, transaccionalidad y validaciones
├── src/main/resources/
│   └── application.properties # Configuración con variables de entorno parametrizadas
├── Fronted/             # Cliente Web desacoplado en Vanilla JS
│   ├── css/             # Hojas de estilo modulares (variables, base, components, pages)
│   ├── js/              # Módulos JS (config, auth, api, ui y controladores de vista)
│   ├── index.html       # Acceso (Login y Registro)
│   ├── dashboard.html   # Métricas y resumen por rol
│   ├── tickets.html     # Listado, creación y filtrado combinado (estado, prioridad, categoría)
│   ├── ticket-detail.html # Detalle, transiciones, comentarios e historial
│   └── admin.html       # Directorio de personal y usuarios (Solo ADMIN)
├── scripts/             # Scripts SQL organizados de base de datos
│   ├── 01-schema.sql    # DDL: Creación de tablas, claves foráneas e índices
│   ├── 02-seed-data.sql # DML: Roles y cuentas iniciales preconfiguradas
│   ├── 03-sample-tickets.sql # DML: Incidencias de prueba en todos los estados
│   └── README.md        # Documentación de base de datos
├── docker-compose.yml   # Contenedor PostgreSQL 17 + pgAdmin 4
├── .env.example         # Plantilla de variables de entorno
└── build.gradle         # Dependencias y configuración Gradle
```

---

## 🔐 Modelo de Seguridad y Roles (RBAC)

El sistema maneja 3 roles con privilegios claramente delimitados:

| Rol | Privilegios y Alcance |
| :--- | :--- |
| **`ADMIN`** | Consulta todos los tickets y usuarios. Asigna y reasigna técnicos. Visualiza estadísticas globales y el historial completo de cualquier solicitud. |
| **`TECHNICIAN`** | Consulta únicamente los tickets asignados a su cuenta. Gestiona el ciclo de vida (`ASIGNADA` ➔ `EN_PROCESO` ➔ `RESUELTA`). Añade comentarios de soporte en sus incidencias. |
| **`USER`** | Crea incidencias. Consulta y modifica **únicamente** sus propias solicitudes mientras permanezcan abiertas y sin asignar. Confirma el cierre (`CERRADA`) una vez resuelta. |

> [!IMPORTANT]
> **Política de Registro:** Al registrarse públicamente en la plataforma, el sistema asigna **automáticamente y de forma obligatoria el rol `USER`**. Los roles de `ADMIN` y `TECHNICIAN` deben ser provisionados desde la base de datos para evitar elevación no autorizada de privilegios.

---

## 🔄 Flujo del Ciclo de Vida de los Tickets

Todo ticket sigue una máquina de estados obligatoria y validada a nivel de Backend:

$$\text{ABIERTA} \xrightarrow{\text{Asignación (ADMIN)}} \text{ASIGNADA} \xrightarrow{\text{Iniciar (TÉCNICO)}} \text{EN\_PROCESO} \xrightarrow{\text{Resolver (TÉCNICO)}} \text{RESUELTA} \xrightarrow{\text{Cierre (SOLICITANTE)}} \text{CERRADA}$$

* **ABIERTA:** Estado inicial automático al crear un ticket. Solo el solicitante puede editarlo si no tiene técnico.
* **ASIGNADA:** Se alcanza cuando un Administrador asigna un técnico (`PATCH /api/tickets/{id}/assign`).
* **EN_PROCESO:** El técnico asignado comienza la atención (`PATCH /api/tickets/{id}/status?status=IN_PROCESS`).
* **RESUELTA:** El técnico termina el soporte. Requiere obligatoriamente haber estado en `EN_PROCESO` (rechaza con HTTP 409 en caso contrario). Registra fecha de resolución.
* **CERRADA:** Solo el solicitante dueño del ticket puede confirmar el cierre. Un ticket cerrado no permite modificaciones ni nuevos comentarios.

---

## 🚀 Requisitos Previos e Instalación

### 1. Requisitos
* **Java Development Kit (JDK) 21** o superior instalado (`java -version`).
* **Docker y Docker Compose** (o PostgreSQL 16/17 instalado localmente).
* Navegador web moderno (Chrome, Edge, Firefox).

---

### 2. Configurar y Levantar la Base de Datos (PostgreSQL)

Desde la raíz del proyecto, inicia el contenedor PostgreSQL configurado:

```bash
docker compose up -d
```

* **Puerto:** `5438`
* **Base de datos:** `campusDESK`
* **Usuario:** `dev_user`
* **Contraseña:** `DevTeamSecurePass2026!`

El contenedor monta la carpeta `./scripts` y ejecuta automáticamente los scripts en orden (`01-schema.sql` ➔ `02-seed-data.sql` ➔ `03-sample-tickets.sql`).

#### Carga Manual de Scripts (Opcional):
Si requieres ejecutar los scripts manualmente mediante `psql`:

```bash
# 1. Crear estructura (DDL)
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/01-schema.sql

# 2. Cargar roles y usuarios iniciales (Seed)
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/02-seed-data.sql

# 3. (Opcional) Cargar casos de prueba
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/03-sample-tickets.sql
```

---

### 3. Iniciar el Servidor Backend (Spring Boot)

En la raíz del proyecto, ejecuta el wrapper de Gradle:

```bash
# En Linux / macOS:
./gradlew bootRun

# En Windows:
gradlew.bat bootRun
```

El servidor iniciará en: **`http://localhost:8080`**.

---

### 4. Iniciar el Frontend

El frontend está desarrollado en Vanilla JS modular (ES Modules), por lo que debe servirse a través de un servidor HTTP local para permitir imports relativos y CORS:

**Opción A — Con extensión Live Server de VS Code:**
* Abre la carpeta `Fronted` en VS Code y haz clic en **"Go Live"** (Puerto por defecto: `5500` o `5501`).

**Opción B — Con Python:**
```bash
cd Fronted
python3 -m http.server 5500
```

Abre en el navegador: **`http://127.0.0.1:5500/index.html`**.

---

## 🔑 Credenciales de Prueba

| Perfil | Correo Electrónico | Contraseña | Rol Principal |
| :--- | :--- | :--- | :--- |
| **Super Administrador** | `admin@campusdesk.com` | `SuperAdmin2026!` | `ROLE_ADMIN` |
| **Técnico de Soporte** | `tecnico@campusdesk.com` | `Tech2026!` | `ROLE_TECHNICIAN` |
| **Usuario Solicitante** | `usuario@campusdesk.com` | `User2026!` | `ROLE_USER` |

---

## 📖 Documentación Swagger / OpenAPI

La API REST cuenta con documentación interactiva con soporte para pruebas protegidas mediante JWT:

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Especificación OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### ¿Cómo probar endpoints en Swagger?
1. Ejecuta `POST /api/auth/login` con credenciales válidas y copia el `token` resultante.
2. Haz clic en el botón verde **Authorize** (arriba a la derecha).
3. Pega el token y haz clic en **Authorize**. Todos los endpoints autenticados quedarán desbloqueados para pruebas.

---

## 🧪 Matriz de Casos de Prueba Obligatorios

| Código | Escenario | Resultado Esperado | Implementación |
| :--- | :--- | :--- | :---: |
| **CP-01** | Registrar usuario válido | Cuenta creada automáticamente con rol `USER` | ✅ Verificado |
| **CP-02** | Registrar correo duplicado | Rechazo con `HTTP 409 Conflict` | ✅ Verificado |
| **CP-03** | Iniciar sesión correctamente | Retorna JWT válido y datos de perfil | ✅ Verificado |
| **CP-04** | Acceder a endpoint protegido sin token | Rechazo con `HTTP 401 Unauthorized` | ✅ Verificado |
| **CP-05** | Crear ticket con datos válidos | Registrado en estado `ABIERTA` | ✅ Verificado |
| **CP-06** | Consultar ticket ajeno como USER | Rechazo con `HTTP 403 Forbidden` | ✅ Verificado |
| **CP-07** | Asignar técnico como ADMIN | Ticket transiciona a `ASIGNADA` | ✅ Verificado |
| **CP-08** | Intentar asignar como USER | Rechazo con `HTTP 403 Forbidden` | ✅ Verificado |
| **CP-09** | Modificar ticket ajeno como técnico | Rechazo con `HTTP 403 Forbidden` | ✅ Verificado |
| **CP-10** | Resolver ticket sin pasar por `EN_PROCESO` | Rechazo con `HTTP 409 Conflict` | ✅ Verificado |
| **CP-11** | Registrar cambio de estado | Registro histórico atómico en PostgreSQL | ✅ Verificado |
| **CP-12** | Cerrar ticket `RESUELTA` como solicitante | Estado transiciona a `CERRADA` | ✅ Verificado |
| **CP-13** | Comentar en ticket `CERRADA` | Rechazo con `HTTP 409 Conflict` | ✅ Verificado |
| **CP-14** | Consultar estadísticas | Coincidencia de conteos con PostgreSQL | ✅ Verificado |
| **CP-15** | Consumo API desde frontend autorizado | Comunicación fluida mediante CORS y Fetch | ✅ Verificado |
| **CP-16** | Utilizar JWT inválido o expirado | Rechazo con `HTTP 401 Unauthorized` | ✅ Verificado |

---

## ⚖️ Licencia

Proyecto desarrollado bajo fines académicos y empresariales para **TechNova Solutions** bajo licencia Apache 2.0.
