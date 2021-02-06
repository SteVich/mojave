package com.mojave.authservice.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationManager implements ReactiveAuthenticationManager {

    JwtTokenProvider jwtTokenProvider;
    CustomUserDetailsService customUserDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username = jwtTokenProvider.extractUsername(authToken);

        Mono<Authentication> authenticationMono = Mono.empty();
        if (Objects.nonNull(username) && jwtTokenProvider.validateAccessToken(authToken)) {
            UserDetails userDetails = (UserDetails) customUserDetailsService.findByUsername(username).subscribe();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authenticationMono = Mono.just(authenticationToken);
        }

        return authenticationMono;
    }
}
