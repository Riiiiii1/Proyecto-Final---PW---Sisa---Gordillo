package ec.edu.ups.icc.proyect.events.dto;

import ec.edu.ups.icc.proyect.events.enums.EventStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateEventStatusDTO {

    @NotNull(message = "El estado es obligatorio")
    private EventStatus status;

    public UpdateEventStatusDTO() {}

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
}