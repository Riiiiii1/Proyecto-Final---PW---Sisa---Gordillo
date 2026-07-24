package ec.edu.ups.icc.proyect.security.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret;
    private long expiration;
    private long refreshExpiration;
    private String issuer;
    private String tokenPrefix;
    private String headerName;
}