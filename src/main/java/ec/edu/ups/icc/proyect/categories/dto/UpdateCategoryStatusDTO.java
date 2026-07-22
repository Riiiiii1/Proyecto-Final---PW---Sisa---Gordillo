package ec.edu.ups.icc.proyect.categories.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCategoryStatusDTO(
        @NotNull(message = "El campo 'active' es obligatorio")
        Boolean active
) {
}
