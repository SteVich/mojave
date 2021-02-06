package com.mojave.authservice.controller;

import com.mojave.authservice.dto.request.LoginRequest;
import com.mojave.authservice.dto.request.SignUpRequest;
import com.mojave.authservice.dto.request.TokenRequest;
import com.mojave.authservice.dto.response.ApiResponse;
import com.mojave.authservice.dto.response.JwtAuthenticationResponse;
import com.mojave.authservice.security.AuthenticationManager;
import com.mojave.authservice.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

    AuthenticationManager authenticationManager;
    AuthService authService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<ApiResponse>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return Mono.just(signUpRequest).map(request -> {
            ApiResponse apiResponse;

            if (authService.existsByUsername(signUpRequest)) {
                apiResponse = new ApiResponse(false, "Username  is already taken!");
            } else if (authService.existsByEmail(signUpRequest)) {
                apiResponse = new ApiResponse(false, "Email  is already taken!");
            } else {
                authService.registerUser(signUpRequest);
                apiResponse = new ApiResponse(true, "User registered successfully");
            }

            return ResponseEntity.ok(apiResponse);
        });
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<JwtAuthenticationResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return Mono.just(loginRequest).map(request -> {
            Authentication authentication = (Authentication) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = authService.createAccessToken(authentication);
            String refreshToken = authService.createRefreshToken(authentication);

            return ResponseEntity.ok(new JwtAuthenticationResponse(accessToken, refreshToken));
        });
    }

    @PostMapping("/refreshToken")
    public Mono<ResponseEntity<JwtAuthenticationResponse>> refreshJwtToken(@Valid @RequestBody TokenRequest tokenRequest) {
        return Mono.just(tokenRequest).map(request -> ResponseEntity.ok(authService.generateNewTokens(tokenRequest)));
    }
}

