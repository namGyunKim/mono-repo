package com.example.global.security.filter;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.entity.Member;
import com.example.domain.member.payload.dto.MemberLoginIdQuery;
import com.example.domain.security.PrincipalDetails;
import com.example.domain.security.jwt.JwtTokenParser;
import com.example.domain.security.jwt.JwtTokenPayload;
import com.example.global.security.SecurityContextManager;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenCheckQuery;
import com.example.global.security.blacklist.service.query.BlacklistedTokenQueryService;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.security.jwt.JwtTokenType;
import com.example.global.security.service.query.MemberAuthQueryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenParser jwtTokenParser;
    private final AccessTokenResolver accessTokenResolver;
    private final BlacklistedTokenQueryService blacklistedTokenQueryService;
    private final MemberAuthQueryService memberAuthQueryService;
    private final SecurityContextManager securityContextManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // REST API는 stateless를 기본으로 하므로, 매 요청마다 토큰 기반 인증만 신뢰합니다.
        // 토큰이 없거나 검증 실패 시 컨텍스트를 즉시 비웁니다.
        Optional<String> tokenOptional = accessTokenResolver.resolveAccessToken(request);
        if (tokenOptional.isEmpty()) {
            securityContextManager.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        final String token = tokenOptional.get();
        if (blacklistedTokenQueryService.isBlacklisted(BlacklistedTokenCheckQuery.of(token))) {
            securityContextManager.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (!authenticate(token)) {
            securityContextManager.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticate(String token) {
        Optional<JwtTokenPayload> payloadOptional = jwtTokenParser.parseToken(token);
        if (payloadOptional.isEmpty()) {
            return false;
        }

        final JwtTokenPayload payload = payloadOptional.get();
        if (payload.tokenType() != JwtTokenType.ACCESS || !StringUtils.hasText(payload.subject())) {
            return false;
        }

        Optional<Member> memberOptional = memberAuthQueryService.findActiveMemberForAuthentication(
                MemberLoginIdQuery.of(payload.subject())
        );
        if (memberOptional.isEmpty()) {
            return false;
        }

        final Member member = memberOptional.get();
        if (member.getTokenVersion() != payload.tokenVersion()) {
            return false;
        }

        AccountAuthMemberView authMember = AccountAuthMemberView.of(
                member.getId(),
                member.getLoginId(),
                member.getPassword(),
                member.getNickName(),
                member.getRole(),
                member.getMemberType(),
                member.getActive(),
                member.getTokenVersion()
        );
        PrincipalDetails principalDetails = new PrincipalDetails(authMember);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContext securityContext = securityContextManager.createEmptyContext();
        securityContext.setAuthentication(authentication);
        securityContextManager.setContext(securityContext);
        return true;
    }

    // API 전용 프로젝트에서는 요청 경로와 무관하게 stateless 정책을 유지합니다.
}
