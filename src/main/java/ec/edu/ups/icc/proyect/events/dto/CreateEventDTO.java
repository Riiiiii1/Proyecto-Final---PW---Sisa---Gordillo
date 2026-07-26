package ec.edu.ups.icc.proyect.events.dto;

import ec.edu.ups.icc.proyect.events.enums.EventModality;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CreateEventDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 160, message = "El título debe tener entre 5 y 160 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "La modalidad es obligatoria")
    private EventModality modality;

    @Size(max = 200, message = "La ubicación no debe superar los 200 caracteres")
    private String location;

    @Size(max = 500, message = "La URL no debe superar los 500 caracteres")
    private String virtualUrl;

    @NotNull(message = "El cupo es obligatorio")
    @Min(value = 1, message = "El cupo debe ser mayor a 0")
    private Integer capacity;

    @NotNull(message = "La fecha de inicio de inscripción es obligatoria")
    private LocalDateTime registrationStartAt;

    @NotNull(message = "La fecha de fin de inscripción es obligatoria")
    private LocalDateTime registrationEndAt;

    @NotNull(message = "La fecha de inicio del evento es obligatoria")
    private LocalDateTime startAt;

    @NotNull(message = "La fecha de fin del evento es obligatoria")
    private LocalDateTime endAt;

    @NotNull(message = "La categoría es obligatoria")
    @Min(value = 1, message = "El ID de la categoría no es válido")
    private Long categoryId;

    public CreateEventDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public EventModality getModality() { return modality; }
    public void setModality(EventModality modality) { this.modality = modality; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getVirtualUrl() { return virtualUrl; }
    public void setVirtualUrl(String virtualUrl) { this.virtualUrl = virtualUrl; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public LocalDateTime getRegistrationStartAt() { return registrationStartAt; }
    public void setRegistrationStartAt(LocalDateTime registrationStartAt) { this.registrationStartAt = registrationStartAt; }
    public LocalDateTime getRegistrationEndAt() { return registrationEndAt; }
    public void setRegistrationEndAt(LocalDateTime registrationEndAt) { this.registrationEndAt = registrationEndAt; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}