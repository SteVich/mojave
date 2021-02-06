package com.mojave.authservice.service;


import com.mojave.authservice.dto.request.SignUpRequest;
import com.mojave.authservice.dto.request.TokenRequest;
import com.mojave.authservice.dto.response.JwtAuthenticationResponse;
import com.mojave.authservice.model.User;
import com.mojave.authservice.model.vocabulary.Role;
import com.mojave.authservice.repository.UserRepository;
import com.mojave.authservice.security.JwtTokenProvider;
import com.mojave.authservice.util.properties.JwtProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider tokenProvider;
    JwtProperties properties;

    @Transactional(readOnly = true)
    public Boolean existsByUsername(SignUpRequest user) {
        return userRepository.existsByUsername(user.getUsername());
    }

    @Transactional(readOnly = true)
    public Boolean existsByEmail(SignUpRequest user) {
        return userRepository.existsByEmail(user.getEmail());
    }

    @Transactional
    public void registerUser(SignUpRequest signUpRequest) {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.NOT_DEFINED);

        userRepository.save(user);
    }

    public String createAccessToken(Authentication authentication) {
        return tokenProvider.generateToken(authentication, properties.getExpirationAccessToken());
    }

    public String createRefreshToken(Authentication authentication) {
        return tokenProvider.generateToken(authentication, properties.getExpirationRefreshToken());
    }

    public JwtAuthenticationResponse generateNewTokens(TokenRequest tokenRequest) {
        String username = tokenProvider.extractUsername(tokenRequest.getRefreshToken());

        String newAccessToken = tokenProvider.generateTokenFromUsername(username, properties.getExpirationAccessToken());
        String newRefreshToken = tokenProvider.generateTokenFromUsername(username, properties.getExpirationRefreshToken());

        return new JwtAuthenticationResponse(newAccessToken, newRefreshToken);
    }

}
