package com.ticketpark.ticketpark.member.repository;

import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemberRepository{

    @PersistenceContext
    private EntityManager em;

    public void saveMember(JoinDTO joinDTO){
        em.persist(joinDTO);
    }

    public MemberEntity findOneByIdx(Integer member_idx){
        MemberEntity member = em.find(MemberEntity.class, member_idx);
        return member;
    }

    public Optional<MemberEntity> findOneById(String member_id){
        return em.createQuery("select m from MemberEntity m where m.member_id = :member_id", MemberEntity.class)
                .setParameter("member_id", member_id)
                .getResultList().stream().findAny();
    }

    public Optional<MemberEntity> findOneByEmail(String member_email){
        return em.createQuery("select m from MemberEntity m where m.member_email = :member_email", MemberEntity.class)
                .setParameter("member_email", member_email)
                .getResultList().stream().findAny();
    }

}
