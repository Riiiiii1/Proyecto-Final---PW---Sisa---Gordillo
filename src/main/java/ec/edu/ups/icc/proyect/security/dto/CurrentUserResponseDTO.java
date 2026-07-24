package ec.edu.ups.icc.proyect.security.dto;

import java.util.List;

public record CurrentUserResponseDTO(

        Long id,
        String firstName,
        String lastName,
        String email,
        String status,
        List<String> roles

) {
}