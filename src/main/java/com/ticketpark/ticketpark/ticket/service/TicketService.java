package com.ticketpark.ticketpark.ticket.service;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.exception.ApiException;
import com.ticketpark.ticketpark.common.exception.ExceptionEnum;
import com.ticketpark.ticketpark.ticket.dto.PageResponseDTO;
import com.ticketpark.ticketpark.ticket.dto.ReservationForm;
import com.ticketpark.ticketpark.ticket.dto.TicketDetailDTO;
import com.ticketpark.ticketpark.ticket.dto.TicketListPageDTO;
import com.ticketpark.ticketpark.ticket.entity.TicketReserveEntity;
import com.ticketpark.ticketpark.ticket.repository.TicketJpaRepository;
import com.ticketpark.ticketpark.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketJpaRepository ticketJpaRepository;

    public DefaultRes getTicketListByOption(Integer page, String searchType, String searchValue) {

        // TODO user-agent가 PC가 아닐때 pageSize 고려
        int pageIndex = (page != null && page > 1) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, 10);

        Page<TicketListPageDTO> ticketList =
                (searchType != null)
                        ? ticketRepository.findAllByOption(pageable, searchType, searchValue)
                        : ticketRepository.findAllByPage(pageable);

        return DefaultRes.res(true, "", PageResponseDTO.from(ticketList));
    }

    public DefaultRes getTicketDetailByIdx(Integer ticket_idx){

        TicketDetailDTO result = ticketRepository.findOneByIdx(ticket_idx)
                .orElseThrow(() -> new ApiException(ExceptionEnum.RUNTIME_EXCEPTION));

        return DefaultRes.res(true, "", result);

    }

    @Transactional
    public DefaultRes reserveTicket(Integer ticket_idx, Integer member_idx){

        boolean checkQty = ticketJpaRepository.findByIdxWithPessimisticLock(ticket_idx);

        if(!checkQty){
            System.out.println(":::: 티켓의 수량 부족");
            throw new ApiException(ExceptionEnum.TICKET_QTY_FAIL);
        }

        ticketJpaRepository.decreaseTicketQty(ticket_idx);
        ticketRepository.createReservation(new TicketReserveEntity(ticket_idx, member_idx));
        return DefaultRes.res(true, "");
    }

}
