package com.ticketpark.ticketpark.member.service;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.common.exception.ApiException;
import com.ticketpark.ticketpark.common.exception.ExceptionEnum;
import com.ticketpark.ticketpark.common.redis.AuthRedisService;
import com.ticketpark.ticketpark.member.dto.GetMemberInfoDTO;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.UpdateDTO;
import com.ticketpark.ticketpark.member.entity.MemberEntity;
import com.ticketpark.ticketpark.member.entity.MemberSecurityEntity;
import com.ticketpark.ticketpark.member.repository.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthRedisService authRedisService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public DefaultRes createMemberInfo(JoinDTO joinDTO){

        DefaultRes checkDuplicate = validateDuplicationMemberInfo(joinDTO);

        if(checkDuplicate.isSuccess()){
            String encodedPassword = passwordEncoder.encode(joinDTO.getPassword());
            memberRepository.saveMember(joinDTO.toMember(encodedPassword));

            return DefaultRes.res(true, "회원가입 성공");// TODO 로그인 페이지로 리다이렉트 필요
        }
        return DefaultRes.res(false, checkDuplicate.getMsg());
    }

    private DefaultRes validateDuplicationMemberInfo(JoinDTO joinDTO){
        String id = joinDTO.getId();
        String email  = joinDTO.getEmail();

        Optional<MemberEntity> isDupId = memberRepository.findOneById(id);
        Optional<MemberEntity> isDupEmail = memberRepository.findOneByEmail(email);

        //아이디 중복 확인
        if(!isDupId.isEmpty()){
            return DefaultRes.res(false, "사용중인 아이디입니다.");
        }

        //이메일 중복 확인
        if(!isDupEmail.isEmpty()){
            return DefaultRes.res(false, "사용중인 이메일입니다.");
        }

        return DefaultRes.res(true,"");
    }

    public GetMemberInfoDTO findMemberByIdx(Integer memberIdx){
        MemberEntity member = memberRepository.findOneByIdx(memberIdx)
                .orElseThrow(() -> new RuntimeException("ID " + memberIdx + "에 해당하는 사용자가 존재하지 않습니다."));

        return new GetMemberInfoDTO(member);

    }

    public DefaultRes login(String memberId, String memberPassword){

        try{
            //버그 및 내부 에러로 인한 실패도 로그인 실패로 간주
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(memberId,memberPassword);
            Authentication authenticated = authenticationManager.authenticate(authenticationToken);

            return DefaultRes.res(true, "로그인 성공", (MemberSecurityEntity) authenticated.getPrincipal());
        }catch (Exception e){
            throw new ApiException(ExceptionEnum.LOGIN_FAIL);
        }
    }

    @Transactional
    public void updateMemberInfo(UpdateDTO updateDTO){
        Optional<MemberEntity> member = memberRepository.findOneByIdx(updateDTO.getIdx());

        if(updateDTO.getPassword() != null){
            member.get().setMember_pw(updateDTO.getPassword());
        }

        if(updateDTO.getEmail() != null){
            member.get().setMember_email(updateDTO.getEmail());
        }

    }
}
