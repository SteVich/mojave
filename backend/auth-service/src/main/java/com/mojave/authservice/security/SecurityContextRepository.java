package com.mojave.authservice.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    JwtTokenProvider tokenProvider;
    AuthenticationManager authenticationManager;


    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new IllegalStateException("Save method not supported!");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String accessToken = getAccessTokenFromRequest(exchange);
        String refreshToken = getRefreshTokenFromRequest(exchange);

        Mono<SecurityContext> securityContextMono = Mono.empty();
        if (StringUtils.hasText(accessToken) && tokenProvider.validateAccessToken(accessToken)
                && tokenProvider.validateRefreshToken(refreshToken)) {

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(accessToken, accessToken);

            securityContextMono = authenticationManager.authenticate(auth)
                    .map(SecurityContextImpl::new);
        }

        return securityContextMono;
    }

    private String getAccessTokenFromRequest(ServerWebExchange exchange) {
        String bearerToken = getHeaderFromServerWebExchange(exchange, HttpHeaders.AUTHORIZATION);

        return (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7) : null;
    }

    private String getRefreshTokenFromRequest(ServerWebExchange exchange) {
        return getHeaderFromServerWebExchange(exchange, "RefreshToken");
    }

    private String getHeaderFromServerWebExchange(ServerWebExchange exchange, String headerName) {
        return exchange.getRequest()
                .getHeaders()
                .getFirst(headerName);
    }
}