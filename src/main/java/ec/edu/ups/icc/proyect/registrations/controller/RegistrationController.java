package ec.edu.ups.icc.proyect.registrations.controller;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.CreateRegistrationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.RegistrationResponseDTO;
import ec.edu.ups.icc.proyect.registrations.dto.UpdateRegistrationStatusDTO;
import ec.edu.ups.icc.proyect.registrations.service.RegistrationService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<RegistrationResponseDTO> create(
            @Valid @RequestBody CreateRegistrationDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        RegistrationResponseDTO created = registrationService.create(dto, currentUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRegistrationStatusDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return ResponseEntity.ok(registrationService.updateStatus(id, dto, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return ResponseEntity.ok(registrationService.findById(id, currentUser));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<Page<RegistrationResponseDTO>> findMine(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @ModelAttribute PaginationDTO pagination) {

        return ResponseEntity.ok(registrationService.findMine(currentUser, pagination));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<RegistrationResponseDTO>> findByEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @ModelAttribute PaginationDTO pagination) {

        return ResponseEntity.ok(registrationService.findByEvent(eventId, currentUser, pagination));
    }
}