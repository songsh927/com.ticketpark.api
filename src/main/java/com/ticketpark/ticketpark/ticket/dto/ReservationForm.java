package com.ticketpark.ticketpark.ticket.dto;

public class ReservationForm {

    private Integer member_idx;
    private Integer ticket_idx;
    private String ticket_member_uq;

    public ReservationForm(Integer member_idx, Integer ticket_idx){
        this.member_idx = member_idx;
        this.ticket_idx = ticket_idx;
    }

}
