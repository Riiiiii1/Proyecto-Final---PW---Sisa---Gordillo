package ec.edu.ups.icc.proyect.registrations.dto;

import ec.edu.ups.icc.proyect.registrations.enums.RegistrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrationResponseDTO(

        Long id,
        UUID registrationCode,
        Long eventId,
        String eventTitle,
        Long participantId,
        String participantName,
        RegistrationStatus status,
        OffsetDateTime registeredAt,
        OffsetDateTime statusUpdatedAt,
        OffsetDateTime confirmedAt,
        OffsetDateTime cancelledAt

) {
}