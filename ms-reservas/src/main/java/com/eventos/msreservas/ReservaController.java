package com.eventos.msreservas;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.client.*; import org.springframework.web.server.ResponseStatusException; import jakarta.validation.*; import jakarta.validation.constraints.*; import java.util.*;
@RestController @RequestMapping("/api/reservas") public class ReservaController {
 record Nueva(@NotNull Long eventoId,@NotBlank String usuarioId,@Min(1) int cantidad){}
 private final ReservaRepository repo; private final RestClient client;
 public ReservaController(ReservaRepository repo,RestClient.Builder builder,@Value("${services.eventos.url}") String url){this.repo=repo;this.client=builder.baseUrl(url).build();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Reserva crear(@Valid @RequestBody Nueva n){
  Map<?,?> r;
  try{r=client.post().uri("/api/eventos/{id}/reservar?cantidad={cantidad}",n.eventoId(),n.cantidad()).retrieve().body(Map.class);}
  catch(HttpClientErrorException e){throw new ResponseStatusException(e.getStatusCode(),"Evento inexistente o sin cupos");}
  catch(RestClientException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Servicio eventos no disponible");}
  Reserva reserva=new Reserva();reserva.eventoId=n.eventoId();reserva.usuarioId=n.usuarioId();reserva.cantidad=n.cantidad();reserva.total=((Number)r.get("precioUnitario")).longValue()*n.cantidad();return repo.save(reserva);
 }
 @GetMapping public List<Reserva> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Reserva buscar(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Reserva no existe"));}
 @GetMapping("/evento/{id}") public List<Reserva> porEvento(@PathVariable Long id){return repo.findByEventoId(id);}
}
