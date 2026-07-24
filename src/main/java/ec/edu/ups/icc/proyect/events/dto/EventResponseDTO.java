package ec.edu.ups.icc.proyect.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String modality;
    private String location;
    private String virtualUrl;
    private Integer capacity;
    private Integer availableCapacity;
    private OffsetDateTime registrationStartAt;
    private OffsetDateTime registrationEndAt;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String status;

    private Long categoryId;
    private String categoryName;
    private Long organizerId;
    private String organizerName;
}