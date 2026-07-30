package ec.edu.ups.icc.proyect.security.ratelimit;

import ec.edu.ups.icc.proyect.security.util.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    public RateLimitFilter(JwtUtil jwtUtil, RateLimitService rateLimitService, RateLimitProperties properties) {
        this.jwtUtil = jwtUtil;
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

    private String resolveAuthenticatedUserKey(HttpServletRequest request, String ip) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateAccessToken(token)) {
                    return jwtUtil.getEmailFromToken(token);
                }
            } catch (Exception ignored) {

            }
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