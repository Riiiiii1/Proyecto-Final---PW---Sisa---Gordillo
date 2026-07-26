package ec.edu.ups.icc.proyect.security.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.security.dto.AuthResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.LoginRequestDTO;
import ec.edu.ups.icc.proyect.security.util.JwtUtil;
import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException("El usuario no está activo");
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponseDTO.of(accessToken, refreshToken, 3600);
    }
}