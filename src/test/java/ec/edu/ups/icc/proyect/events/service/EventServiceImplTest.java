package ec.edu.ups.icc.proyect.events.service;

import ec.edu.ups.icc.proyect.categories.entity.CategoryEntity;
import ec.edu.ups.icc.proyect.categories.repository.CategoryRepository;
import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.ConflictException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.events.dto.CreateEventDTO;
import ec.edu.ups.icc.proyect.events.dto.EventFilterDTO;
import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import ec.edu.ups.icc.proyect.events.dto.UpdateEventDTO;
import ec.edu.ups.icc.proyect.events.dto.UpdateEventStatusDTO;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.enums.EventModality;
import ec.edu.ups.icc.proyect.events.enums.EventStatus;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private User organizerUser;
    private CategoryEntity category;
    private UserDetailsImpl organizerPrincipal;
    private UserDetailsImpl adminPrincipal;
    private CreateEventDTO createDto;

    @BeforeEach
    void setUp() {
        organizerUser = User.builder()
                .id(1L)
                .firstName("Carlos")
                .lastName("Sisa")
                .email("carlos@academic.test")
                .passwordHash("hash")
                .status("ACTIVE")
                .build();

        category = CategoryEntity.builder()
                .id(1L)
                .name("Tecnología")
                .active(true)
                .build();

        List<GrantedAuthority> organizerAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        organizerPrincipal = new UserDetailsImpl(1L, "Carlos Sisa", "carlos@academic.test", "hash", "ACTIVE", organizerAuthorities);

        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        adminPrincipal = new UserDetailsImpl(99L, "Admin", "admin@academic.test", "hash", "ACTIVE", adminAuthorities);

        createDto = new CreateEventDTO();
        createDto.setTitle("Taller de Spring Boot");
        createDto.setDescription("Descripción del taller");
        createDto.setModality(EventModality.PRESENTIAL);
        createDto.setLocation("Auditorio Central");
        createDto.setVirtualUrl(null);
        createDto.setCapacity(50);
        createDto.setRegistrationStartAt(LocalDateTime.now().plusDays(1));
        createDto.setRegistrationEndAt(LocalDateTime.now().plusDays(5));
        createDto.setStartAt(LocalDateTime.now().plusDays(10));
        createDto.setEndAt(LocalDateTime.now().plusDays(10).plusHours(4));
        createDto.setCategoryId(1L);
    }


    @Test
    void create_deberiaRetornarEventoCreado_cuandoDatosValidos() {
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizerUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> {
            EventEntity e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });

        EventResponseDTO result = eventService.create(createDto, organizerPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Taller de Spring Boot");
        assertThat(result.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(result.getOrganizer().getId()).isEqualTo(1L);
        assertThat(result.getCategory().getId()).isEqualTo(1L);
        verify(eventRepository, times(1)).save(any(EventEntity.class));
    }

    @Test
    void create_deberiaLanzarConflictException_cuandoTituloDuplicado() {
        EventEntity existing = new EventEntity();
        existing.setId(5L);
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> eventService.create(createDto, organizerPrincipal))
                .isInstanceOf(ConflictException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void create_deberiaLanzarBadRequestException_cuandoModalidadInvalida() {
        createDto.setModality(EventModality.PRESENTIAL);
        createDto.setLocation(null);

        assertThatThrownBy(() -> eventService.create(createDto, organizerPrincipal))
                .isInstanceOf(BadRequestException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void create_deberiaLanzarBadRequestException_cuandoFechasInvalidas() {
        createDto.setRegistrationStartAt(LocalDateTime.now().plusDays(5));
        createDto.setRegistrationEndAt(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> eventService.create(createDto, organizerPrincipal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void create_deberiaLanzarNotFoundException_cuandoOrganizadorNoExiste() {
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(createDto, organizerPrincipal))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_deberiaLanzarNotFoundException_cuandoCategoriaNoExiste() {
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizerUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(createDto, organizerPrincipal))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    void update_deberiaActualizarEvento_cuandoEsPropietario() {
        EventEntity existingEvent = buildExistingEvent();
        UpdateEventDTO updateDto = buildUpdateDto();

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponseDTO result = eventService.update(100L, updateDto, organizerPrincipal);

        assertThat(result.getTitle()).isEqualTo("Taller Actualizado XYZ");
        verify(eventRepository).save(any(EventEntity.class));
    }

    @Test
    void update_deberiaLanzarAccessDeniedException_cuandoNoEsPropietario() {
        EventEntity existingEvent = buildExistingEvent();

        List<GrantedAuthority> otherOrganizerAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        UserDetailsImpl otroOrganizador = new UserDetailsImpl(2L, "Otro Organizador", "otro@academic.test", "hash", "ACTIVE", otherOrganizerAuthorities);

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.update(100L, buildUpdateDto(), otroOrganizador))
                .isInstanceOf(AccessDeniedException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void update_deberiaPermitirActualizar_cuandoEsAdmin() {
        EventEntity existingEvent = buildExistingEvent();

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponseDTO result = eventService.update(100L, buildUpdateDto(), adminPrincipal);

        assertThat(result).isNotNull();
        verify(eventRepository).save(any(EventEntity.class));
    }

    @Test
    void update_deberiaLanzarNotFoundException_cuandoEventoNoExiste() {
        when(eventRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.update(999L, buildUpdateDto(), organizerPrincipal))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_deberiaLanzarConflictException_cuandoTituloDuplicadoPerteneceAOtroEvento() {
        EventEntity existingEvent = buildExistingEvent();
        EventEntity otherEventWithSameTitle = buildExistingEvent();
        otherEventWithSameTitle.setId(200L);

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.findByTitleIgnoreCaseAndDeletedFalse(anyString())).thenReturn(Optional.of(otherEventWithSameTitle));

        assertThatThrownBy(() -> eventService.update(100L, buildUpdateDto(), organizerPrincipal))
                .isInstanceOf(ConflictException.class);

        verify(eventRepository, never()).save(any());
    }


    @Test
    void delete_deberiaEliminarLogicamente_cuandoEstaEnDraft() {
        EventEntity existingEvent = buildExistingEvent();
        existingEvent.setStatus(EventStatus.DRAFT);

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.delete(100L, organizerPrincipal);

        assertThat(existingEvent.getDeleted()).isTrue();
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void delete_deberiaLanzarConflictException_cuandoEventoYaPublicado() {
        EventEntity existingEvent = buildExistingEvent();
        existingEvent.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.delete(100L, organizerPrincipal))
                .isInstanceOf(ConflictException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void delete_deberiaLanzarAccessDeniedException_cuandoNoEsPropietarioNiAdmin() {
        EventEntity existingEvent = buildExistingEvent();

        List<GrantedAuthority> otherAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        UserDetailsImpl otro = new UserDetailsImpl(2L, "Otro", "otro@academic.test", "hash", "ACTIVE", otherAuthorities);

        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.delete(100L, otro))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void findById_deberiaRetornarEvento_cuandoExiste() {
        EventEntity existingEvent = buildExistingEvent();
        when(eventRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(existingEvent));

        EventResponseDTO result = eventService.findById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findById_deberiaLanzarNotFoundException_cuandoNoExiste() {
        when(eventRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    void findAllPage_deberiaRetornarPaginaDeEventos() {
        EventEntity event1 = buildExistingEvent();
        Page<EventEntity> page = new PageImpl<>(List.of(event1));

        EventFilterDTO filter = new EventFilterDTO();
        filter.setTitle("Taller");

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSortBy("id");
        pagination.setDirection("asc");

        when(eventRepository.findPageWithFilters(eq("Taller"), any(), any(), any())).thenReturn(page);

        Page<EventResponseDTO> result = eventService.findAllPage(filter, pagination);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    void updateStatus_deberiaActualizarEstado_cuandoTransicionValida() {
        Long eventId = 100L;
        UpdateEventStatusDTO dto = new UpdateEventStatusDTO();
        dto.setStatus(EventStatus.PUBLISHED);

        EventEntity existingEvent = buildExistingEvent();
        existingEvent.setStatus(EventStatus.DRAFT);

        when(eventRepository.findByIdAndDeletedFalse(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponseDTO result = eventService.updateStatus(eventId, dto, organizerPrincipal);

        assertThat(result).isNotNull();
        assertThat(existingEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateStatus_deberiaLanzarBadRequestException_cuandoTransicionInvalida() {
        Long eventId = 100L;
        UpdateEventStatusDTO dto = new UpdateEventStatusDTO();
        dto.setStatus(EventStatus.DRAFT);

        EventEntity existingEvent = buildExistingEvent();
        existingEvent.setStatus(EventStatus.FINISHED);

        when(eventRepository.findByIdAndDeletedFalse(eventId)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.updateStatus(eventId, dto, organizerPrincipal))
                .isInstanceOf(BadRequestException.class);

        verify(eventRepository, never()).save(any());
    }


    private EventEntity buildExistingEvent() {
        EventEntity event = new EventEntity();
        event.setId(100L);
        event.setTitle("Taller Original");
        event.setDescription("Descripción original");
        event.setModality(EventModality.PRESENTIAL);
        event.setLocation("Auditorio Central");
        event.setVirtualUrl(null);
        event.setCapacity(50);
        event.setAvailableCapacity(50);
        event.setRegistrationStartAt(LocalDateTime.now().plusDays(1));
        event.setRegistrationEndAt(LocalDateTime.now().plusDays(5));
        event.setStartAt(LocalDateTime.now().plusDays(10));
        event.setEndAt(LocalDateTime.now().plusDays(10).plusHours(4));
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(organizerUser);
        event.setCategory(category);
        event.setDeleted(false);
        return event;
    }

    private UpdateEventDTO buildUpdateDto() {
        UpdateEventDTO dto = new UpdateEventDTO();
        dto.setTitle("Taller Actualizado XYZ");
        dto.setDescription("Nueva descripción");
        dto.setModality(EventModality.PRESENTIAL);
        dto.setLocation("Auditorio Central");
        dto.setVirtualUrl(null);
        dto.setCapacity(60);
        dto.setRegistrationStartAt(LocalDateTime.now().plusDays(1));
        dto.setRegistrationEndAt(LocalDateTime.now().plusDays(5));
        dto.setStartAt(LocalDateTime.now().plusDays(10));
        dto.setEndAt(LocalDateTime.now().plusDays(10).plusHours(4));
        dto.setCategoryId(1L);
        return dto;
    }
}