package org.burgas.javaspring.config;

import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.service.IdentityDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final IdentityDetailsService identityDetailsService;

    @Bean
    public AuthenticationManager authenticationManager() {
        var provider = new DaoAuthenticationProvider(this.identityDetailsService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(csrf -> csrf.csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler()))
                .cors(cors -> cors.configurationSource(new UrlBasedCorsConfigurationSource()))
                .httpBasic(httpBasic ->
                        httpBasic.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .authenticationManager(this.authenticationManager())
                .authorizeHttpRequests(httpRequests -> httpRequests

                        .requestMatchers(
                                "/api/v1/security/csrf-token",

                                "/api/v1/identities/create"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/identities/by-id", "/api/v1/identities/update", "/api/v1/identities/delete",
                                "/api/v1/identities/upload-image", "/api/v1/identities/remove-image",
                                "/api/v1/identities/change-password", "/api/v1/identities/change-status"
                        )

                        .hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers(
                                "/api/v1/identities"
                        )
                        .hasAnyAuthority("ADMIN")
                )
                .build();
    }
}
