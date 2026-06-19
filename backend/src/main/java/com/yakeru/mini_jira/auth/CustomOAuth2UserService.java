package com.yakeru.mini_jira.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.yakeru.mini_jira.user.User;
import com.yakeru.mini_jira.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {

        OAuth2User oAuth2User = super.loadUser(request);

        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        User user = userRepository.findByGithubId(githubId)
            .map(existing -> updateUser(existing, username, name, email, avatarUrl))
            .orElseGet(() -> createUser(githubId, username, name, email, avatarUrl));

        log.info("OAuth2 login: user={} id={}", username, user.getId());

        return oAuth2User;
    }

    private User createUser(String githubId, String username, String name, String email, String avatarUrl) {

        User user = User.builder()
            .githubId(githubId)
            .username(username)
            .name(name)
            .email(email)
            .avatarUrl(avatarUrl)
            .build();

        return userRepository.save(user);
    }

    private User updateUser(User existing, String username, String name, String email, String avatarUrl) {

        existing.setUsername(username);
        existing.setName(name);
        existing.setEmail(email);
        existing.setAvatarUrl(avatarUrl);

        return userRepository.save(existing);
    }

}
