package ec.edu.ups.icc.proyect.sessions.service;

import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import ec.edu.ups.icc.proyect.sessions.dto.CreateSessionDTO;
import ec.edu.ups.icc.proyect.sessions.dto.SessionResponseDTO;
import java.util.List;

public interface SessionService {
    List<SessionResponseDTO> getSessionsByEventId(Long eventId);
    SessionResponseDTO createSession(CreateSessionDTO dto, UserDetailsImpl currentUser);
    void deleteSession(Long id, UserDetailsImpl currentUser);
}