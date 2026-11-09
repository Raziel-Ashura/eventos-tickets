package com.eventos.msreservas;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import java.time.LocalDateTime;
@Entity public class Reserva {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long eventoId;
 public String usuarioId;
 public int cantidad;
 public long total;
 public String estado="CONFIRMADA";
 public LocalDateTime creadaEn=LocalDateTime.now();
}
