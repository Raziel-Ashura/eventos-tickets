package com.eventos.mseventos;
import jakarta.validation.Valid; import jakarta.validation.constraints.Min; import org.springframework.http.*; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.util.*;
@RestController @RequestMapping("/api/eventos") public class EventoController {
 private final EventoRepository repo;
 public EventoController(EventoRepository repo){this.repo=repo;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Evento crear(@Valid @RequestBody Evento e){e.id=null;e.reservados=0;return repo.save(e);}
 @GetMapping public List<Evento> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Evento buscar(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no existe"));}
 @PutMapping("/{id}") public Evento editar(@PathVariable Long id,@Valid @RequestBody Evento entrada){Evento e=buscar(id);if(entrada.capacidad<e.reservados)throw new ResponseStatusException(HttpStatus.CONFLICT,"Capacidad inferior a reservas");e.nombre=entrada.nombre;e.fecha=entrada.fecha;e.lugar=entrada.lugar;e.capacidad=entrada.capacidad;e.precio=entrada.precio;return repo.save(e);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){Evento e=buscar(id);if(e.reservados>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Evento tiene reservas");repo.delete(e);}
 @PostMapping("/{id}/reservar") @Transactional public Map<String,Object> reservar(@PathVariable Long id,@RequestParam @Min(1) int cantidad){if(repo.reservar(id,cantidad)==0){buscar(id);throw new ResponseStatusException(HttpStatus.CONFLICT,"Sin cupos suficientes");}return Map.of("eventoId",id,"cantidad",cantidad,"precioUnitario",buscar(id).precio);}
}
