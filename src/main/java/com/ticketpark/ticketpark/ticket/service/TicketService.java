package com.ticketpark.ticketpark.ticket.service;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.exception.ApiException;
import com.ticketpark.ticketpark.common.exception.ExceptionEnum;
import com.ticketpark.ticketpark.ticket.dto.ReservationForm;
import com.ticketpark.ticketpark.ticket.dto.TicketDetailDTO;
import com.ticketpark.ticketpark.ticket.dto.TicketListPageDTO;
import com.ticketpark.ticketpark.ticket.entity.TicketReserveEntity;
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

    public DefaultRes getTicketListByOption(Integer page, String searchType, String searchValue) {

        // TODO user-agent가 PC가 아닐때 pageSize 고려
        Pageable pageable = PageRequest.of(page > 1? page -1 : 0, 10);

        Page<TicketListPageDTO> ticketList = null;

        if(searchType != null){
            ticketList = ticketRepository.findAllByOption(pageable, searchType, searchValue);
        } else {
            ticketList = ticketRepository.findAllByPage(pageable);
        }

        return DefaultRes.res(true, "", ticketList);
    }

    public DefaultRes getTicketDetailByIdx(Integer ticket_idx){

        TicketDetailDTO result = ticketRepository.findOneByIdx(ticket_idx)
                .orElseThrow(() -> new ApiException(ExceptionEnum.RUNTIME_EXCEPTION));

        return DefaultRes.res(true, "", result);

    }

    @Transactional
    public DefaultRes reserveTicket(Integer ticket_idx, Integer member_idx){

        ticketRepository.createReservation(new TicketReserveEntity(ticket_idx, member_idx));
        return DefaultRes.res(true, "");
    }

}
