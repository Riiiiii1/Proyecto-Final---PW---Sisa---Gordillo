package ec.edu.ups.icc.proyect.reports.service.excel;

import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistrationsExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generate(EventEntity event, List<RegistrationEntity> registrations) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inscritos");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Listado de Inscritos - " + event.getTitle());
            titleCell.setCellStyle(titleStyle);

            String[] headers = {"ID", "Nombre", "Email", "Estado", "Fecha Registro", "Fecha Confirmación"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 3;
            for (RegistrationEntity registration : registrations) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(registration.getId());
                row.createCell(1).setCellValue(
                        registration.getParticipant().getFirstName() + " " + registration.getParticipant().getLastName());
                row.createCell(2).setCellValue(registration.getParticipant().getEmail());
                row.createCell(3).setCellValue(registration.getStatus().name());
                row.createCell(4).setCellValue(
                        registration.getRegisteredAt() != null ? registration.getRegisteredAt().format(DATE_FORMATTER) : "-");
                row.createCell(5).setCellValue(
                        registration.getConfirmedAt() != null ? registration.getConfirmedAt().format(DATE_FORMATTER) : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el reporte Excel de inscripciones", e);
        }
    }
}