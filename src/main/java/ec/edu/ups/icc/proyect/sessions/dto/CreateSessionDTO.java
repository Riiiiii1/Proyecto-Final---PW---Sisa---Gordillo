package ec.edu.ups.icc.proyect.sessions.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class CreateSessionDTO {

    @NotBlank(message = "El título de la sesión es obligatorio")
    private String title;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser en el futuro")
    private OffsetDateTime startAt;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private OffsetDateTime endAt;

    private String location;

    @NotNull(message = "El ID del evento asociado es obligatorio")
    private Long eventId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public OffsetDateTime getStartAt() { return startAt; }
    public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }

    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}