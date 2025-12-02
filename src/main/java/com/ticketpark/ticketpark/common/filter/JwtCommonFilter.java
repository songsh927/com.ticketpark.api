package com.ticketpark.ticketpark.common.filter;

import com.ticketpark.ticketpark.common.auth.JwtProvider;
import com.ticketpark.ticketpark.member.entity.MemberSecurityEntity;
import com.ticketpark.ticketpark.member.service.MemberSecurityService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtCommonFilter implements Filter {

    private final JwtProvider jwtProvider;
    private final MemberSecurityService memberSecurityService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = ((HttpServletRequest) request).getHeader("x-access-token");

        if (token != null && jwtProvider.validateToken(token)) {
            Claims extractedTokenInfo = jwtProvider.extractAllClaims(token);

            Map<String, String> member = new HashMap<>();

            member.put("memberIdx", extractedTokenInfo.get("memberIdx").toString());
            member.put("memberId", (String) extractedTokenInfo.get("memberId"));

            request.setAttribute("user", member);

        }

        chain.doFilter(request, response);
    }
}
