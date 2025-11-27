package com.ticketpark.ticketpark.member.dto;

import com.ticketpark.ticketpark.member.entity.MemberEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetMemberInfoDTO {
    private Integer idx;
    private String id;
    private String email;
    private LocalDateTime createdAt;

    public GetMemberInfoDTO(MemberEntity memberEntity) {
        this.idx = memberEntity.getMember_idx();
        this.id = memberEntity.getMember_id();
        this.email = memberEntity.getMember_email();
        this.createdAt = memberEntity.getCreated_at();
    }
}
