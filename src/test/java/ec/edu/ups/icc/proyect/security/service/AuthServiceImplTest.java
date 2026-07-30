package ec.edu.ups.icc.proyect.security.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .firstName("Carlos")
                .lastName("Gordillo")
                .email("carlos@academic.test")
                .passwordHash("encodedPassword")
                .status("ACTIVE")
                .userRoles(new HashSet<>())
                .build();

        loginRequest = new LoginRequestDTO("carlos@academic.test", "Password123*");
    }

    @Test
    void login_deberiaRetornarTokens_cuandoCredencialesValidas() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.password(), activeUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessTokenFromUserDetails(any())).thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token-123");
        when(jwtProperties.getExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        AuthResponseDTO response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-123");
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void login_deberiaLanzarBadRequest_cuandoElCorreoNoExiste() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void login_deberiaLanzarBadRequest_cuandoLaContraseñaEsIncorrecta() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.password(), activeUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void login_deberiaLanzarBadRequest_cuandoElUsuarioNoEstaActivo() {
        activeUser.setStatus("BLOCKED");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.password(), activeUser.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está activo");
    }

    @Test
    void register_deberiaCrearUsuario_cuandoCorreoNoExiste() {
        RegisterRequestDTO request = new RegisterRequestDTO("Ana", "Torres", "ana.torres@academic.test", "Password123*");
        RoleEntity participantRole = RoleEntity.builder().id(3L).name(RoleName.PARTICIPANT).description("Participante").build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findByName(RoleName.PARTICIPANT)).thenReturn(Optional.of(participantRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessTokenFromUserDetails(any())).thenReturn("access-token-456");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token-456");
        when(jwtProperties.getExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        AuthResponseDTO response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_deberiaLanzarBadRequest_cuandoElCorreoYaExiste() {
        RegisterRequestDTO request = new RegisterRequestDTO("Ana", "Torres", "ana.torres@academic.test", "Password123*");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("correo ya se encuentra registrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void refresh_deberiaRotarToken_cuandoRefreshTokenEsValido() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-valido");

        RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
                .id(1L)
                .user(activeUser)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(jwtUtil.validateRefreshToken(request.refreshToken())).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(jwtUtil.generateAccessTokenFromUserDetails(any())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtProperties.getExpiration()).thenReturn(900000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        AuthResponseDTO response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(storedToken.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    void refresh_deberiaLanzarBadRequest_cuandoElJwtEsInvalido() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-invalido");

        when(jwtUtil.validateRefreshToken(request.refreshToken())).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadRequestException.class);

        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void refresh_deberiaLanzarBadRequest_cuandoElTokenYaFueRevocado() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-revocado");

        RefreshTokenEntity revokedToken = RefreshTokenEntity.builder()
                .id(2L)
                .user(activeUser)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revokedAt(OffsetDateTime.now().minusMinutes(5))
                .build();

        when(jwtUtil.validateRefreshToken(request.refreshToken())).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void logout_deberiaRevocarToken_cuandoExiste() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-activo");

        RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
                .id(3L)
                .user(activeUser)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        authService.logout(request);

        assertThat(storedToken.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void getCurrentUser_deberiaRetornarDatosDelUsuario_conSusRoles() {
        RoleEntity adminRole = RoleEntity.builder().id(1L).name(RoleName.ADMIN).description("Administrador").build();
        UserRole userRole = UserRole.builder().user(activeUser).role(adminRole).build();
        activeUser.getUserRoles().add(userRole);

        UserDetailsImpl principal = UserDetailsImpl.build(activeUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        CurrentUserResponseDTO result = authService.getCurrentUser(principal);

        assertThat(result.email()).isEqualTo("carlos@academic.test");
        assertThat(result.roles()).contains("ADMIN");
    }
}