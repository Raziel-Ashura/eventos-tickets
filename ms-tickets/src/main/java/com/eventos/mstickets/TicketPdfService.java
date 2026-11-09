package com.eventos.mstickets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TicketPdfService {

    private final Path storagePath;

    public TicketPdfService(
            @Value("${tickets.storage.path:/app/tickets}") String path) {
        this.storagePath = Path.of(path);
    }

    public Path generar(Ticket ticket) throws IOException {

        Files.createDirectories(storagePath);

        Path archivo = storagePath.resolve(
                "ticket-" + ticket.id + ".pdf"
        );

        try (PDDocument documento = new PDDocument()) {

            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido =
                         new PDPageContentStream(documento, pagina)) {

                contenido.beginText();

                contenido.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                        20
                );

                contenido.newLineAtOffset(50, 750);
                contenido.showText("EVENTOS TICKETS");

                contenido.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );

                contenido.newLineAtOffset(0, -45);
                contenido.showText("Ticket N°: " + ticket.id);

                contenido.newLineAtOffset(0, -25);
                contenido.showText("Evento ID: " + ticket.eventoId);

                contenido.newLineAtOffset(0, -25);
                contenido.showText("Reserva ID: " + ticket.reservaId);

                contenido.newLineAtOffset(0, -25);
                contenido.showText("Usuario: " + textoSeguro(ticket.usuarioId));

                contenido.newLineAtOffset(0, -25);
                contenido.showText(
                        "Asistente: " + textoSeguro(ticket.nombreAsistente)
                );

                contenido.newLineAtOffset(0, -25);
                contenido.showText("Estado: " + textoSeguro(ticket.estado));

                contenido.endText();
            }

            documento.save(archivo.toFile());
        }

        return archivo;
    }

    private String textoSeguro(String texto) {
        if (texto == null) {
            return "";
        }

        // Las fuentes Standard 14 utilizan una codificación limitada.
        return texto.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
