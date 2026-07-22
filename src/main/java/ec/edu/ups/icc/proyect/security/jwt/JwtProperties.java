package ec.edu.ups.icc.proyect.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * Mapea las propiedades definidas en application.yaml bajo el prefijo "jwt":
 *
 * jwt:
 *   secret: ...
 *   access-expiration: ...   (en milisegundos)
 *   refresh-expiration: ...  (en milisegundos)
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long accessExpiration;
    private long refreshExpiration;
}