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
import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "status", "registeredAt", "statusUpdatedAt"
    );

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   EventRepository eventRepository,
                                   UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RegistrationResponseDTO create(CreateRegistrationDTO dto, UserDetailsImpl currentUser) {
        EventEntity event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new NotFoundException("Evento con id " + dto.eventId() + " no encontrado"));

        if (Boolean.TRUE.equals(event.getDeleted())) {
            throw new NotFoundException("Evento con id " + dto.eventId() + " no encontrado");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Solo se puede inscribir en eventos publicados");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(toOffset(event.getRegistrationStartAt())) || now.isAfter(toOffset(event.getRegistrationEndAt()))) {
            throw new BadRequestException("El periodo de inscripciones para este evento no está activo");
        }

        if (event.getAvailableCapacity() <= 0) {
            throw new ConflictException("NO_AVAILABLE_SLOTS", "El evento no tiene cupos disponibles");
        }

        Long participantId = currentUser.getId();

        if (registrationRepository.existsByEvent_IdAndParticipant_Id(dto.eventId(), participantId)) {
            throw new ConflictException("DUPLICATE_RESOURCE", "Ya tienes una inscripción registrada en este evento");
        }

        User participant = userRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        RegistrationEntity registration = RegistrationEntity.builder()
                .event(event)
                .participant(participant)
                .status(RegistrationStatus.PENDING)
                .build();

        registration = registrationRepository.save(registration);

        return toResponseDto(registration);
    }

    @Override
    @Transactional
    public RegistrationResponseDTO updateStatus(Long id, UpdateRegistrationStatusDTO dto, UserDetailsImpl currentUser) {
        RegistrationEntity registration = registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inscripción con id " + id + " no encontrada"));

        RegistrationStatus target = dto.status();
        RegistrationStatus current = registration.getStatus();

        boolean isOwner = registration.getParticipant().getId().equals(currentUser.getId());
        boolean isEventOwner = registration.getEvent().getOrganizer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        switch (target) {
            case CONFIRMED, REJECTED -> {
                if (!isEventOwner && !isAdmin) {
                    throw new ForbiddenException("Solo el organizador del evento o un administrador pueden confirmar o rechazar inscripciones");
                }
                if (current != RegistrationStatus.PENDING) {
                    throw new BadRequestException("Solo se pueden confirmar o rechazar inscripciones en estado PENDING");
                }
            }
            case CANCELLED -> {
                if (!isOwner && !isEventOwner && !isAdmin) {
                    throw new ForbiddenException("No tienes permiso para cancelar esta inscripción");
                }
                if (current == RegistrationStatus.CANCELLED || current == RegistrationStatus.REJECTED) {
                    throw new BadRequestException("La inscripción ya está cancelada o rechazada");
                }
            }
            case PENDING -> throw new BadRequestException("No se puede volver a establecer el estado PENDING manualmente");
        }

        EventEntity event = registration.getEvent();
        OffsetDateTime now = OffsetDateTime.now();

        if (target == RegistrationStatus.CONFIRMED) {
            if (event.getAvailableCapacity() <= 0) {
                throw new ConflictException("NO_AVAILABLE_SLOTS", "El evento ya no tiene cupos disponibles");
            }
            event.setAvailableCapacity(event.getAvailableCapacity() - 1);
            eventRepository.save(event);
            registration.setConfirmedAt(now);
        }

        if (target == RegistrationStatus.CANCELLED && current == RegistrationStatus.CONFIRMED) {
            event.setAvailableCapacity(event.getAvailableCapacity() + 1);
            eventRepository.save(event);
        }

        if (target == RegistrationStatus.CANCELLED) {
            registration.setCancelledAt(now);
        }

        registration.setStatus(target);
        registration.setStatusUpdatedAt(now);

        registration = registrationRepository.save(registration);

        return toResponseDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDTO findById(Long id, UserDetailsImpl currentUser) {
        RegistrationEntity registration = registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inscripción con id " + id + " no encontrada"));

        boolean isOwner = registration.getParticipant().getId().equals(currentUser.getId());
        boolean isEventOwner = registration.getEvent().getOrganizer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isEventOwner && !isAdmin) {
            throw new ForbiddenException("No tienes permiso para ver esta inscripción");
        }

        return toResponseDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponseDTO> findMine(UserDetailsImpl currentUser, PaginationDTO pagination) {
        Pageable pageable = createPageable(pagination);
        return registrationRepository.findByParticipant_Id(currentUser.getId(), pageable)
                .map(this::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponseDTO> findByEvent(Long eventId, UserDetailsImpl currentUser, PaginationDTO pagination) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento con id " + eventId + " no encontrado"));

        boolean isEventOwner = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isEventOwner && !isAdmin) {
            throw new ForbiddenException("No tienes permiso para ver las inscripciones de este evento");
        }

        Pageable pageable = createPageable(pagination);
        return registrationRepository.findByEvent_Id(eventId, pageable)
                .map(this::toResponseDto);
    }

    private RegistrationResponseDTO toResponseDto(RegistrationEntity r) {
        return new RegistrationResponseDTO(
                r.getId(),
                r.getRegistrationCode(),
                r.getEvent().getId(),
                r.getEvent().getTitle(),
                r.getParticipant().getId(),
                r.getParticipant().getFirstName() + " " + r.getParticipant().getLastName(),
                r.getStatus(),
                r.getRegisteredAt(),
                r.getStatusUpdatedAt(),
                r.getConfirmedAt(),
                r.getCancelledAt()
        );
    }

    private OffsetDateTime toOffset(java.time.LocalDateTime localDateTime) {
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
    }

    private Pageable createPageable(PaginationDTO pagination) {
        String sortBy = normalizeSortBy(pagination.getSortBy());
        Sort.Direction direction = normalizeDirection(pagination.getDirection());
        return PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by(direction, sortBy));
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return "id";
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        }
        return sortBy;
    }

    private Sort.Direction normalizeDirection(String direction) {
        if (direction == null || direction.isBlank()) return Sort.Direction.ASC;
        if (direction.equalsIgnoreCase("asc")) return Sort.Direction.ASC;
        if (direction.equalsIgnoreCase("desc")) return Sort.Direction.DESC;
        throw new BadRequestException("Dirección de ordenamiento no válida: " + direction);
    }
}