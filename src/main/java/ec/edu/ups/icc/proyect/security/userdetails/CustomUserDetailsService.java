package ec.edu.ups.icc.proyect.security.userdetails;

import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 * Puente entre Spring Security y la base de datos.
 * Spring Security llama a loadUserByUsername(...) durante la
 * autenticación (login) y también dentro del filtro JWT para
 * reconstruir el usuario autenticado en cada request.
 *
 * "username" aquí es en realidad el email (ver CustomUserDetails).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return new CustomUserDetails(user);
    }
}