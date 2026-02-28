package com.example.domain.social.google.service.command;

import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.support.GoogleOauthSessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GoogleSocialLoginStartCommandService {

    private final HttpServletRequest httpServletRequest;

    public void prepareLoginSession() {
        final GoogleOauthSession oauthSession = GoogleOauthSession.create();
        final HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(GoogleOauthSessionKeys.SESSION_KEY, oauthSession);
    }
}
