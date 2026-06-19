package com.yakeru.mini_jira.auth;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Utils {
    
    public String extractId(OAuth2User user, String registrationId) {
        return switch (registrationId) {
            case "github" -> user.getAttribute("id").toString();
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    public String extractUsername(OAuth2User user, String registrationId) {
        return switch (registrationId) {
            case "github" -> user.getAttribute("login");
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    public String extractAvatarUrl(OAuth2User user, String registrationId) {
        return switch (registrationId) {
            case "github" -> user.getAttribute("avatar_url");
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}
