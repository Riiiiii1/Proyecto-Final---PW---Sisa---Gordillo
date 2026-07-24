package ec.edu.ups.icc.proyect.events.mapper;

import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventResponseDTO toResponseDTO(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        return EventResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .modality(entity.getModality())
                .location(entity.getLocation())
                .virtualUrl(entity.getVirtualUrl())
                .capacity(entity.getCapacity())
                .availableCapacity(entity.getAvailableCapacity())
                .registrationStartAt(entity.getRegistrationStartAt())
                .registrationEndAt(entity.getRegistrationEndAt())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .status(entity.getStatus())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .organizerId(entity.getOrganizer() != null ? entity.getOrganizer().getId() : null)
                .organizerName(entity.getOrganizer() != null ?
                        entity.getOrganizer().getFirstName() + " " + entity.getOrganizer().getLastName() : null)
                .build();
    }
}