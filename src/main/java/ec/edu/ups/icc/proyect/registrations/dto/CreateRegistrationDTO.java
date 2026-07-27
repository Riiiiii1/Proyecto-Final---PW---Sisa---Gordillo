package ec.edu.ups.icc.proyect.registrations.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRegistrationDTO(

        @NotNull(message = "El id del evento es obligatorio")
        Long eventId

) {
}