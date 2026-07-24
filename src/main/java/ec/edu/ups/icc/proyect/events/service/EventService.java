package ec.edu.ups.icc.proyect.events.service;

import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    EventResponseDTO getEventById(Long id);

    Page<EventResponseDTO> getAllEvents(Pageable pageable);

}