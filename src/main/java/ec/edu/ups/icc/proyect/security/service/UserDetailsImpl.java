package ec.edu.ups.icc.proyect.security.service;

import ec.edu.ups.icc.proyect.security.entity.RoleEntity;
import ec.edu.ups.icc.proyect.users.entity.User;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final String status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String name, String email, String password,
                           String status, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        Collection<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(userRole -> {
                    RoleEntity role = userRole.getRole();
                    return new SimpleGrantedAuthority("ROLE_" + role.getName().name());
                })
                .collect(Collectors.toList());

        String fullName = user.getFirstName() + " " + user.getLastName();

        return new UserDetailsImpl(
                user.getId(),
                fullName,
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                authorities
        );
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
        return !"BLOCKED".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
}