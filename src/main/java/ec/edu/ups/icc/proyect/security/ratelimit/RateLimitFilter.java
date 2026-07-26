package ec.edu.ups.icc.proyect.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = resolveClientIp(request);

        String key;
        int limit;
        long windowSeconds;

        if (path.contains("/auth/login")) {
            key = "rate-limit:login:" + ip;
            limit = properties.getLoginLimit();
            windowSeconds = properties.getLoginWindowSeconds();

        } else if (path.contains("/auth/register")) {
            key = "rate-limit:register:" + ip;
            limit = properties.getRegisterLimit();
            windowSeconds = properties.getRegisterWindowSeconds();

        } else if (path.contains("/reports/")) {
            String userKey = resolveAuthenticatedUserKey(request, ip);
            key = "rate-limit:reports:" + userKey;
            limit = properties.getReportsLimit();
            windowSeconds = properties.getReportsWindowSeconds();

        } else {
            String authHeader = request.getHeader("Authorization");
            boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

            if (isAuthenticated) {
                key = "rate-limit:auth:" + resolveAuthenticatedUserKey(request, ip);
                limit = properties.getAuthenticatedLimit();
                windowSeconds = properties.getAuthenticatedWindowSeconds();
            } else {
                key = "rate-limit:public:" + ip;
                limit = properties.getPublicLimit();
                windowSeconds = properties.getPublicWindowSeconds();
            }
        }

        boolean allowed = rateLimitService.isAllowed(key, limit, windowSeconds);

        if (!allowed) {
            long retryAfter = rateLimitService.getTtlSeconds(key);
            respondTooManyRequests(response, retryAfter);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Simplificación: usamos el hash del token Bearer como identificador de usuario,
     * sin decodificar el JWT dentro del filtro (evita acoplar este filtro a JwtUtil).
     * Si no hay token, cae de vuelta a la IP.
     */
    private String resolveAuthenticatedUserKey(HttpServletRequest request, String ip) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return String.valueOf(authHeader.hashCode());
        }
        return ip;
    }

    private void respondTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        String json = """
                {
                  "error": "Too Many Requests",
                  "errorCode": "RATE_LIMIT_EXCEEDED",
                  "message": "Ha superado el límite de solicitudes permitidas. Intente nuevamente en %d segundos.",
                  "status": 429
                }
                """.formatted(retryAfterSeconds);

        response.getWriter().write(json);
    }
}