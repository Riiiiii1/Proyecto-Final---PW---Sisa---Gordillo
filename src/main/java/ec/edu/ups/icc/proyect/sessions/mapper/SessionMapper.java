package ec.edu.ups.icc.proyect.sessions.mapper;

import ec.edu.ups.icc.proyect.sessions.dto.CreateSessionDTO;
import ec.edu.ups.icc.proyect.sessions.dto.SessionResponseDTO;
import ec.edu.ups.icc.proyect.sessions.entity.SessionEntity;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionResponseDTO toResponseDTO(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setLocation(entity.getLocation());

        if (entity.getEvent() != null) {
            dto.setEventId(entity.getEvent().getId());
        }
        return dto;
    }

    public SessionEntity toEntity(CreateSessionDTO dto) {
        if (dto == null) {
            return null;
        }
        SessionEntity entity = new SessionEntity();
        entity.setTitle(dto.getTitle());
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setLocation(dto.getLocation());
        return entity;
    }
}