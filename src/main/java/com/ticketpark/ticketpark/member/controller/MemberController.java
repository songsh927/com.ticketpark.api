package com.ticketpark.ticketpark.member.controller;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.common.dto.TokenInfo;
import com.ticketpark.ticketpark.common.redis.RedisService;
import com.ticketpark.ticketpark.member.dto.GetMemberInfoDTO;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.LoginDTO;
import com.ticketpark.ticketpark.member.dto.UpdateDTO;
import com.ticketpark.ticketpark.member.entity.MemberSecurityEntity;
import com.ticketpark.ticketpark.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @PostMapping("/create")
    public ResponseEntity join(@RequestBody JoinDTO joinDTO){
        DefaultRes result = memberService.createMemberInfo(joinDTO);

        return new ResponseEntity(DefaultRes.res(result.isSuccess(), result.getMsg()), result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);

    }

    @GetMapping(path = "/myinfo")
    public ResponseEntity findMember(@RequestAttribute("user") Map<String, Object> userInfo){

        GetMemberInfoDTO member = memberService.findMemberByIdx(Integer.parseInt((String) userInfo.get("memberIdx")));

        return new ResponseEntity(DefaultRes.res(true, "회원정보 조회 성공", member), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO){

        DefaultRes result = memberService.login(loginDTO.getId(), loginDTO.getPassword());
        MemberSecurityEntity memberInfo = (MemberSecurityEntity) result.getData();

        if(result.isSuccess()){
            TokenInfo token = jwtProvider.create(memberInfo.getMemberIdx(), memberInfo.getMemberId());
            return new ResponseEntity(DefaultRes.res(true,"로그인 성공", token), HttpStatus.OK);
        }

        return new ResponseEntity(DefaultRes.res(false,"로그인 실패"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/logout")
    public void logout(@RequestAttribute("user") Map<String, Object> userInfo){

        System.out.println(userInfo);
        redisService.deleteValues(userInfo.get("token").toString());
        SecurityContextHolder.clearContext();

    }

    @PatchMapping
    public ResponseEntity update(@RequestBody UpdateDTO updateDTO){
        memberService.updateMemberInfo(updateDTO);

        return new ResponseEntity(DefaultRes.res(true, "업데이트 성공"), HttpStatus.ACCEPTED);
    }

}
