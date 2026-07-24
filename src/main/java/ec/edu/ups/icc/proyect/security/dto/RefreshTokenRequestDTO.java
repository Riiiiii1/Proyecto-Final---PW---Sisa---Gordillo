package ec.edu.ups.icc.proyect.security.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken

) {
}