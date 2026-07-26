package ec.edu.ups.icc.proyect.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("API de Programación y Plataformas Web")
                .version("1.0.0")
                .description("""
                        Documentación interactiva de la API REST desarrollada en Spring Boot.
                        
                        Esta API incluye:
                        - autenticación con JWT
                        - autorización por roles
                        - validación de ownership
                        - paginación
                        - manejo global de errores
                        """);


        Server localServer = new Server()
                .url("/api")
                .description("Servidor local");


        SecurityScheme bearerScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el JWT generado en /auth/login");

        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme);


        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}