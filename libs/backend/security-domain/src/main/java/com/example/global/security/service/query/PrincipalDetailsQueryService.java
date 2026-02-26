package com.example.global.security.service.query;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.domain.member.enums.MemberType;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrincipalDetailsQueryService implements UserDetailsService {

    private final AccountQueryService queryAccountService;

    // 로그인 아이디로 유저 정보 로드 (Spring Security Form Login의 핵심)
    // Spring Security의 기본 Form Login은 'username' 파라미터만 사용하여 이 메서드를 호출합니다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // [수정] Form Login 시 `role` 파라미터를 함께 전달받지 못하므로,
            // AccountQueryService는 `loginId`만으로 찾도록 합니다.
            // *단, AccountQueryService::findActiveMemberForAuthByLoginId는 Active 체크를 수행합니다.*
            AccountAuthMemberView member = queryAccountService.findActiveMemberForAuthByLoginId(AccountLoginIdQuery.of(username));
            if (member.memberType() != MemberType.GENERAL) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
            }
            return new PrincipalDetails(member);
        } catch (GlobalException e) {
            // [중요]
            // AccountQueryService는 Active 체크를 수행하며, 비활성 계정은 MEMBER_INACTIVE 예외를 던집니다.
            // 이 경우 UsernameNotFoundException으로 뭉개면 "계정 없음"으로 오인될 수 있어
            // DisabledException으로 변환하여 FailureHandler가 정확한 메시지를 노출할 수 있도록 합니다.
            if (e.getErrorCode() == ErrorCode.MEMBER_INACTIVE) {
                throw new DisabledException("비활성화된 계정입니다: " + username, e);
            }

            if (e.getErrorCode() == ErrorCode.MEMBER_NOT_EXIST) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username, e);
            }

            // 그 외 예외는 내부 인증 처리 실패로 전환 (Spring Security 표준 예외)
            throw new InternalAuthenticationServiceException("인증 처리 중 오류가 발생했습니다.", e);
        }
    }
}
