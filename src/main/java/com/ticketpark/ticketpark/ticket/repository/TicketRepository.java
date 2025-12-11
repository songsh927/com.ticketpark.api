package com.ticketpark.ticketpark.ticket.repository;

import com.ticketpark.ticketpark.ticket.dto.TicketDetailDTO;
import com.ticketpark.ticketpark.ticket.dto.TicketListPageDTO;
import com.ticketpark.ticketpark.ticket.entity.TicketMainEntity;
import com.ticketpark.ticketpark.ticket.entity.TicketReserveEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {

    @PersistenceContext
    private EntityManager em;

    public Page<TicketListPageDTO> findAllByPage(Pageable pageable){

        String contentQuery = "SELECT tm FROM TicketMainEntity tm WHERE tm.ticket_close > NOW()";

        List<TicketMainEntity> ticketList = em.createQuery(contentQuery, TicketMainEntity.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countQuery = "SELECT COUNT(tm) FROM TicketMainEntity tm WHERE tm.ticket_close > NOW()";

        Long total = em.createQuery(countQuery, Long.class).getSingleResult();

        return new PageImpl<>(ticketList.stream().map(ticket -> TicketListPageDTO.from(ticket)).collect(Collectors.toList()), pageable, total);

    }

    public Page<TicketListPageDTO> findAllByOption(Pageable pageable, String searchType, String searchValue){

        String contentQuery = "SELECT tm FROM TicketMainEntity tm WHERE tm.ticket_title LIKE CONCAT('%', :searchValue,'%') AND tm.ticket_close > NOW()";

        List<TicketMainEntity> ticketList = em.createQuery(contentQuery, TicketMainEntity.class)
                .setParameter("searchValue", searchValue)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        String countQuery = "SELECT COUNT(tm) FROM TicketMainEntity tm WHERE tm.ticket_title LIKE CONCAT('%', :searchValue,'%') AND tm.ticket_close > NOW()";

        Long total = em.createQuery(countQuery, Long.class)
                .setParameter("searchValue", searchValue)
                .getSingleResult();

        return new PageImpl<>(ticketList.stream().map(ticket -> TicketListPageDTO.from(ticket)).collect(Collectors.toList()), pageable, total);

    }

    public Optional<TicketDetailDTO> findOneByIdx(Integer ticket_idx){

        String query = "SELECT NEW TicketDetailDTO(" +
                "tm.ticket_idx, tm.ticket_title, tm.ticket_sub_title, tm.ticket_title_image, tm.ticket_open, tm.ticket_close, " +
                "td.ticket_price, td.ticket_info) " +
                "FROM TicketMainEntity tm LEFT JOIN tm.ticketDetail td " +
                "WHERE tm.ticket_idx = :ticket_idx";

        List<TicketDetailDTO> ticketInfo =  em.createQuery(query, TicketDetailDTO.class)
                .setParameter("ticket_idx", ticket_idx)
                .getResultList();

        return ticketInfo.stream().findAny();

    }

    public void createReservation(TicketReserveEntity reservationForm){
        em.persist(reservationForm);
    }

}
