package com.ticketpark.ticketpark.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "`TICKET_MAIN`")
@Getter
@Setter
@NoArgsConstructor
public class TicketMainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ticket_idx;

    @Column(nullable = false, length = 50, unique = true, updatable = false)
    private String ticket_title;

    @Column(nullable = true, length = 200, updatable = false)
    private String ticket_title_image;

    @Column(nullable = true, length = 100, updatable = false)
    private String ticket_sub_title;

    @Column(nullable = false, updatable = false)
    private Integer ticket_qty;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime ticket_open;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime ticket_close;

    @OneToOne(fetch = FetchType.LAZY)

    // 2. @JoinColumn을 사용하여 외래 키 컬럼 정의
    // TICKET_MAIN 테이블에 TICKET_DETAIL_IDX라는 외래 키 컬럼이 생성됩니다.
    @JoinColumn(name = "ticket_idx")
    private TicketDetailEntity ticketDetail; // <- JPQL JOIN에 사용될 필드

    // Setter/Getter 및 편의 메서드 추가 (양방향 설정 시)
//    public void setTicketDetail(TicketDetailEntity detail) {
//        this.ticketDetail = detail;
//        if (detail.getTicketMain() != this) {
//            detail.setTicketMain(this); // 상대방 쪽도 함께 설정
//        }
//    }

}
