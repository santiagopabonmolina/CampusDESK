## 5. Infraestructura y Base de Datos (Docker Compose)

El archivo [`docker-compose.yml`](docker-compose.yml) levanta:

1. **`dev_postgres` (PostgreSQL 17):**
   * **Puerto:** `5438:5432`
   * **Base de datos:** `campusDESK`
   * **Credenciales:** Usuario `dev_user`, Contraseña `DevTeamSecurePass2026!`
   * **Volumen de Scripts:** `./scripts:/docker-entrypoint-initdb.d:ro` (ejecuta automáticamente los scripts en [`scripts/`](scripts/) en el primer arranque).
2. **`dev_pgadmin` (pgAdmin 4):**
   * **Puerto:** `5055:80` (Interfaz gráfica para administrar PostgreSQL).
   * **Credenciales:** `team@devlocal.com` / `PgAdminTeamPass2026!`
