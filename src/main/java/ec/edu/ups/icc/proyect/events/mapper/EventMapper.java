package ec.edu.ups.icc.proyect.events.mapper;

import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;

public class EventMapper {
    public static EventResponseDTO toResponse(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setModality(entity.getModality());
        dto.setLocation(entity.getLocation());
        dto.setVirtualUrl(entity.getVirtualUrl());
        dto.setCapacity(entity.getCapacity());
        dto.setAvailableCapacity(entity.getAvailableCapacity());
        dto.setRegistrationStartAt(entity.getRegistrationStartAt());
        dto.setRegistrationEndAt(entity.getRegistrationEndAt());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getOrganizer() != null) {
            EventResponseDTO.UserSummaryDto organizerDto = new EventResponseDTO.UserSummaryDto();
            organizerDto.setId(entity.getOrganizer().getId());
            organizerDto.setFirstName(entity.getOrganizer().getFirstName());
            organizerDto.setLastName(entity.getOrganizer().getLastName());
            organizerDto.setEmail(entity.getOrganizer().getEmail());

            dto.setOrganizer(organizerDto);
        }


        if (entity.getCategory() != null) {
            EventResponseDTO.CategorySummaryDto categoryDto = new EventResponseDTO.CategorySummaryDto();
            categoryDto.setId(entity.getCategory().getId());
            categoryDto.setName(entity.getCategory().getName());

            dto.setCategory(categoryDto);
        }

        return dto;
    }
}