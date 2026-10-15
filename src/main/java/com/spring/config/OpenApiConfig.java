// Defines the package namespace containing application configuration beans
package com.spring.config;

// Imports Components model for defining reusable OpenAPI security schemas
import io.swagger.v3.oas.models.Components;
// Imports OpenAPI root object model representing the entire OpenAPI specification
import io.swagger.v3.oas.models.OpenAPI;
// Imports Contact model representing API developer contact metadata
import io.swagger.v3.oas.models.info.Contact;
// Imports Info model representing title, description, and version of API
import io.swagger.v3.oas.models.info.Info;
// Imports License model representing software license terms for API
import io.swagger.v3.oas.models.info.License;
// Imports SecurityRequirement model defining global security requirements
import io.swagger.v3.oas.models.security.SecurityRequirement;
// Imports SecurityScheme model configuring HTTP Bearer JWT security
import io.swagger.v3.oas.models.security.SecurityScheme;
// Imports Server model representing target deployment environment URLs
import io.swagger.v3.oas.models.servers.Server;
// Imports Spring Bean annotation declaring factory methods producing managed beans
import org.springframework.context.annotation.Bean;
// Imports Spring Configuration annotation marking class as bean definition source
import org.springframework.context.annotation.Configuration;

// Imports Java utility List collection interface for handling sequential lists
import java.util.List;

// Marks this class as Spring configuration defining Swagger OpenAPI documentation
@Configuration
// Declares OpenApiConfig setting up interactive Swagger documentation and JWT authorization
public class OpenApiConfig {

    /**
     * Configures Swagger OpenAPI documentation.
     */
    // Registers OpenAPI bean into Spring application context container
    @Bean
    // Builds and configures OpenAPI metadata, local servers, and JWT Bearer security scheme
    public OpenAPI campusDeskOpenAPI() {
        // Defines the security scheme identifier name used across Swagger UI
        final String securitySchemeName = "BearerAuth";

        // Instantiates and configures OpenAPI instance with info, servers, security requirements, and JWT scheme
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
                                + "---\n"
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
    // Closes campusDeskOpenAPI bean method
    }
// Closes OpenApiConfig class definition
}
