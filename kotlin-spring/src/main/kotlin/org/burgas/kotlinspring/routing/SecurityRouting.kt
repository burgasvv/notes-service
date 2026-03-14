package org.burgas.kotlinspring.routing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.servlet.function.router

@Configuration
class SecurityRouting {

    @Bean
    fun securityRouter() = router {
        "/api/v1/security".nest {

            GET("/csrf-token") {
                val csrfToken = it.attribute("_csrf").orElseThrow() as CsrfToken
                ok().body(csrfToken)
            }
        }
    }
}