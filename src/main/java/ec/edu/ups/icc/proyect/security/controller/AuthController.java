package ec.edu.ups.icc.proyect.security.controller;

import ec.edu.ups.icc.proyect.security.dto.AuthResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.CurrentUserResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.LoginRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RefreshTokenRequestDTO;
import ec.edu.ups.icc.proyect.security.dto.RegisterRequestDTO;
import ec.edu.ups.icc.proyect.security.service.AuthService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDTO> me(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser));
    }
}