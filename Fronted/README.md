# CampusDESK - Frontend

Este es el frontend Vanilla JS para la plataforma **CampusDESK** de **TechNova Solutions**.

## Características Técnicas
- **HTML5, CSS3, JavaScript Vanilla (ES6+) con módulos nativos**.
- Tema oscuro refinado, glassmorphism con efectos ambientales y acentos morados/violetas modernos.
- Consumo de API REST usando `fetch` y `async/await`.
- Componentes reutilizables de UI (Toasts, Spinners, Modals elásticos, Badges con estados, Empty States interactivos).
- Sistema de rutas protegido basado en JWT.
- **Página de Presentación (Landing Page)** inspirada en arquitecturas tecnológicas modernas (GeForce SaaS).
- **Flujo de Verificación por Correo** de 6 dígitos para activación de cuentas nuevas.

## Estructura de Páginas
| Archivo | Propósito |
| :--- | :--- |
| **`landing.html`** | Página principal de presentación, capacidades del sistema, ciclo de vida y métricas en vivo. |
| **`index.html`** | Portal de autenticación: Inicio de sesión, Registro y Verificación de código de 6 dígitos. |
| **`dashboard.html`** | Tablero de control con métricas en tiempo real, KPIs y gráficos de barras por prioridad. |
| **`tickets.html`** | Listado de incidencias con filtros dinámicos por estado, prioridad y categoría, y creación de tickets. |
| **`ticket-detail.html`** | Detalle completo de incidencia, línea de tiempo de auditoría, cambio de estado y comentarios. |
| **`admin.html`** | Panel exclusivo de administradores para gestión de personal y asignación de roles. |

## Cómo ejecutarlo
Para visualizar el frontend correctamente (sin errores de módulos ES6 ni CORS):
1. Abre esta carpeta (`Fronted`) en VS Code o IntelliJ IDEA.
2. Usa la extensión **Live Server** (Click derecho en `landing.html` o `index.html` > *Open with Live Server*).
3. Puertos habituales autorizados en CORS: `5500`, `5501`, `63342`, `8000`, `5173`.

## Cuentas de Prueba
| Rol | Correo Electrónico | Contraseña |
| :--- | :--- | :--- |
| **ADMIN** | `admin@campusdesk.com` | `SuperAdmin2026!` |
| **TECHNICIAN** | `tecnico@campusdesk.com` | `Tech2026!` |
| **USER** | `usuario@campusdesk.com` | `User2026!` |

## Configuración de la API
Si necesitas cambiar la URL base de la API, edita el archivo `js/config.js`:
```javascript
export const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api',
  // ...
};
```
