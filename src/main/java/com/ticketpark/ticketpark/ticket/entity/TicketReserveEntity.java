package com.ticketpark.ticketpark.ticket.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Table(name = "`TICKET_RESERVE`")
public class TicketReserveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false, updatable = false)
    private Integer member_idx;

    @Column(nullable = false, updatable = false)
    private Integer ticket_idx;

    @Column(nullable = false, updatable = false, unique = true)
    private String ticket_member_uq;

    @Builder
    public TicketReserveEntity(Integer ticket_idx, Integer member_idx){
        this.member_idx = member_idx;
        this.ticket_idx = ticket_idx;
        this.ticket_member_uq = ticket_idx+"-"+member_idx;
    }
}
