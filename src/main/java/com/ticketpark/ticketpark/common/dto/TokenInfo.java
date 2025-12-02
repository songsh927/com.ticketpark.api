package com.ticketpark.ticketpark.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
}
