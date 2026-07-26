package ec.edu.ups.icc.proyect.events.service;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.events.dto.CreateEventDTO;
import ec.edu.ups.icc.proyect.events.dto.EventFilterDTO;
import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import ec.edu.ups.icc.proyect.events.dto.UpdateEventDTO;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public interface EventService {

    EventResponseDTO create(CreateEventDTO dto, UserDetailsImpl currentUser);

    EventResponseDTO update(Long id, UpdateEventDTO dto, UserDetailsImpl currentUser);

    void delete(Long id, UserDetailsImpl currentUser);

    EventResponseDTO findById(Long id);

    Page<EventResponseDTO> findAllPage(EventFilterDTO filter, PaginationDTO pagination);

    Slice<EventResponseDTO> findAllSlice(EventFilterDTO filter, PaginationDTO pagination);
}