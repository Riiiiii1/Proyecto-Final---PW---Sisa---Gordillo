package ec.edu.ups.icc.proyect.security.dto;

public record AuthResponseDTO(

        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn

) {
    public static AuthResponseDTO of(String accessToken, String refreshToken, long expiresInSeconds) {
        return new AuthResponseDTO(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}