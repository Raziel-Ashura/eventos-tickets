package com.eventos.mstickets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface TicketRepository extends JpaRepository<Ticket,Long>{List<Ticket> findByEventoId(Long id);List<Ticket> findByUsuarioId(String id);long countByReservaId(Long id);}
