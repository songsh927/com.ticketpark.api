package com.ticketpark.ticketpark.common.filter;

import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.common.dto.DefaultRes;
import com.ticketpark.ticketpark.common.redis.RedisService;
import com.ticketpark.ticketpark.member.service.MemberSecurityService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtCommonFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberSecurityService memberSecurityService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        String token = request.getHeader("x-access-token");
        String checkToken = (String) redisService.getValues(token);

        if(token == null || checkToken == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String errorMessage = "{\"success\": false, \"message\": \"유효하지 않거나 만료된 토큰입니다.\", \"data\": null}";
            response.getWriter().write(errorMessage);
            return;
        }


        if (jwtProvider.validateToken(token)) {

            Claims extractedTokenInfo = jwtProvider.extractAllClaims(token);

            UserDetails userDetails = memberSecurityService.loadUserByUsername((String) extractedTokenInfo.get("memberId"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Map<String, String> member = new HashMap<>();

            member.put("memberIdx", extractedTokenInfo.get("memberIdx").toString());
            member.put("memberId", (String) extractedTokenInfo.get("memberId"));
            member.put("token", token);

            request.setAttribute("user", member);
        }


        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // JWT 토큰이 필요 없는 경로들에 대해 필터를 건너뜁니다
        return path.startsWith("/member/login") ||
                path.startsWith("/member/create") ||
                path.startsWith("/ticket/list") ||
                path.startsWith("/ticket/detail");
    }
}
