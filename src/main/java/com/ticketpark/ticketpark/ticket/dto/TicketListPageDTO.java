package com.ticketpark.ticketpark.ticket.dto;

import com.ticketpark.ticketpark.ticket.entity.TicketMainEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketListPageDTO {

    private Integer ticket_idx;
    private String ticket_title;
    private String ticket_sub_title;
    private String ticket_title_image;
    private LocalDateTime ticket_open;
    private LocalDateTime ticket_close;

    public static TicketListPageDTO from(TicketMainEntity ticketMainEntity){
        TicketListPageDTO data = new TicketListPageDTO();

        data.setTicket_idx(ticketMainEntity.getTicket_idx());
        data.setTicket_title(ticketMainEntity.getTicket_title());
        data.setTicket_sub_title(ticketMainEntity.getTicket_sub_title());
        data.setTicket_title_image(ticketMainEntity.getTicket_title_image());
        data.setTicket_open(ticketMainEntity.getTicket_open());
        data.setTicket_close(ticketMainEntity.getTicket_close());

        return data;
    }

}
