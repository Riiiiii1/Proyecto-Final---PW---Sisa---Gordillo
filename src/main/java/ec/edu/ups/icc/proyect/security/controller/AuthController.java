package ec.edu.ups.icc.proyect.security.controller;

import ec.edu.ups.icc.proyect.security.dto.AuthResponseDTO;
import ec.edu.ups.icc.proyect.security.dto.LoginRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO mockResponse = AuthResponseDTO.of("token_de_prueba", "refresh_token_prueba", 3600);

        return ResponseEntity.ok(mockResponse);
    }
}