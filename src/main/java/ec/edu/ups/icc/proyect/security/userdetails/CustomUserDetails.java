package ec.edu.ups.icc.proyect.security.userdetails;

import ec.edu.ups.icc.proyect.users.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/*
 * Adaptador entre la entidad User (tu modelo de dominio) y lo que
 * Spring Security necesita para trabajar con autenticación (UserDetails).
 *
 * - getUsername() devuelve el email, porque en este proyecto se hace
 *   login con email, no con un username separado.
 * - Las authorities llevan el prefijo "ROLE_" que exige hasRole('X')
 *   en @PreAuthorize (así hasRole('ADMIN') encuentra "ROLE_ADMIN").
 * - isEnabled()/isAccountNonLocked() reflejan el campo status
 *   (ACTIVE vs BLOCKED) de la tabla users.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final User.Status status;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.status = user.getStatus();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == User.Status.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == User.Status.ACTIVE;
    }
}