package com.yakeru.mini_jira.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.yakeru.mini_jira.user.User;
import com.yakeru.mini_jira.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${mini-jira.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest  request, HttpServletResponse response, 
        Authentication authentication) throws IOException {
            
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String githubId = oAuth2User.getAttribute("id").toString();

        User user = userRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login — this should never happen"));

        String jwt = jwtService.generateToken(user);
        String redirectUrl = frontendUrl + "/auth/callback?token=" + jwt;

        log.info("OAuth2 success, redirecting user={} to Mini-Jira Frontend", user.getUsername());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
