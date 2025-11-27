package com.ticketpark.ticketpark.member.controller;

import com.ticketpark.ticketpark.common.DefaultRes;
import com.ticketpark.ticketpark.member.dto.GetMemberInfoDTO;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.LoginDTO;
import com.ticketpark.ticketpark.member.dto.UpdateDTO;
import com.ticketpark.ticketpark.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity join(@RequestBody JoinDTO joinDTO){
        DefaultRes result = memberService.createMemberInfo(joinDTO);

        return new ResponseEntity(DefaultRes.res(result.isSuccess(), result.getMsg()), result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);

    }

    @GetMapping("/find")
    public ResponseEntity findMember(@RequestParam(required = true) String idx){
        GetMemberInfoDTO member = memberService.findMemberByIdx(Integer.parseInt(idx));

        return new ResponseEntity(DefaultRes.res(true, "회원정보 조회 성공", member), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO){

        boolean result = memberService.login(loginDTO.getId(), loginDTO.getPassword());

        if(result){
            return new ResponseEntity(DefaultRes.res(true, "로그인 성공"), HttpStatus.OK);
        } else {
            return new ResponseEntity(DefaultRes.res(false, "로그인 실패"), HttpStatus.BAD_REQUEST);
        }

    }

//    토큰 개발 후 추가개발 예정
//    @PostMapping("/logout")
//    public ResponseEntity logout(){}

    @PatchMapping
    public ResponseEntity update(@RequestBody UpdateDTO updateDTO){
        memberService.updateMemberInfo(updateDTO);

        return new ResponseEntity(DefaultRes.res(true, "업데이트 성공"), HttpStatus.ACCEPTED);
    }

}
