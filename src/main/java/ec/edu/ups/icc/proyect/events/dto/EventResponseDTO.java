package ec.edu.ups.icc.proyect.events.dto;

import ec.edu.ups.icc.proyect.events.enums.EventModality;
import ec.edu.ups.icc.proyect.events.enums.EventStatus;
import java.time.LocalDateTime;

public class EventResponseDTO {

    private Long id;
    private String title;
    private String description;
    private EventModality modality;
    private String location;
    private String virtualUrl;
    private Integer capacity;
    private Integer availableCapacity;
    private LocalDateTime registrationStartAt;
    private LocalDateTime registrationEndAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryDto organizer;
    private CategorySummaryDto category;

    public EventResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) { this.availableCapacity = availableCapacity; }
    public LocalDateTime getRegistrationStartAt() { return registrationStartAt; }
    public void setRegistrationStartAt(LocalDateTime registrationStartAt) { this.registrationStartAt = registrationStartAt; }
    public LocalDateTime getRegistrationEndAt() { return registrationEndAt; }
    public void setRegistrationEndAt(LocalDateTime registrationEndAt) { this.registrationEndAt = registrationEndAt; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UserSummaryDto getOrganizer() { return organizer; }
    public void setOrganizer(UserSummaryDto organizer) { this.organizer = organizer; }
    public CategorySummaryDto getCategory() { return category; }
    public void setCategory(CategorySummaryDto category) { this.category = category; }

    public static class UserSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public UserSummaryDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class CategorySummaryDto {
        private Long id;
        private String name;

        public CategorySummaryDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}