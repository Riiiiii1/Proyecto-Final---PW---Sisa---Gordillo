package ec.edu.ups.icc.proyect.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int loginLimit = 5;
    private int loginWindowSeconds = 60;

    private int registerLimit = 3;
    private int registerWindowSeconds = 3600;

    private int publicLimit = 60;
    private int publicWindowSeconds = 60;

    private int authenticatedLimit = 120;
    private int authenticatedWindowSeconds = 60;

    private int reportsLimit = 5;
    private int reportsWindowSeconds = 60;

    public int getLoginLimit() { return loginLimit; }
    public void setLoginLimit(int loginLimit) { this.loginLimit = loginLimit; }
    public int getLoginWindowSeconds() { return loginWindowSeconds; }
    public void setLoginWindowSeconds(int loginWindowSeconds) { this.loginWindowSeconds = loginWindowSeconds; }

    public int getRegisterLimit() { return registerLimit; }
    public void setRegisterLimit(int registerLimit) { this.registerLimit = registerLimit; }
    public int getRegisterWindowSeconds() { return registerWindowSeconds; }
    public void setRegisterWindowSeconds(int registerWindowSeconds) { this.registerWindowSeconds = registerWindowSeconds; }

    public int getPublicLimit() { return publicLimit; }
    public void setPublicLimit(int publicLimit) { this.publicLimit = publicLimit; }
    public int getPublicWindowSeconds() { return publicWindowSeconds; }
    public void setPublicWindowSeconds(int publicWindowSeconds) { this.publicWindowSeconds = publicWindowSeconds; }

    public int getAuthenticatedLimit() { return authenticatedLimit; }
    public void setAuthenticatedLimit(int authenticatedLimit) { this.authenticatedLimit = authenticatedLimit; }
    public int getAuthenticatedWindowSeconds() { return authenticatedWindowSeconds; }
    public void setAuthenticatedWindowSeconds(int authenticatedWindowSeconds) { this.authenticatedWindowSeconds = authenticatedWindowSeconds; }

    public int getReportsLimit() { return reportsLimit; }
    public void setReportsLimit(int reportsLimit) { this.reportsLimit = reportsLimit; }
    public int getReportsWindowSeconds() { return reportsWindowSeconds; }
    public void setReportsWindowSeconds(int reportsWindowSeconds) { this.reportsWindowSeconds = reportsWindowSeconds; }
}