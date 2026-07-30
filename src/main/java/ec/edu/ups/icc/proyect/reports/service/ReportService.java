package ec.edu.ups.icc.proyect.reports.service;

import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.ForbiddenException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.repository.EventRepository;
import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import ec.edu.ups.icc.proyect.registrations.enums.RegistrationStatus;
import ec.edu.ups.icc.proyect.registrations.repository.RegistrationRepository;
import ec.edu.ups.icc.proyect.reports.service.excel.RegistrationsExcelService;
import ec.edu.ups.icc.proyect.reports.service.pdf.CertificatePdfService;
import ec.edu.ups.icc.proyect.reports.service.pdf.RegistrationsPdfService;
import ec.edu.ups.icc.proyect.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationsPdfService registrationsPdfService;
    private final RegistrationsExcelService registrationsExcelService;
    private final CertificatePdfService certificatePdfService;

    @Transactional(readOnly = true)
    public byte[] generateEventRegistrationsPdf(Long eventId, UserDetailsImpl currentUser) {
        EventEntity event = findActiveEventOrThrow(eventId);
        validateEventOwnership(event, currentUser);

        List<RegistrationEntity> registrations = registrationRepository
                .findByEvent_Id(eventId, Pageable.unpaged())
                .getContent();

        return registrationsPdfService.generate(event, registrations);
    }

    @Transactional(readOnly = true)
    public byte[] generateEventRegistrationsExcel(Long eventId, UserDetailsImpl currentUser) {
        EventEntity event = findActiveEventOrThrow(eventId);
        validateEventOwnership(event, currentUser);

        List<RegistrationEntity> registrations = registrationRepository
                .findByEvent_Id(eventId, Pageable.unpaged())
                .getContent();

        return registrationsExcelService.generate(event, registrations);
    }

    @Transactional(readOnly = true)
    public byte[] generateCertificate(Long registrationId, UserDetailsImpl currentUser) {
        RegistrationEntity registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException("Inscripción con id " + registrationId + " no encontrada"));

        boolean isOwner = registration.getParticipant().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("No tienes permiso para descargar este certificado");
        }

        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new BadRequestException("Solo se puede generar el certificado de inscripciones en estado CONFIRMED");
        }

        return certificatePdfService.generate(registration);
    }

    private EventEntity findActiveEventOrThrow(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado"));
    }

    private void validateEventOwnership(EventEntity event, UserDetailsImpl currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUser.getId()))) {
            throw new ForbiddenException("No tienes permiso para generar reportes de este evento");
        }
    }
}