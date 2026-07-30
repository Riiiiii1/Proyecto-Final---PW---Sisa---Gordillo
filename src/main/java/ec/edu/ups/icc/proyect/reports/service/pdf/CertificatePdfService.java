package ec.edu.ups.icc.proyect.reports.service.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificatePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color BRAND_COLOR = new Color(41, 65, 148);

    public byte[] generate(RegistrationEntity registration) {
        Document document = new Document(PageSize.A4, 60, 60, 80, 60);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_COLOR);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            Paragraph title = new Paragraph("Comprobante de Inscripcion", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30f);
            document.add(title);

            String participantName = registration.getParticipant().getFirstName() + " "
                    + registration.getParticipant().getLastName();

            Paragraph body = new Paragraph();
            body.setFont(bodyFont);
            body.setAlignment(Element.ALIGN_JUSTIFIED);
            body.add("Se certifica que ");
            body.add(new Chunk(participantName, boldFont));
            body.add(", identificado con el correo ");
            body.add(new Chunk(registration.getParticipant().getEmail(), boldFont));
            body.add(", se encuentra inscrito y con estado confirmado en el evento ");
            body.add(new Chunk(registration.getEvent().getTitle(), boldFont));
            body.add(", organizado a traves de la plataforma Academic Events API.");
            body.setSpacingAfter(20f);
            document.add(body);

            Paragraph details = new Paragraph();
            details.setFont(bodyFont);
            details.add("Codigo de inscripcion: " + registration.getRegistrationCode() + "\n");
            details.add("Fecha de inscripcion: " + registration.getRegisteredAt().format(DATE_FORMATTER) + "\n");
            if (registration.getConfirmedAt() != null) {
                details.add("Fecha de confirmacion: " + registration.getConfirmedAt().format(DATE_FORMATTER) + "\n");
            }
            details.setSpacingAfter(40f);
            document.add(details);

            Paragraph footer = new Paragraph(
                    "Este documento fue generado automaticamente por Academic Events API y no requiere firma fisica.",
                    smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el certificado PDF", e);
        }

        return outputStream.toByteArray();
    }
}