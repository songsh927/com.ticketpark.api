package com.ticketpark.ticketpark.service;

import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.dto.TokenInfo;
import com.ticketpark.ticketpark.common.redis.RedisService;
import com.ticketpark.ticketpark.member.dto.GetMemberInfoDTO;
import com.ticketpark.ticketpark.member.dto.JoinDTO;
import com.ticketpark.ticketpark.member.dto.LoginDTO;
import com.ticketpark.ticketpark.member.entity.MemberSecurityEntity;
import com.ticketpark.ticketpark.member.repository.MemberRepository;
import com.ticketpark.ticketpark.member.service.MemberSecurityService;
import com.ticketpark.ticketpark.member.service.MemberService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "JWT_SECRET_KEY=testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest!!",
        "JWT_ACCESS_EXPIRE=3600000",
        "JWT_REFRESH_EXPIRE=7200000"
})
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberSecurityService memberSecurityService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    RedisService redisService;

    @Test
    public void 정상_회원가입() throws Exception {

        JoinDTO member = new JoinDTO();
        member.setId("test3");
        member.setPassword("qwer1234");
        member.setEmail("test3@email.com");

        DefaultRes result = memberService.createMemberInfo(member);

        assertEquals(result.isSuccess(), true);

    }

    @Test
    public void 로그인() throws Exception {

        //디비에 가입되어있는 계정 test1의 idx 1 조회
        DefaultRes result = memberService.login("test1", "qwer1234");
        MemberSecurityEntity memberInfo = (MemberSecurityEntity) result.getData();
        assertEquals(memberInfo.getMemberIdx(), 1);

        //로그인 과정에서 redis에 저장된 토큰과 일치하는지 검증
        TokenInfo token = jwtProvider.create(memberInfo.getMemberIdx(), memberInfo.getMemberId());
        String accessToken = token.getAccessToken();

        String redisMemberToken = (String) redisService.getValues(accessToken);
        String keyInfo[] = redisMemberToken.split("-");

        assertEquals(keyInfo[0], memberInfo.getMemberId());

    }

    @Test
    public void 내정보조회() throws Exception {

        // 토큰 발급 후 내정보조회 테스트
        TokenInfo token = jwtProvider.create(1, "test1");
        String accessToken = token.getAccessToken();

        boolean checkToken = jwtProvider.validateToken(accessToken);
        Claims claims = jwtProvider.extractAllClaims(accessToken);

        Integer idx = (Integer) claims.get("memberIdx");

        GetMemberInfoDTO memberInfo = memberService.findMemberByIdx(idx);

        assertEquals("test1",memberInfo.getId());

    }

}
