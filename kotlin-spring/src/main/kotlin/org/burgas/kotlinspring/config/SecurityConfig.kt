package org.burgas.kotlinspring.config

import org.burgas.kotlinspring.service.IdentityDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private final val passwordEncoder: PasswordEncoder
    private final val identityDetailsService: IdentityDetailsService

    constructor(passwordEncoder: PasswordEncoder, identityDetailsService: IdentityDetailsService) {
        this.passwordEncoder = passwordEncoder
        this.identityDetailsService = identityDetailsService
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val daoAuthenticationProvider = DaoAuthenticationProvider(this.identityDetailsService)
        daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder)
        return ProviderManager(daoAuthenticationProvider)
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .csrf{ csrfConfigurer -> csrfConfigurer.csrfTokenRequestHandler(XorCsrfTokenRequestAttributeHandler()) }
            .cors { corsConfigurer -> corsConfigurer.configurationSource(UrlBasedCorsConfigurationSource()) }
            .httpBasic { httpBasicConfigurer -> httpBasicConfigurer.securityContextRepository(RequestAttributeSecurityContextRepository()) }
            .authenticationManager(this.authenticationManager())
            .authorizeHttpRequests { requestMatcherRegistry -> requestMatcherRegistry

                .requestMatchers(
                    "/api/v1/security/csrf-token",

                    "/api/v1/images/by-id",

                    "/api/v1/identities/create"
                )
                .permitAll()

                .requestMatchers(
                    "/api/v1/identities/by-id", "/api/v1/identities/update", "/api/v1/identities/delete",
                    "/api/v1/identities/upload-image", "/api/v1/identities/remove-image", "/api/v1/identities/change-password",

                    "/api/v1/notes", "/api/v1/notes/by-id", "/api/v1/notes/create", "/api/v1/notes/update", "/api/v1/notes/delete",
                    "/api/v1/notes/remove-images", "/api/v1/notes/add-images"
                )
                .hasAnyAuthority("ADMIN", "USER")

                .requestMatchers(
                    "/api/v1/identities", "/api/v1/identities/change-status"
                )
                .hasAnyAuthority("ADMIN")
            }
        return httpSecurity.build()
    }
}