package org.burgas.javaspring.routing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class SecurityRouting {

    @Bean
    public RouterFunction<ServerResponse> securityRouter() {
        return RouterFunctions.route()
                .path(
                        "/api/v1/security", builder -> builder
                                .GET("/csrf-token", request -> {
                                            CsrfToken csrfToken = (CsrfToken) request.attribute("_csrf").orElseThrow();
                                            return ServerResponse.ok().body(csrfToken);
                                        }
                                )
                )
                .build();
    }
}
