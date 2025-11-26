package com.ticketpark.ticketpark.member.service;

import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.entity.MemberEntity;
import com.ticketpark.ticketpark.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public boolean createMemberInfo(JoinDTO joinDTO){
        Optional<MemberEntity> isDupId = memberRepository.findOneById(joinDTO.getId());
        // 아이디 중복
        if(!isDupId.isEmpty()){
            return false;
        }

        Optional<MemberEntity> isDupEmail = memberRepository.findOneById(joinDTO.getId());
        // 이메일 중복
        if(!isDupEmail.isEmpty()){
            return false;
        }

        memberRepository.saveMember(joinDTO);
        return true;

    }

    public MemberEntity findMemberByIdx(Integer memberIdx){
        return memberRepository.findOneByIdx(memberIdx);
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

}
