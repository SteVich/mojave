package com.mojave.authservice.security;

import com.mojave.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .switchIfEmpty(Mono.error(() -> new UsernameNotFoundException("User not found with username or email with : " + usernameOrEmail)))
                .map(UserPrincipal::create)
                .cast(UserDetails.class);
    }
}