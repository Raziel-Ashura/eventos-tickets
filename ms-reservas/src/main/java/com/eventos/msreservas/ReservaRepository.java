package com.eventos.msreservas;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
interface ReservaRepository extends JpaRepository<Reserva,Long>{List<Reserva> findByEventoId(Long id);}
