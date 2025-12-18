package com.ticketpark.ticketpark.member.service;

import com.ticketpark.ticketpark.member.entity.MemberEntity;
import com.ticketpark.ticketpark.member.entity.MemberSecurityEntity;
import com.ticketpark.ticketpark.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;


    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        return memberRepository.findOneById(memberId).map(
                member -> new MemberSecurityEntity(member)
        ).orElseThrow(() -> new UsernameNotFoundException(memberId + "을 찾을 수 없습니다."));
    }
}
