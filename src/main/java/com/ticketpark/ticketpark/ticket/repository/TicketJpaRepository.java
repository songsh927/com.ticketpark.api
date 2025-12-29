package com.ticketpark.ticketpark.ticket.repository;

import com.ticketpark.ticketpark.ticket.entity.TicketMainEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketJpaRepository extends JpaRepository<TicketMainEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tm.ticket_qty > 0 FROM TicketMainEntity tm WHERE tm.ticket_idx = :ticket_idx")
    boolean findByIdxWithPessimisticLock(@Param("ticket_idx") Integer ticket_idx);

    @Modifying
    @Query("update TicketMainEntity tm set tm.ticket_qty = tm.ticket_qty - 1 where tm.ticket_idx = :ticket_idx")
    void decreaseTicketQty(@Param("ticket_idx") Integer ticket_idx);

}
