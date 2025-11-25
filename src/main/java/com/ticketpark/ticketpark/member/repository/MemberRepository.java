package com.ticketpark.ticketpark.member.repository;

import com.ticketpark.ticketpark.member.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository{

    @PersistenceContext
    private EntityManager em;

    public void createMember(){}

    public MemberEntity findOne(Integer member_idx){
        MemberEntity member = em.find(MemberEntity.class, member_idx);
        return member;
    }

}
