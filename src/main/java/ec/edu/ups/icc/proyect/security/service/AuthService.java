package ec.edu.ups.icc.proyect.security.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.security.config.JwtProperties;
import ec.edu.ups.icc.proyect.security.dto.AuthResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.CurrentUserResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.LoginRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RefreshTokenRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RegisterRequestDTO;
import ec.edu.ups.icc.proyect.security.entity.RefreshTokenEntity;
import ec.edu.ups.icc.proyect.security.entity.RoleEntity;
import ec.edu.ups.icc.proyect.security.enums.RoleName;
import ec.edu.ups.icc.proyect.security.repository.RefreshTokenRepository;
import ec.edu.ups.icc.proyect.security.repository.RoleRepository;
import ec.edu.ups.icc.proyect.security.util.JwtUtil;
import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.entity.UserRole;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;


    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El correo ya se encuentra registrado");
        }

        RoleEntity participantRole = roleRepository.findByName(RoleName.PARTICIPANT)
                .orElseThrow(() -> new IllegalStateException("El rol PARTICIPANT no está configurado en el sistema"));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status("ACTIVE")
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(participantRole)
                .build();

        user.getUserRoles().add(userRole);

        User savedUser = userRepository.save(user);

        return issueTokens(savedUser);
    }

    @Transactional
    public AuthResponseDTO refresh(RefreshTokenRequestDTO request) {
        String rawToken = request.refreshToken();

        if (!jwtUtil.validateRefreshToken(rawToken)) {
            throw new BadRequestException("Refresh token inválido o expirado");
        }

        RefreshTokenEntity storedToken = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new BadRequestException("Refresh token no reconocido"));

        if (!storedToken.isActive()) {
            throw new BadRequestException("Refresh token inválido o expirado");
        }

        User user = storedToken.getUser();

        storedToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(storedToken);

        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequestDTO request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .ifPresent(storedToken -> {
                    storedToken.setRevokedAt(OffsetDateTime.now());
                    refreshTokenRepository.save(storedToken);
                });
    }

    @Transactional(readOnly = true)
    public CurrentUserResponseDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().name())
                .toList();

        return new CurrentUserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus(),
                roles
        );
    }

    private AuthResponseDTO issueTokens(User user) {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .tokenId(UUID.randomUUID())
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.of(accessToken, refreshToken, jwtProperties.getExpiration() / 1000);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error al generar el hash del token", e);
        }
    }
}