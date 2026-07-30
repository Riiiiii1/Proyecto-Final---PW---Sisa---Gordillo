package ec.edu.ups.icc.proyect.events.service;

import ec.edu.ups.icc.proyect.categories.entity.CategoryEntity;
import ec.edu.ups.icc.proyect.categories.repository.CategoryRepository;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
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
import ec.edu.ups.icc.proyect.events.mapper.EventMapper;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import ec.edu.ups.icc.proyect.users.entity.User;
import ec.edu.ups.icc.proyect.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public EventResponseDTO create(CreateEventDTO dto, UserDetailsImpl currentUser) {
        validateDates(dto.getRegistrationStartAt(), dto.getRegistrationEndAt(), dto.getStartAt(), dto.getEndAt());

        String location = normalizeBlankToNull(dto.getLocation());
        String virtualUrl = normalizeBlankToNull(dto.getVirtualUrl());
        validateModality(dto.getModality(), location, virtualUrl);

        if (eventRepository.findByTitleIgnoreCaseAndDeletedFalse(dto.getTitle().trim()).isPresent()) {
            throw new ConflictException("Ya existe un evento activo con ese título");
        }

        User organizer = findActiveUserOrThrow(currentUser.getId());
        CategoryEntity category = findActiveCategoryOrThrow(dto.getCategoryId());

        EventEntity event = new EventEntity();
        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription().trim());
        event.setModality(dto.getModality());
        event.setLocation(location);
        event.setVirtualUrl(virtualUrl);
        event.setCapacity(dto.getCapacity());
        event.setAvailableCapacity(dto.getCapacity());
        event.setRegistrationStartAt(dto.getRegistrationStartAt());
        event.setRegistrationEndAt(dto.getRegistrationEndAt());
        event.setStartAt(dto.getStartAt());
        event.setEndAt(dto.getEndAt());
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(organizer);
        event.setCategory(category);

        EventEntity savedEvent = eventRepository.save(event);
        return EventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional
    public EventResponseDTO update(Long id, UpdateEventDTO dto, UserDetailsImpl currentUser) {
        EventEntity event = findActiveEventOrThrow(id);

        validateOwnership(event, currentUser);

        validateDates(dto.getRegistrationStartAt(), dto.getRegistrationEndAt(), dto.getStartAt(), dto.getEndAt());

        String location = normalizeBlankToNull(dto.getLocation());
        String virtualUrl = normalizeBlankToNull(dto.getVirtualUrl());
        validateModality(dto.getModality(), location, virtualUrl);

        eventRepository.findByTitleIgnoreCaseAndDeletedFalse(dto.getTitle().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Ya existe otro evento activo con ese título");
                });

        CategoryEntity category = findActiveCategoryOrThrow(dto.getCategoryId());

        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription().trim());
        event.setModality(dto.getModality());
        event.setLocation(location);
        event.setVirtualUrl(virtualUrl);

        int capacityDifference = dto.getCapacity() - event.getCapacity();
        event.setCapacity(dto.getCapacity());
        event.setAvailableCapacity(event.getAvailableCapacity() + capacityDifference);

        event.setRegistrationStartAt(dto.getRegistrationStartAt());
        event.setRegistrationEndAt(dto.getRegistrationEndAt());
        event.setStartAt(dto.getStartAt());
        event.setEndAt(dto.getEndAt());
        event.setCategory(category);

        EventEntity updatedEvent = eventRepository.save(event);
        return EventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponseDTO updateStatus(Long id, UpdateEventStatusDTO dto, UserDetailsImpl currentUser) {
        EventEntity event = findActiveEventOrThrow(id);

        validateOwnership(event, currentUser);
        validateStatusTransition(event.getStatus(), dto.getStatus());

        event.setStatus(dto.getStatus());

        EventEntity updatedEvent = eventRepository.save(event);
        return EventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void delete(Long id, UserDetailsImpl currentUser) {
        EventEntity event = findActiveEventOrThrow(id);

        validateOwnership(event, currentUser);

        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.CANCELLED) {
            throw new ConflictException("No se puede eliminar un evento que ya ha sido publicado o finalizado");
        }


        event.setDeleted(true);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO findById(Long id) {
        return EventMapper.toResponse(findActiveEventOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> findAllPage(EventFilterDTO filter, PaginationDTO pagination) {
        Pageable pageable = createPageable(pagination);
        String titleFilter = (filter.getTitle() != null && !filter.getTitle().isBlank()) ? filter.getTitle().trim() : null;

        return eventRepository.findPageWithFilters(titleFilter, filter.getCategoryId(), filter.getStatus(), pageable)
                .map(EventMapper::toResponse);
    }


    private EventEntity findActiveEventOrThrow(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado"));
    }

    private User findActiveUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario organizador no encontrado"));
    }

    private CategoryEntity findActiveCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }

    private void validateOwnership(EventEntity event, UserDetailsImpl currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        if (event.getOrganizer() == null || event.getOrganizer().getId() == null) {
            throw new AccessDeniedException("El evento no tiene un organizador asignado");
        }

        if (!currentUser.getId().equals(event.getOrganizer().getId())) {
            throw new AccessDeniedException("Acceso denegado: No puedes modificar o eliminar eventos ajenos");
        }
    }

    private void validateDates(LocalDateTime regStart, LocalDateTime regEnd, LocalDateTime start, LocalDateTime end) {
        if (!regStart.isBefore(regEnd)) {
            throw new BadRequestException("La fecha de inicio de inscripción debe ser anterior a la de fin de inscripción");
        }
        if (regEnd.isAfter(start)) {
            throw new BadRequestException("El periodo de inscripción debe terminar antes o en el mismo momento que inicia el evento");
        }
        if (!start.isBefore(end)) {
            throw new BadRequestException("La fecha de inicio del evento debe ser anterior a la fecha de finalización");
        }
    }

    private void validateModality(EventModality modality, String location, String virtualUrl) {
        if (modality == EventModality.PRESENTIAL) {
            if (location == null || virtualUrl != null) {
                throw new BadRequestException("Un evento PRESENTIAL debe tener 'location' y NO debe tener 'virtualUrl'");
            }
        } else if (modality == EventModality.VIRTUAL) {
            if (virtualUrl == null || location != null) {
                throw new BadRequestException("Un evento VIRTUAL debe tener 'virtualUrl' y NO debe tener 'location'");
            }
        } else if (modality == EventModality.HYBRID) {
            if (location == null || virtualUrl == null) {
                throw new BadRequestException("Un evento HYBRID requiere especificar tanto 'location' como 'virtualUrl'");
            }
        }
    }

    private void validateStatusTransition(EventStatus current, EventStatus target) {
        if (current == target) {
            throw new BadRequestException("El evento ya se encuentra en el estado '" + target + "'");
        }

        boolean isValidTransition = switch (current) {
            case DRAFT -> target == EventStatus.PUBLISHED || target == EventStatus.CANCELLED;
            case PUBLISHED -> target == EventStatus.FINISHED || target == EventStatus.CANCELLED;
            case FINISHED, CANCELLED -> false;
        };

        if (!isValidTransition) {
            throw new BadRequestException(
                    "Transición de estado no permitida: no se puede pasar de '" + current + "' a '" + target + "'");
        }
    }

    private String normalizeBlankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private Pageable createPageable(PaginationDTO pagination) {
        String sortBy = (pagination.getSortBy() == null || pagination.getSortBy().isBlank()) ? "id" : pagination.getSortBy();
        Sort.Direction direction = "desc".equalsIgnoreCase(pagination.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by(direction, sortBy));
    }
}