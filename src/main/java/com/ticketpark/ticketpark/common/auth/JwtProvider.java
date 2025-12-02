package com.ticketpark.ticketpark.common.auth;

import com.ticketpark.ticketpark.common.dto.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    private final String key;
    private final long accessExpireTime;
    private final long refreshExpireTime;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expire-time}") long accessExpireTime,
            @Value("${jwt.refresh-expire-time}") long refreshExpireTime
    ) {
        this.key = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.accessExpireTime = 3600000; //accessExpireTime;
        this.refreshExpireTime = 3600000; //refreshExpireTime;
    }

    public TokenInfo create(Integer memberIdx, String memberId){
//        String memberId = authentication.getName();
        String accessToken = generateAccessToken(memberIdx, memberId);
        String refreshToken = generateRefreshToken(memberIdx, memberId);

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateAccessToken(Integer memberIdx, String memberId){
        Date now = new Date();
        Date expire = new Date(now.getTime() + accessExpireTime);

        Map<String, Object> memberInfo = new HashMap<>();
        memberInfo.put("memberId", memberId);
        memberInfo.put("memberIdx", memberIdx);

        return Jwts.builder()
                .setSubject(memberIdx.toString())
                .setClaims(memberInfo)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

    }

    private String generateRefreshToken(Integer memberIdx, String memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpireTime);

        Map<String, Object> memberInfo = new HashMap<>();
        memberInfo.put("memberId", memberId);
        memberInfo.put("memberIdx", memberIdx);

        return Jwts.builder()
                .setSubject(memberIdx.toString())
                .setClaims(memberInfo)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token){

        try{
            Jws<Claims> claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e){
            return false;
        }

    }

}
