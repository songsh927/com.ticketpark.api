package com.ticketpark.ticketpark.member.controller;

import com.ticketpark.ticketpark.common.DefaultRes;
import com.ticketpark.ticketpark.common.ResponseMessage;
import com.ticketpark.ticketpark.common.StatusCode;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.LoginDTO;
import com.ticketpark.ticketpark.member.entity.MemberEntity;
import com.ticketpark.ticketpark.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity join(@RequestBody JoinDTO joinDTO){
        boolean result = memberService.createMemberInfo(joinDTO);

        if(result){
            return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.CREATED_USER), HttpStatus.OK);
        } else {
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, ResponseMessage.CREATED_USER_FAIL), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/find")
    public MemberEntity findMember(@RequestParam(required = true) String idx){
        MemberEntity member = memberService.findMemberByIdx(Integer.parseInt(idx));

        return member;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO){

        boolean result = memberService.login(loginDTO.getId(), loginDTO.getPassword());

        if(result){
            return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.LOGIN_SUCCESS), HttpStatus.OK);
        } else {
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, ResponseMessage.LOGIN_FAIL), HttpStatus.BAD_REQUEST);
        }

    }

}
