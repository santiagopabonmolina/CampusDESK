# CampusDESK - Frontend

Este es el frontend Vanilla JS para la plataforma CampusDESK.

## Características Técnicas
- **HTML5, CSS3, JavaScript Vanilla (ES6+)**
- Tema oscuro, glassmorphism, acentos morados (inspirado en Zyricon).
- Consumo de API REST usando `fetch` y `async/await`.
- Componentes reutilizables de UI (Toasts, Spinners, Modals, Badges).
- Sistema de rutas protegido basado en JWT.

## Requisitos Previos (Backend)
El backend debe estar corriendo y tener el puerto configurado en `8080` (o ajustarlo en `js/config.js`). El CORS del backend debe permitir el puerto donde sirvas este frontend (ej. `http://127.0.0.1:5500`).

## Cómo ejecutarlo
Para visualizar el frontend correctamente (sin errores de módulos de ES6 de CORS):
1. Abre esta carpeta (`Fronted`) en VS Code.
2. Usa la extensión **Live Server** (Click derecho en `index.html` > *Open with Live Server*).
3. Asegúrate de que el puerto del Live Server sea `5500` o `5501` (ya autorizados en el `application.properties` del backend generado).

## Cuentas de Prueba
No hay usuarios precargados en la DB vacía.
**Instrucciones de prueba:**
1. Crea un usuario desde la pantalla de Registro en `index.html`.
2. Por defecto, todo usuario creado por la web obtiene rol `USER`.
3. Para probar el rol `ADMIN` o `TECHNICIAN`, tendrás que modificar manualmente el registro del usuario creado en la base de datos PostgreSQL, cambiando la tabla intermedia `user_roles` para apuntar a un rol distinto.

## Configuración de la API
Si necesitas cambiar la URL base de la API, edita el archivo `js/config.js`:
```javascript
export const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api', // <-- Ajustar si es necesario
  // ...
};
```
