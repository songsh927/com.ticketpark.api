package com.ticketpark.ticketpark.ticket.controller;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.ticket.dto.TicketListPageDTO;
import com.ticketpark.ticketpark.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

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

        System.out.println("TicketController.getTicketDetail");
        DefaultRes result = ticketService.getTicketDetailByIdx(Integer.parseInt(idx));

        return new ResponseEntity(DefaultRes.res(true, "", result.getData()), HttpStatus.OK);
    }

//    @GetMapping("/reserve")
//    public ResponseEntity getTicketReserveDetail(){
//        return new ResponseEntity(DefaultRes.res(true, ""), HttpStatus.OK);
//    }

    @PostMapping("/reserve/{idx}")
    public ResponseEntity reserveTicket(@RequestAttribute("user") Map<String, Object> userInfo, @PathVariable(value = "idx") String idx){

        System.out.println("TicketController.reserveTicket");
        ticketService.reserveTicket(Integer.parseInt(idx), Integer.parseInt((String) userInfo.get("memberIdx")));

        return new ResponseEntity(DefaultRes.res(true, ""), HttpStatus.OK);
    }

}
