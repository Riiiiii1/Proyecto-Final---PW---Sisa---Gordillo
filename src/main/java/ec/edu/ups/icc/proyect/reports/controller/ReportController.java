package ec.edu.ups.icc.proyect.reports.controller;

import ec.edu.ups.icc.proyect.reports.service.ReportService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints para la generación de reportes y certificados")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Exportar PDF de inscritos por evento", description = "Permite al ADMIN o al ORGANIZER propietario descargar el listado de inscritos en formato PDF.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/reports/events/{eventId}/registrations.pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> eventRegistrationsPdf(@PathVariable Long eventId,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {
        byte[] pdf = reportService.generateEventRegistrationsPdf(eventId, currentUser);
        return fileResponse(pdf, MediaType.APPLICATION_PDF, "registrations-event-" + eventId + ".pdf");
    }

    @Operation(summary = "Exportar Excel de inscritos por evento", description = "Permite al ADMIN o al ORGANIZER propietario descargar el listado de inscritos en formato Excel.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel generado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/reports/events/{eventId}/registrations.xlsx")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> eventRegistrationsExcel(@PathVariable Long eventId,
                                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {
        byte[] excel = reportService.generateEventRegistrationsExcel(eventId, currentUser);
        MediaType xlsxType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return fileResponse(excel, xlsxType, "registrations-event-" + eventId + ".xlsx");
    }

    @Operation(summary = "Descargar certificado de inscripción", description = "Permite al PARTICIPANT propietario descargar el comprobante de una inscripción confirmada.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificado generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "La inscripción no está confirmada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es el propietario)"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @GetMapping("/registrations/{id}/certificate.pdf")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<byte[]> registrationCertificate(@PathVariable Long id,
                                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser) {
        byte[] pdf = reportService.generateCertificate(id, currentUser);
        return fileResponse(pdf, MediaType.APPLICATION_PDF, "certificate-registration-" + id + ".pdf");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] content, MediaType mediaType, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}