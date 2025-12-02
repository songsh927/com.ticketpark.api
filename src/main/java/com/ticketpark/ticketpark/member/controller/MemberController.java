package com.ticketpark.ticketpark.member.controller;

import com.ticketpark.ticketpark.common.DefaultRes;
import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.common.dto.TokenInfo;
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

        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getId(),
                loginDTO.getPassword()
        );

        Authentication authenticated = authenticationManager.authenticate(authenticationToken);
        MemberSecurityEntity memberInfo = (MemberSecurityEntity) authenticated.getPrincipal();

        TokenInfo token = jwtProvider.create(memberInfo.getUserIdx(), memberInfo.getMemberId());

        return new ResponseEntity(DefaultRes.res(true,"로그인 성공", token), HttpStatus.OK);

    }

//    @PostMapping("/logout")
//    public ResponseEntity logout(Authentication authentication){
//
//        // 1. 💡 Refresh Token 무효화 (핵심)
//        if (authentication != null && authentication.isAuthenticated()) {
//            String memberId = authentication.getName(); // 현재 로그인된 사용자 ID 추출
//
//            // Redis 또는 DB에 저장된 Refresh Token 삭제
////            redisService.deleteValues(memberId);
//
//            // 2. [선택] Access Token 블랙리스트 처리 (Access Token 수명이 길 경우)
//            // 현재 사용 중인 Access Token이 만료되기 전에 사용되는 것을 막기 위해
//            // 토큰을 받아서 남은 시간 동안 블랙리스트(Redis)에 저장할 수 있습니다.
//        }
//
//        // 3. 💡 SecurityContext 초기화 (현재 요청의 인증 상태 제거)
//        SecurityContextHolder.clearContext();
//
//    }

    @PatchMapping
    public ResponseEntity update(@RequestBody UpdateDTO updateDTO){
        memberService.updateMemberInfo(updateDTO);

        return new ResponseEntity(DefaultRes.res(true, "업데이트 성공"), HttpStatus.ACCEPTED);
    }

}
