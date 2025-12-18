package com.ticketpark.ticketpark.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO {
    private Integer ticket_idx;
    private String ticket_title;
    private String ticket_sub_title;
    private String ticket_title_image;
    private LocalDateTime ticket_open;
    private LocalDateTime ticket_close;
    private Integer ticket_price;
//    private TicketJsonInfoDTO ticket_info;
    private Map<String, Object> ticket_info;

}

//class TicketJsonInfoDTO{
//    private String location;
//    private String time;
//    private String discount;
//}