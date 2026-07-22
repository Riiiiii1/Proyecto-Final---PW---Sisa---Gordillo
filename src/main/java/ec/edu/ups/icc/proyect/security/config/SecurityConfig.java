package ec.edu.ups.icc.proyect.security.config;

import ec.edu.ups.icc.proyect.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/*
 * Configuración central de Spring Security.
 *
 * - Deshabilita el login por formulario y HTTP Basic (usamos JWT).
 * - Define qué rutas son públicas y cuáles requieren autenticación/rol.
 * - Registra el filtro JWT antes del filtro estándar de usuario/contraseña.
 * - Habilita @PreAuthorize a nivel de método (prePostEnabled = true),
 *   igual que en la práctica 12, para poder combinar reglas globales
 *   (aquí) con reglas finas por endpoint (en los controllers).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsServiceWrapper customUserDetailsService; // ver nota abajo
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Públicos: auth, docs, swagger, actuator health
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // Endpoints solo ADMIN (gestión de usuarios/roles/categorías)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Reportes: propietario o ADMIN -> control fino se hace con @PreAuthorize
                        // en el controller (necesita comparar el eventId/registrationId), aquí
                        // solo exigimos autenticación como primera barrera.
                        .requestMatchers("/api/reports/**").authenticated()

                        // Eventos: lectura pública opcional según negocio; de momento autenticado
                        .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()
                        .requestMatchers("/api/events/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Resto: cualquier usuario autenticado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}