package com.spring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusDeskOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("CampusDESK API — TechNova Solutions")
                        .description("### Plataforma Integral de Gestión de Incidencias Tecnológicas\n\n"
                                + "API REST desarrollada para la empresa ficticia **TechNova Solutions**.\n\n"
                                + "**Características del Sistema:**\n"
                                + "* 🔑 **Seguridad**: Autenticación mediante JWT y contraseñas cifradas con BCrypt.\n"
                                + "* 👥 **Control de Acceso (RBAC)**: Roles `ADMIN`, `TECHNICIAN`, `USER` con aislamiento de datos.\n"
                                + "* 🔄 **Ciclo de Vida Controlado**: `ABIERTA` ➔ `ASIGNADA` ➔ `EN_PROCESO` ➔ `RESUELTA` ➔ `CERRADA`.\n"
                                + "* 📜 **Auditoría e Historial**: Registro consistente de todas las transiciones.\n"
                                + "* 💬 **Comentarios**: Diálogo cronológico en tickets activos.\n"
                                + "* 📊 **Dashboard**: Métricas en tiempo real sincronizadas con PostgreSQL.\n\n"
                                + "--- \n"
                                + "*Para probar endpoints protegidos, primero haga login en `/api/auth/login`, copie el token y use el botón verde **Authorize** arriba a la derecha.*")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo TechNova Solutions")
                                .email("soporte@technova.com")
                                .url("https://technova.solutions"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8050").description("Servidor Local de Desarrollo (Port 8050)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese su token JWT sin el prefijo 'Bearer '. Ejemplo: `eyJhbGciOiJIUzI1NiIsInR5cCI6Ik...`")));
    }
}
