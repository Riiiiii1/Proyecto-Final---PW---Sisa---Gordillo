package ec.edu.ups.icc.proyect.events.controller;

import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.events.dto.CreateEventDTO;
import ec.edu.ups.icc.proyect.events.dto.EventFilterDTO;
import ec.edu.ups.icc.proyect.events.dto.EventResponseDTO;
import ec.edu.ups.icc.proyect.events.dto.UpdateEventDTO;
import ec.edu.ups.icc.proyect.events.service.EventService;
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
@RequestMapping("/api/events")
@Tag(name = "Events", description = "API para la gestión de eventos académicos")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Crear un nuevo evento", description = "Permite a un ADMIN u ORGANIZER registrar un evento nuevo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación o reglas de negocio"),
            @ApiResponse(responseCode = "409", description = "Conflicto (ej. título duplicado)")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventResponseDTO> create(
            @Valid @RequestBody CreateEventDTO createEventDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        EventResponseDTO created = eventService.create(createEventDTO, currentUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un evento", description = "Permite a un ADMIN o al ORGANIZER propietario modificar un evento existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventDTO updateEventDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        EventResponseDTO updated = eventService.update(id, updateEventDTO, currentUser);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar un evento (lógico)", description = "Permite a un ADMIN o al ORGANIZER propietario eliminar un evento en estado DRAFT o CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "409", description = "El evento ya está publicado o tiene inscripciones")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {

        eventService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener un evento por ID", description = "Consulta los detalles de un evento específico.")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @Operation(summary = "Listar eventos (Paginado)", description = "Devuelve una lista paginada de eventos aplicando filtros dinámicos.")
    @ApiResponse(responseCode = "200", description = "Lista de eventos recuperada exitosamente")
    @GetMapping("/page")
    public ResponseEntity<Page<EventResponseDTO>> findAllPage(
            @ModelAttribute EventFilterDTO filter,
            @Valid @ModelAttribute PaginationDTO pagination) {

        return ResponseEntity.ok(eventService.findAllPage(filter, pagination));
    }
}