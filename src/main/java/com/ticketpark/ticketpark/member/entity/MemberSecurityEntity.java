package com.ticketpark.ticketpark.member.entity;


import com.ticketpark.ticketpark.member.entity.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class MemberSecurityEntity implements UserDetails {

    private final String memberId;
    private final String password;
    private final int memberIdx;
    private final Collection<? extends GrantedAuthority> authorities;

    public MemberSecurityEntity(MemberEntity member) {
        this.memberId = member.getMember_id();
        this.password = member.getMember_pw();
        this.memberIdx = member.getMember_idx();

        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return memberId;
    }

    public Integer getUserIdx(){
        return memberIdx;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}