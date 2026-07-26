package ec.edu.ups.icc.proyect.sessions.service;

import ec.edu.ups.icc.proyect.sessions.dto.CreateSessionDTO;
import ec.edu.ups.icc.proyect.sessions.dto.SessionResponseDTO;
import java.util.List;

public interface SessionService {
    SessionResponseDTO createSession(CreateSessionDTO dto);
    List<SessionResponseDTO> getSessionsByEventId(Long eventId);
    void deleteSession(Long id);
}