# 🗄️ Database Scripts — CampusDESK (TechNova Solutions)

Este directorio contiene los scripts SQL ordenados para inicializar, configurar y poblar la base de datos PostgreSQL de **CampusDESK**.

---

## 📁 Estructura de Archivos

| Archivo | Tipo | Descripción |
| :--- | :---: | :--- |
| **[`01-schema.sql`](01-schema.sql)** | DDL | Definición de tablas (`users`, `role`, `user_roles`, `ticket`, `comment`, `ticket_history`), claves foráneas, restricciones e índices. |
| **[`02-seed-data.sql`](02-seed-data.sql)** | DML | Catálogo oficial de roles (`ADMIN`, `TECHNICIAN`, `USER`) y usuarios iniciales precargados con contraseñas encriptadas con BCrypt. |
| **[`03-sample-tickets.sql`](03-sample-tickets.sql)** | DML *(Opcional)* | Incidencias de demostración que cubren los 5 estados del ciclo de vida (`OPENED`, `ASSIGNED`, `IN_PROCESS`, `RESOLVED`, `CLOSED`), incluyendo comentarios e historial atómico. |

---

## 🚀 Modos de Ejecución

### Opción 1: Ejecución Automática con Docker Compose (Recomendado)
El archivo [`docker-compose.yml`](../docker-compose.yml) monta este directorio dentro de PostgreSQL:
```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
  - ./scripts:/docker-entrypoint-initdb.d:ro
```
Al levantar el contenedor por primera vez (`docker compose up -d`), PostgreSQL ejecutará automáticamente los scripts en orden alfabético (`01-schema.sql` ➔ `02-seed-data.sql` ➔ `03-sample-tickets.sql`).

---

### Opción 2: Ejecución Manual con `psql` / Docker
Si la base de datos ya fue iniciada previamente o deseas reinicializarla manualmente:

```bash
# 1. Crear estructura (DDL)
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/01-schema.sql

# 2. Cargar roles y usuarios iniciales (Seed)
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/02-seed-data.sql

# 3. (Opcional) Cargar tickets de demostración
docker exec -i dev_postgres psql -U dev_user -d campusDESK < scripts/03-sample-tickets.sql
```

---

## 🔑 Cuentas Creadas por Defecto

| Rol | Correo Electrónico | Contraseña | Propósito |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@campusdesk.com` | `SuperAdmin2026!` | Asignación, reasignación, métricas globales y administración |
| **TECHNICIAN** | `tecnico@campusdesk.com` | `Tech2026!` | Gestión del flujo (`ASIGNADA` ➔ `EN_PROCESO` ➔ `RESUELTA`) y comentarios |
| **USER** | `usuario@campusdesk.com` | `User2026!` | Creación de solicitudes, edición en estado abierto y cierre final |
