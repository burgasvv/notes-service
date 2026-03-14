package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.exception.ExceptionResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
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

            onError({ true }) { throwable, _ ->
                val exception = ExceptionResponse(
                    status = HttpStatus.BAD_REQUEST.name,
                    code = HttpStatus.BAD_REQUEST.ordinal,
                    message = throwable.localizedMessage
                )
                ok().body(exception)
            }
        }
    }
}