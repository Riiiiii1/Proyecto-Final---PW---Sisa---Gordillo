package ec.edu.ups.icc.proyect.reports.service.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistrationsPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color HEADER_COLOR = new Color(41, 65, 148);

    public byte[] generate(EventEntity event, List<RegistrationEntity> registrations) {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Listado de Inscritos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Evento: " + event.getTitle(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 3f, 2f, 2.5f, 2.5f});

            String[] headers = {"ID", "Participante", "Email", "Estado", "Fecha Registro", "Fecha Confirmación"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(6f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (RegistrationEntity registration : registrations) {
                table.addCell(cellOf(String.valueOf(registration.getId()), cellFont));
                table.addCell(cellOf(
                        registration.getParticipant().getFirstName() + " " + registration.getParticipant().getLastName(),
                        cellFont));
                table.addCell(cellOf(registration.getParticipant().getEmail(), cellFont));
                table.addCell(cellOf(registration.getStatus().name(), cellFont));
                table.addCell(cellOf(
                        registration.getRegisteredAt() != null ? registration.getRegisteredAt().format(DATE_FORMATTER) : "-",
                        cellFont));
                table.addCell(cellOf(
                        registration.getConfirmedAt() != null ? registration.getConfirmedAt().format(DATE_FORMATTER) : "-",
                        cellFont));
            }

            document.add(table);

            Paragraph footer = new Paragraph("Total de inscritos: " + registrations.size(), subtitleFont);
            footer.setSpacingBefore(15f);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte PDF de inscripciones", e);
        }

        return outputStream.toByteArray();
    }

    private PdfPCell cellOf(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        return cell;
    }
}