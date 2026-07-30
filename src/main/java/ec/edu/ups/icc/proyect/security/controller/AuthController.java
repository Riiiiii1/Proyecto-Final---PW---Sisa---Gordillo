package ec.edu.ups.icc.proyect.security.controller;

import ec.edu.ups.icc.proyect.security.dto.AuthResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.CurrentUserResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.LoginRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RefreshTokenRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RegisterRequestDTO;
import ec.edu.ups.icc.proyect.security.service.AuthService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API para autenticación y gestión de sesión")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un access token y un refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una cuenta con rol PARTICIPANT por defecto y devuelve tokens de sesión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación"),
            @ApiResponse(responseCode = "409", description = "El correo ya se encuentra registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Renovar access token", description = "Genera un nuevo access token y rota el refresh token a partir de uno vigente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado o no reconocido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token proporcionado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada exitosamente")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request);
    }

    @Operation(summary = "Obtener el usuario autenticado", description = "Devuelve los datos del usuario actualmente autenticado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDTO> me(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser));
    }
}