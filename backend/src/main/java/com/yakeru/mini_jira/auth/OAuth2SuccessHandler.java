package com.yakeru.mini_jira.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    private final OAuth2Utils oauth2Utils;

    @Value("${mini-jira.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest  request, HttpServletResponse response, 
        Authentication authentication) throws IOException {
            
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String registrationId = token.getAuthorizedClientRegistrationId();
        String providerId = oauth2Utils.extractId(token.getPrincipal(), registrationId);

        User user = userRepository
            .findByProviderIdAndProvider(providerId, registrationId)
            .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login — this should never happen"));

        String jwt = jwtService.generateToken(user);
        String redirectUrl = frontendUrl + "/auth/callback?token=" + jwt;

        // TODO : Note for Angular's /auth/callback route implementation:
        // After reading the token, call router.navigate(['/dashboard'], { replaceUrl: true })
        // to remove the token from browser history and the Referer header.
        log.info("OAuth2 success, redirecting user={} to Mini-Jira Frontend", user.getUsername());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
