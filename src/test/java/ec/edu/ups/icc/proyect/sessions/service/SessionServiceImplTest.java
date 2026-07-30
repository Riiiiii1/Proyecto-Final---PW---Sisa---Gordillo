package ec.edu.ups.icc.proyect.sessions.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.ForbiddenException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import ec.edu.ups.icc.proyect.sessions.dto.CreateSessionDTO;
import ec.edu.ups.icc.proyect.sessions.dto.SessionResponseDTO;
import ec.edu.ups.icc.proyect.sessions.entity.SessionEntity;
import ec.edu.ups.icc.proyect.sessions.mapper.SessionMapper;
import ec.edu.ups.icc.proyect.sessions.repository.SessionRepository;
import ec.edu.ups.icc.proyect.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private EventEntity eventEntity;
    private SessionEntity sessionEntity;
    private CreateSessionDTO createDTO;
    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);

        eventEntity = new EventEntity();
        eventEntity.setId(10L);
        eventEntity.setTitle("Evento Tecnologico");
        eventEntity.setOrganizer(organizer);

        sessionEntity = new SessionEntity();
        sessionEntity.setId(100L);
        sessionEntity.setTitle("Charla de Spring");
        sessionEntity.setEvent(eventEntity);
        sessionEntity.setStartAt(OffsetDateTime.now().plusDays(1));
        sessionEntity.setEndAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        sessionEntity.setLocation("Auditorio");

        createDTO = new CreateSessionDTO();
        createDTO.setEventId(10L);
        createDTO.setTitle("Charla de Spring");
        createDTO.setStartAt(OffsetDateTime.now().plusDays(1));
        createDTO.setEndAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        createDTO.setLocation("Auditorio");
    }

    // ---- helpers para construir usuarios de prueba ----

    private UserDetailsImpl ownerUser(Long id) {
        return new UserDetailsImpl(id, "Organizador", "org@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
    }

    private UserDetailsImpl otherOrganizerUser(Long id) {
        return new UserDetailsImpl(id, "Otro Organizador", "otro@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
    }

    private UserDetailsImpl adminUser(Long id) {
        return new UserDetailsImpl(id, "Admin", "admin@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // ---- createSession ----

    @Test
    void createSession_deberiaCrearSesion_cuandoEsPropietario() {
        UserDetailsImpl currentUser = ownerUser(1L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(sessionMapper.toEntity(createDTO)).thenReturn(sessionEntity);
        when(sessionRepository.save(any(SessionEntity.class))).thenReturn(sessionEntity);

        SessionResponseDTO responseDto = new SessionResponseDTO();
        responseDto.setId(100L);
        responseDto.setTitle("Charla de Spring");
        responseDto.setEventId(10L);
        when(sessionMapper.toResponseDTO(sessionEntity)).thenReturn(responseDto);

        SessionResponseDTO result = sessionService.createSession(createDTO, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        verify(sessionRepository).save(any(SessionEntity.class));
    }

    @Test
    void createSession_deberiaCrearSesion_cuandoEsAdmin() {
        UserDetailsImpl currentUser = adminUser(99L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(sessionMapper.toEntity(createDTO)).thenReturn(sessionEntity);
        when(sessionRepository.save(any(SessionEntity.class))).thenReturn(sessionEntity);
        when(sessionMapper.toResponseDTO(sessionEntity)).thenReturn(new SessionResponseDTO());

        SessionResponseDTO result = sessionService.createSession(createDTO, currentUser);

        assertThat(result).isNotNull();
        verify(sessionRepository).save(any(SessionEntity.class));
    }

    @Test
    void createSession_deberiaLanzarForbidden_cuandoNoEsPropietarioNiAdmin() {
        UserDetailsImpl currentUser = otherOrganizerUser(2L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));

        assertThatThrownBy(() -> sessionService.createSession(createDTO, currentUser))
                .isInstanceOf(ForbiddenException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_deberiaLanzarBadRequest_cuandoStartAtEsIgualOPosteriorAEndAt() {
        UserDetailsImpl currentUser = ownerUser(1L);
        OffsetDateTime mismaFecha = OffsetDateTime.now().plusDays(1);
        createDTO.setStartAt(mismaFecha);
        createDTO.setEndAt(mismaFecha);

        assertThatThrownBy(() -> sessionService.createSession(createDTO, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("anterior a la fecha de fin");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_deberiaLanzarNotFound_cuandoEventoNoExiste() {
        UserDetailsImpl currentUser = ownerUser(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.createSession(createDTO, currentUser))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Evento no encontrado");

        verify(sessionRepository, never()).save(any());
    }

    // ---- getSessionsByEventId ----

    @Test
    void getSessionsByEventId_deberiaRetornarLista_cuandoEventoExiste() {
        when(eventRepository.existsById(10L)).thenReturn(true);
        when(sessionRepository.findByEventId(10L)).thenReturn(List.of(sessionEntity));

        SessionResponseDTO responseDto = new SessionResponseDTO();
        responseDto.setId(100L);
        responseDto.setEventId(10L);
        when(sessionMapper.toResponseDTO(sessionEntity)).thenReturn(responseDto);

        List<SessionResponseDTO> result = sessionService.getSessionsByEventId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventId()).isEqualTo(10L);
    }

    @Test
    void getSessionsByEventId_deberiaLanzarNotFound_cuandoEventoNoExiste() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> sessionService.getSessionsByEventId(99L))
                .isInstanceOf(NotFoundException.class);

        verify(sessionRepository, never()).findByEventId(any());
    }

    // ---- deleteSession ----

    @Test
    void deleteSession_deberiaEliminar_cuandoEsPropietario() {
        UserDetailsImpl currentUser = ownerUser(1L);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(sessionEntity));

        sessionService.deleteSession(100L, currentUser);

        verify(sessionRepository).deleteById(100L);
    }

    @Test
    void deleteSession_deberiaEliminar_cuandoEsAdmin() {
        UserDetailsImpl currentUser = adminUser(99L);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(sessionEntity));

        sessionService.deleteSession(100L, currentUser);

        verify(sessionRepository).deleteById(100L);
    }

    @Test
    void deleteSession_deberiaLanzarForbidden_cuandoNoEsPropietarioNiAdmin() {
        UserDetailsImpl currentUser = otherOrganizerUser(2L);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(sessionEntity));

        assertThatThrownBy(() -> sessionService.deleteSession(100L, currentUser))
                .isInstanceOf(ForbiddenException.class);

        verify(sessionRepository, never()).deleteById(any());
    }

    @Test
    void deleteSession_deberiaLanzarNotFound_cuandoNoExiste() {
        UserDetailsImpl currentUser = ownerUser(1L);
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.deleteSession(999L, currentUser))
                .isInstanceOf(NotFoundException.class);

        verify(sessionRepository, never()).deleteById(any());
    }
}