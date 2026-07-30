package ec.edu.ups.icc.proyect.registrations.service;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.ConflictException;
import ec.edu.ups.icc.proyect.core.exception.domain.ForbiddenException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.enums.EventStatus;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.registrations.dto.CreateRegistrationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.RegistrationResponseDTO;
import ec.edu.ups.icc.proyect.registrations.dto.UpdateRegistrationStatusDTO;
import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import ec.edu.ups.icc.proyect.registrations.enums.RegistrationStatus;
import ec.edu.ups.icc.proyect.registrations.repository.RegistrationRepository;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private User participantUser;
    private User organizerUser;
    private EventEntity validEvent;
    private RegistrationEntity registrationEntity;

    private UserDetailsImpl participantPrincipal;
    private UserDetailsImpl organizerPrincipal;
    private UserDetailsImpl adminPrincipal;

    @BeforeEach
    void setUp() {
        participantUser = User.builder().id(1L).firstName("Carlos").lastName("Participante").build();
        organizerUser = User.builder().id(2L).firstName("David").lastName("Organizador").build();

        participantPrincipal = new UserDetailsImpl(1L, "Carlos P", "carlos@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_PARTICIPANT")));
        organizerPrincipal = new UserDetailsImpl(2L, "David O", "david@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
        adminPrincipal = new UserDetailsImpl(99L, "Admin", "admin@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        validEvent = new EventEntity();
        validEvent.setId(10L);
        validEvent.setTitle("Congreso de Software");
        validEvent.setStatus(EventStatus.PUBLISHED);
        validEvent.setDeleted(false);
        validEvent.setAvailableCapacity(50);
        validEvent.setRegistrationStartAt(LocalDateTime.now().minusDays(1));
        validEvent.setRegistrationEndAt(LocalDateTime.now().plusDays(5));
        validEvent.setOrganizer(organizerUser);

        registrationEntity = RegistrationEntity.builder()
                .id(100L)
                .registrationCode(UUID.randomUUID())
                .event(validEvent)
                .participant(participantUser)
                .status(RegistrationStatus.PENDING)
                .build();
    }


    @Test
    void create_deberiaCrearInscripcion_cuandoDatosSonValidos() {
        CreateRegistrationDTO dto = new CreateRegistrationDTO(10L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));
        when(registrationRepository.existsByEvent_IdAndParticipant_Id(10L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(participantUser));
        when(registrationRepository.save(any(RegistrationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResponseDTO result = registrationService.create(dto, participantPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(result.participantName()).isEqualTo("Carlos Participante");
        verify(registrationRepository).save(any(RegistrationEntity.class));
    }

    @Test
    void create_deberiaLanzarBadRequest_cuandoEventoNoEstaPublicado() {
        validEvent.setStatus(EventStatus.DRAFT);
        CreateRegistrationDTO dto = new CreateRegistrationDTO(10L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));

        assertThatThrownBy(() -> registrationService.create(dto, participantPrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Solo se puede inscribir en eventos publicados");
    }

    @Test
    void create_deberiaLanzarBadRequest_cuandoFechasInscripcionNoEstanActivas() {
        validEvent.setRegistrationStartAt(LocalDateTime.now().plusDays(2));
        validEvent.setRegistrationEndAt(LocalDateTime.now().plusDays(5));
        CreateRegistrationDTO dto = new CreateRegistrationDTO(10L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));

        assertThatThrownBy(() -> registrationService.create(dto, participantPrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("El periodo de inscripciones para este evento no está activo");
    }

    @Test
    void create_deberiaLanzarConflict_cuandoNoHayCupos() {
        validEvent.setAvailableCapacity(0);
        CreateRegistrationDTO dto = new CreateRegistrationDTO(10L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));

        assertThatThrownBy(() -> registrationService.create(dto, participantPrincipal))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("El evento no tiene cupos disponibles");
    }

    @Test
    void create_deberiaLanzarConflict_cuandoYaEstaInscrito() {
        CreateRegistrationDTO dto = new CreateRegistrationDTO(10L);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));
        when(registrationRepository.existsByEvent_IdAndParticipant_Id(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.create(dto, participantPrincipal))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya tienes una inscripción registrada");
    }


    @Test
    void updateStatus_deberiaConfirmarYRestarCupo_cuandoEsOrganizadorYHayCupos() {
        UpdateRegistrationStatusDTO dto = new UpdateRegistrationStatusDTO(RegistrationStatus.CONFIRMED);

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(validEvent);
        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(registrationEntity);

        RegistrationResponseDTO result = registrationService.updateStatus(100L, dto, organizerPrincipal);

        assertThat(result.status()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(validEvent.getAvailableCapacity()).isEqualTo(49);
        verify(eventRepository).save(validEvent);
    }

    @Test
    void updateStatus_deberiaLanzarForbidden_cuandoParticipanteIntentaConfirmar() {
        UpdateRegistrationStatusDTO dto = new UpdateRegistrationStatusDTO(RegistrationStatus.CONFIRMED);

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));

        assertThatThrownBy(() -> registrationService.updateStatus(100L, dto, participantPrincipal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Solo el organizador del evento o un administrador pueden confirmar");
    }

    @Test
    void updateStatus_deberiaRestaurarCupo_cuandoSeCancelaUnaInscripcionConfirmada() {
        registrationEntity.setStatus(RegistrationStatus.CONFIRMED);
        validEvent.setAvailableCapacity(49);

        UpdateRegistrationStatusDTO dto = new UpdateRegistrationStatusDTO(RegistrationStatus.CANCELLED);

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(validEvent);
        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(registrationEntity);

        RegistrationResponseDTO result = registrationService.updateStatus(100L, dto, participantPrincipal);

        assertThat(result.status()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(validEvent.getAvailableCapacity()).isEqualTo(50);
        verify(eventRepository).save(validEvent);
    }

    @Test
    void updateStatus_deberiaLanzarBadRequest_cuandoSeIntentaVolverAPending() {
        UpdateRegistrationStatusDTO dto = new UpdateRegistrationStatusDTO(RegistrationStatus.PENDING);
        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));

        assertThatThrownBy(() -> registrationService.updateStatus(100L, dto, adminPrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No se puede volver a establecer el estado PENDING manualmente");
    }


    @Test
    void findById_deberiaRetornarInscripcion_cuandoEsPropietario() {
        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));

        RegistrationResponseDTO result = registrationService.findById(100L, participantPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
    }

    @Test
    void findById_deberiaLanzarForbidden_cuandoEsOtroParticipante() {
        UserDetailsImpl otroParticipante = new UserDetailsImpl(9L, "Otro", "otro@test.com", "hash", "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_PARTICIPANT")));

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registrationEntity));

        assertThatThrownBy(() -> registrationService.findById(100L, otroParticipante))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findMine_deberiaRetornarPaginaDeInscripciones() {
        PaginationDTO pagination = new PaginationDTO();
        Page<RegistrationEntity> page = new PageImpl<>(List.of(registrationEntity));

        when(registrationRepository.findByParticipant_Id(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<RegistrationResponseDTO> result = registrationService.findMine(participantPrincipal, pagination);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
    }

    @Test
    void findByEvent_deberiaRetornarPagina_cuandoEsOrganizador() {
        PaginationDTO pagination = new PaginationDTO();
        Page<RegistrationEntity> page = new PageImpl<>(List.of(registrationEntity));

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));
        when(registrationRepository.findByEvent_Id(eq(10L), any(Pageable.class))).thenReturn(page);

        Page<RegistrationResponseDTO> result = registrationService.findByEvent(10L, organizerPrincipal, pagination);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByEvent_deberiaLanzarForbidden_cuandoNoEsOrganizadorNiAdmin() {
        PaginationDTO pagination = new PaginationDTO();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(validEvent));

        assertThatThrownBy(() -> registrationService.findByEvent(10L, participantPrincipal, pagination))
                .isInstanceOf(ForbiddenException.class);
    }
}