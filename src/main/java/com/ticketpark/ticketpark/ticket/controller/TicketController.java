package com.ticketpark.ticketpark.ticket.controller;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.ticket.service.QueueService;
import com.ticketpark.ticketpark.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final QueueService queueService;

    @GetMapping("/list")
    public ResponseEntity getTicketList(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ){

        DefaultRes result = ticketService.getTicketListByOption(page, searchType, searchValue);

        return new ResponseEntity(DefaultRes.res(result.isSuccess(), result.getMsg(), result.getData()), HttpStatus.OK);
    }

    @GetMapping("/detail/{idx}")
    public ResponseEntity getTicketDetail(@PathVariable(value = "idx") String idx){

        DefaultRes result = ticketService.getTicketDetailByIdx(Integer.parseInt(idx));

        return new ResponseEntity(DefaultRes.res(true, "", result.getData()), HttpStatus.OK);
    }

    @PostMapping("/waiting/{idx}")
    public ResponseEntity<?> joinQueue(@RequestAttribute("user") Map<String, Object> userInfo, @PathVariable(value = "idx") String idx) {
        String userId = (String) userInfo.get("memberId");
        Long rank = queueService.enterQueue(userId, idx);
        return new ResponseEntity(DefaultRes.res(true, "대기열 진입", rank+1), HttpStatus.OK);

    }

    @GetMapping("/waiting/{idx}")
    public ResponseEntity<?> getStatus(@RequestAttribute("user") Map<String, Object> userInfo, @PathVariable(value = "idx") String idx) {
        String userId = (String) userInfo.get("memberId");
        if (queueService.isAllowed(userId, idx)) {
            return new ResponseEntity(DefaultRes.res(true, ""), HttpStatus.FOUND);
        }
        Long rank = queueService.getWaitCount(userId, idx);
        if(rank < 0){
            return new ResponseEntity(DefaultRes.res(false, "시간초과"), HttpStatus.REQUEST_TIMEOUT);
        }
        return new ResponseEntity(DefaultRes.res(true, "현재 대기 순번", rank), HttpStatus.OK);
    }

    @PostMapping("/reserve/{idx}")
    public ResponseEntity reserveTicket(@RequestAttribute("user") Map<String, Object> userInfo, @PathVariable(value = "idx") String idx){

        ticketService.reserveTicket(Integer.parseInt(idx), Integer.parseInt((String) userInfo.get("memberIdx")));
        queueService.finishTicketing(idx, (String) userInfo.get("memberId"));

        return new ResponseEntity(DefaultRes.res(true, ""), HttpStatus.OK);
    }

}
