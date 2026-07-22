package ec.edu.ups.icc.proyect.categories.dto;

import java.time.OffsetDateTime;

public record CategoryResponseDTO(

        Long id,
        String name,
        String description,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}