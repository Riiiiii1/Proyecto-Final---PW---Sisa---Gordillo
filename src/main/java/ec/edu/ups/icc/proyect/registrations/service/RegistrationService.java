package ec.edu.ups.icc.proyect.registrations.service;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.CreateRegistrationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.RegistrationResponseDTO;
import ec.edu.ups.icc.proyect.registrations.dto.UpdateRegistrationStatusDTO;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import org.springframework.data.domain.Page;

public interface RegistrationService {

    RegistrationResponseDTO create(CreateRegistrationDTO dto, UserDetailsImpl currentUser);

    RegistrationResponseDTO updateStatus(Long id, UpdateRegistrationStatusDTO dto, UserDetailsImpl currentUser);

    RegistrationResponseDTO findById(Long id, UserDetailsImpl currentUser);

    Page<RegistrationResponseDTO> findMine(UserDetailsImpl currentUser, PaginationDTO pagination);

    Page<RegistrationResponseDTO> findByEvent(Long eventId, UserDetailsImpl currentUser, PaginationDTO pagination);
}