package ec.edu.ups.icc.proyect.registrations.dto;

import ec.edu.ups.icc.proyect.registrations.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRegistrationStatusDTO(

        @NotNull(message = "El nuevo estado es obligatorio")
        RegistrationStatus status

) {
}