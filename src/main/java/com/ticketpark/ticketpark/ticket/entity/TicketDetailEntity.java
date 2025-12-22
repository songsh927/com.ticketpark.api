package com.ticketpark.ticketpark.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "TICKET_DETAIL")
@Getter
@Setter
@NoArgsConstructor
public class TicketDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ticket_idx;

    @Column(nullable = false)
    private Integer ticket_price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> ticket_info;

    // mappedBy를 사용하여 주인이 아님을 명시 (주인인 TicketMainEntity의 필드 이름: "ticketDetail")
    @OneToOne(mappedBy = "ticketDetail", fetch = FetchType.LAZY)
    private TicketMainEntity ticketMain;

    // Setter/Getter 및 편의 메서드 추가 (양방향 설정 시)
//    public void setTicketMain(TicketMainEntity main) {
//        this.ticketMain = main;
//    }
}
