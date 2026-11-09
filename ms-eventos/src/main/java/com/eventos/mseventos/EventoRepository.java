package com.eventos.mseventos;
import org.springframework.data.jpa.repository.*; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.repository.query.Param; import org.springframework.data.jpa.repository.Modifying;
interface EventoRepository extends JpaRepository<Evento,Long> {
 @Modifying @Query("update Evento e set e.reservados=e.reservados+:cantidad where e.id=:id and e.reservados+:cantidad<=e.capacidad")
 int reservar(@Param("id") Long id,@Param("cantidad") int cantidad);
}
