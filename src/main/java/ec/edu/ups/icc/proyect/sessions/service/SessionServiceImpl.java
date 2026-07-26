package ec.edu.ups.icc.proyect.sessions.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.sessions.dto.CreateSessionDTO;
import ec.edu.ups.icc.proyect.sessions.dto.SessionResponseDTO;
import ec.edu.ups.icc.proyect.sessions.entity.SessionEntity;
import ec.edu.ups.icc.proyect.sessions.mapper.SessionMapper;
import ec.edu.ups.icc.proyect.sessions.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final SessionMapper sessionMapper;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              EventRepository eventRepository,
                              SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.sessionMapper = sessionMapper;
    }

    @Override
    @Transactional
    public SessionResponseDTO createSession(CreateSessionDTO dto) {
        if (dto.getStartAt().isAfter(dto.getEndAt()) || dto.getStartAt().isEqual(dto.getEndAt())) {
            throw new BadRequestException("La fecha de inicio debe ser obligatoriamente anterior a la fecha de fin");
        }

        EventEntity event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NotFoundException("Evento no encontrado con ID: " + dto.getEventId()));

        SessionEntity entity = sessionMapper.toEntity(dto);
        entity.setEvent(event);

        SessionEntity savedSession = sessionRepository.save(entity);
        return sessionMapper.toResponseDTO(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDTO> getSessionsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Evento no encontrado con ID: " + eventId);
        }

        return sessionRepository.findByEventId(eventId)
                .stream()
                .map(sessionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new NotFoundException("Sesión no encontrada con ID: " + id);
        }
        sessionRepository.deleteById(id);
    }
}