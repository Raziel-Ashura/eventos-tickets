package com.eventos.mseventos;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import java.time.LocalDateTime;
@Entity public class Evento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String nombre;
 @NotNull @Future public LocalDateTime fecha;
 @NotBlank public String lugar;
 @Min(1) public int capacidad;
 @PositiveOrZero public long precio;
 public int reservados=0;
}
