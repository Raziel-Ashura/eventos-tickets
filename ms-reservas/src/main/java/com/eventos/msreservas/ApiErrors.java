package com.eventos.msreservas;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
@RestControllerAdvice class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,Object>> status(ResponseStatusException e) {return ResponseEntity.status(e.getStatusCode()).body(Map.of("codigo",e.getStatusCode().value(),"mensaje",e.getReason()==null?"Error":e.getReason()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e) {return ResponseEntity.badRequest().body(Map.of("codigo",400,"mensaje","Datos invalidos"));}
}
