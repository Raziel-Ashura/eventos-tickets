package com.eventos.mstickets;
import jakarta.persistence.*;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"reservaId","numero"})) public class Ticket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long reservaId; public Long eventoId; public String usuarioId; public int numero;
 public String nombreAsistente; public String estado="REGISTRADO";
 @Column(length=1024) public String s3Key;
}
