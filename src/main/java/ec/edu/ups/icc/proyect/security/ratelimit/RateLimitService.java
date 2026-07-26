package ec.edu.ups.icc.proyect.security.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Incrementa atómicamente el contador asociado a la clave.
     * Si es la primera vez que se usa esa clave (contador recién creado en 1),
     * le asigna un TTL igual a la ventana de tiempo de la regla.
     *
     * @return true si la petición debe permitirse, false si debe bloquearse (429).
     */
    public boolean isAllowed(String key, int limit, long windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return count != null && count <= limit;
    }

    /**
     * Segundos restantes hasta que expire la clave (para el header Retry-After).
     */
    public long getTtlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }
}