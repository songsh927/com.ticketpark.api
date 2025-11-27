package com.ticketpark.ticketpark.member.dto;

import com.ticketpark.ticketpark.member.entity.MemberEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinDTO {
    private String id;
    private String password;
    private String email;

    public MemberEntity toMember(){
        return MemberEntity.builder()
                .member_pw(password)
                .member_id(id)
                .member_email(email)
                .created_at(LocalDateTime.now())
                .build();
    }

}
