package com.ticketpark.ticketpark.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`MEMBER`")
@Getter
@NoArgsConstructor
public class MemberEntity {

    @Id
    private Integer member_idx;

    @Column(nullable = false, length = 50, unique = true, updatable = false) //length=20
    private String member_id;

    @Column(nullable = false, length = 100, unique = false, updatable = false) //length=500
    private String member_pw;

    @Column(nullable = false, length = 100, unique = true, updatable = false) //length=70
    private String member_email;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime created_at;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updated_at;
}
