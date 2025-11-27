package com.ticketpark.ticketpark.member.dto;

import lombok.Data;

@Data
public class UpdateDTO {
    private Integer idx;
    private String email;
    private String password;
}
