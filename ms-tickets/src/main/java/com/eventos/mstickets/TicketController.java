package com.eventos.mstickets;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.client.*; import org.springframework.web.server.ResponseStatusException; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*; import java.io.IOException; import java.nio.file.Path;
@RestController @RequestMapping("/api/tickets") public class TicketController {
 record Nuevo(@NotNull Long reservaId,@NotBlank String nombreAsistente){}
 private final TicketRepository repo;private final RestClient client;private final TicketPdfService pdfService;private final TicketS3Service s3Service;
 public TicketController(TicketRepository repo,RestClient.Builder builder,@Value("${services.reservas.url}") String url,TicketPdfService pdfService,TicketS3Service s3Service){this.repo=repo;this.pdfService=pdfService;this.s3Service=s3Service;client=builder.baseUrl(url).build();}
 @PostMapping("/registrar") @ResponseStatus(HttpStatus.CREATED) public Ticket registrar(@Valid @RequestBody Nuevo n){
  Map<?,?> r;
  try{r=client.get().uri("/api/reservas/{id}",n.reservaId()).retrieve().body(Map.class);}
  catch(HttpClientErrorException e){throw new ResponseStatusException(e.getStatusCode(),"Reserva no existe");}
  catch(RestClientException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Servicio reservas no disponible");}
  if(!"CONFIRMADA".equals(r.get("estado")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Reserva no confirmada");
  long existentes=repo.countByReservaId(n.reservaId());int cantidad=((Number)r.get("cantidad")).intValue();if(existentes>=cantidad)throw new ResponseStatusException(HttpStatus.CONFLICT,"Todos los tickets ya registrados");
  Ticket t=new Ticket();t.reservaId=n.reservaId();t.eventoId=((Number)r.get("eventoId")).longValue();t.usuarioId=(String)r.get("usuarioId");t.numero=(int)existentes+1;t.nombreAsistente=n.nombreAsistente();return repo.save(t);
 }
 @GetMapping("/{id}") public Ticket buscar(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket no existe"));}
 @GetMapping("/evento/{id}") public List<Ticket> porEvento(@PathVariable Long id){return repo.findByEventoId(id);}
 @GetMapping("/usuario/{id}") public List<Ticket> porUsuario(@PathVariable String id){return repo.findByUsuarioId(id);}

 @PostMapping("/{id}/generar-pdf")
 public Map<String,Object> generarPdf(@PathVariable Long id) {
  Ticket ticket=repo.findById(id).orElseThrow(
    ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket no existe")
  );
  try {
   Path archivo=pdfService.generar(ticket);
   String s3Key=s3Service.subir(ticket,archivo);
   ticket.s3Key=s3Key;
   repo.save(ticket);
   return Map.of(
     "ticketId",ticket.id,
     "archivo",archivo.getFileName().toString(),
     "s3Key",s3Key,
     "estado","PDF_SUBIDO_A_S3"
   );
  } catch(IOException e) {
   throw new ResponseStatusException(
     HttpStatus.INTERNAL_SERVER_ERROR,"No se pudo generar el PDF",e
   );
  }
 }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        Ticket ticket = repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket no existe"
            )
        );

        if (ticket.s3Key == null || ticket.s3Key.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El ticket todavía no tiene un PDF en S3"
            );
        }

        try {
            byte[] pdf = s3Service.descargar(ticket.s3Key);

            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(
                    org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"ticket-" + ticket.id + ".pdf\""
                )
                .body(pdf);

        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El archivo no existe en S3",
                e
            );
        } catch (software.amazon.awssdk.core.exception.SdkException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No fue posible recuperar el archivo desde S3",
                e
            );
        }
    }

    record Actualizar(@NotBlank String nombreAsistente) {}

    @PutMapping("/{id}")
    public Ticket actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Actualizar datos) {

        Ticket ticket = repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket no existe"
            )
        );

        String nombreAnterior = ticket.nombreAsistente;
        ticket.nombreAsistente = datos.nombreAsistente();

        if (ticket.s3Key != null && !ticket.s3Key.isBlank()) {
            try {
                Path archivo = pdfService.generar(ticket);
                String nuevaKey = s3Service.subir(ticket, archivo);
                ticket.s3Key = nuevaKey;

            } catch (IOException e) {
                ticket.nombreAsistente = nombreAnterior;
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo actualizar el PDF",
                    e
                );

            } catch (software.amazon.awssdk.core.exception.SdkException e) {
                ticket.nombreAsistente = nombreAnterior;
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo actualizar el PDF en S3",
                    e
                );
            }
        }

        return repo.save(ticket);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        Ticket ticket = repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket no existe"
            )
        );

        if (ticket.s3Key != null && !ticket.s3Key.isBlank()) {
            try {
                s3Service.eliminar(ticket.s3Key);
            } catch (software.amazon.awssdk.core.exception.SdkException e) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo eliminar el PDF de S3",
                    e
                );
            }
        }

        try {
            java.nio.file.Files.deleteIfExists(
                java.nio.file.Path.of("/app/tickets", "ticket-" + id + ".pdf")
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo eliminar el PDF de EFS",
                e
            );
        }

        repo.delete(ticket);
    }

    @GetMapping("/estadisticas/evento/{id}")
    public Map<String, Object> estadisticasEvento(@PathVariable Long id) {

        List<Ticket> tickets = repo.findByEventoId(id);

        long registrados = tickets.stream()
            .filter(t -> "REGISTRADO".equals(t.estado))
            .count();

        return Map.of(
            "eventoId", id,
            "totalTickets", tickets.size(),
            "registrados", registrados
        );
    }
}
