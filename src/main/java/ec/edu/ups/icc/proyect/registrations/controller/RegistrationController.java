package ec.edu.ups.icc.proyect.registrations.controller;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.CreateRegistrationDTO;
import ec.edu.ups.icc.proyect.registrations.dto.RegistrationResponseDTO;
import ec.edu.ups.icc.proyect.registrations.dto.UpdateRegistrationStatusDTO;
import ec.edu.ups.icc.proyect.registrations.service.RegistrationService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registrations")
@Tag(name = "Registrations", description = "API para la gestión de inscripciones a eventos")
@SecurityRequirement(name = "bearerAuth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Operation(summary = "Crear una inscripción", description = "Permite a un PARTICIPANT inscribirse en un evento publicado con cupos disponibles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscripción creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación o periodo de inscripción no activo"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe una inscripción o no hay cupos disponibles")
    })
    @PostMapping
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<RegistrationResponseDTO> create(
            @Valid @RequestBody CreateRegistrationDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        RegistrationResponseDTO created = registrationService.create(dto, currentUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar el estado de una inscripción", description = "Permite confirmar, rechazar o cancelar una inscripción según el rol y la propiedad del recurso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado no permitida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada"),
            @ApiResponse(responseCode = "409", description = "El evento ya no tiene cupos disponibles")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRegistrationStatusDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return ResponseEntity.ok(registrationService.updateStatus(id, dto, currentUser));
    }

    @Operation(summary = "Obtener una inscripción por ID", description = "Consulta los detalles de una inscripción. Accesible por el participante propietario, el organizador del evento o un ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscripción encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDTO> findById(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return ResponseEntity.ok(registrationService.findById(id, currentUser));
    }

    @Operation(summary = "Listar mis inscripciones (Paginado)", description = "Devuelve las inscripciones del participante autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscripciones recuperada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    @GetMapping("/mine")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<Page<RegistrationResponseDTO>> findMine(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @ModelAttribute PaginationDTO pagination) {

        return ResponseEntity.ok(registrationService.findMine(currentUser, pagination));
    }

    @Operation(summary = "Listar inscripciones por evento (Paginado)", description = "Permite al ADMIN o al ORGANIZER propietario consultar las inscripciones de un evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscripciones recuperada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<RegistrationResponseDTO>> findByEvent(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @ModelAttribute PaginationDTO pagination) {

        return ResponseEntity.ok(registrationService.findByEvent(eventId, currentUser, pagination));
    }
}