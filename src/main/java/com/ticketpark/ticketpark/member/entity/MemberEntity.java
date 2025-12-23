package com.ticketpark.ticketpark.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`MEMBER`")
@Getter
@Setter
@NoArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer member_idx;

    @Column(nullable = false, length = 50, unique = true, updatable = false) //length=20
    private String member_id;

    @Column(nullable = false, length = 100, unique = false, updatable = false) //length=500
    private String member_pw;

    @Column(nullable = false, length = 100, unique = true, updatable = false) //length=70
    private String member_email;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime created_at;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updated_at;

    @Builder
    public MemberEntity(String member_id, String member_pw, String member_email, LocalDateTime created_at){
        this.member_id = member_id;
        this.member_pw = member_pw;
        this.member_email = member_email;
        this.created_at = created_at;
    }

}
