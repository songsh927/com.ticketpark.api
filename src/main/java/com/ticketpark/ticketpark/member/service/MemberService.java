package com.ticketpark.ticketpark.member.service;

import com.ticketpark.ticketpark.common.DefaultRes;
import com.ticketpark.ticketpark.member.dto.GetMemberInfoDTO;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.UpdateDTO;
import com.ticketpark.ticketpark.member.entity.MemberEntity;
import com.ticketpark.ticketpark.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public DefaultRes createMemberInfo(JoinDTO joinDTO){

        DefaultRes checkDuplicate = validateDuplicationMemberInfo(joinDTO);

        if(checkDuplicate.isSuccess()){
            memberRepository.saveMember(joinDTO.toMember());
            return DefaultRes.res(true, "회원가입 성공", new GetMemberInfoDTO(joinDTO.toMember()));
        }

        return DefaultRes.res(false, checkDuplicate.getMsg());

    }

    private DefaultRes validateDuplicationMemberInfo(JoinDTO joinDTO){
        String id = joinDTO.getId();
        String email  = joinDTO.getEmail();

        Optional<MemberEntity> isDupId = memberRepository.findOneById(id);

        Optional<MemberEntity> isDupEmail = memberRepository.findOneByEmail(email);

        if(!isDupId.isEmpty()){
            return DefaultRes.res(false, "아이디가 사용중입니다.");
        }

        if(!isDupEmail.isEmpty()){
            return DefaultRes.res(false, "이메일이 중복되었습니다.");
        }

        return DefaultRes.res(true,"");
    }

    public GetMemberInfoDTO findMemberByIdx(Integer memberIdx){
        MemberEntity member = memberRepository.findOneByIdx(memberIdx)
                .orElseThrow(() -> new RuntimeException("ID " + memberIdx + "에 해당하는 사용자가 존재하지 않습니다."));

        return new GetMemberInfoDTO(member);

    }

    public boolean login(String memberId, String memberPassword){

        Optional<MemberEntity> member = memberRepository.findOneById(memberId);

        if(member.isEmpty()){
            return false;
        }

        if(member.get().getMember_id().equals(memberId) && member.get().getMember_pw().equals(memberPassword)){
            return true;
        }

        return false;
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
