package org.burgas.javaspring.routing;

import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.dto.exception.ExceptionResponse;
import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.service.IdentityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class IdentityRouting {

    private final IdentityService identityService;

    @Bean
    public RouterFunction<ServerResponse> identityRouter() {
        return RouterFunctions.route()
                .GET(
                        "/api/v1/identities", _ ->
                                ServerResponse
                                        .status(HttpStatus.OK)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(this.identityService.findAll())
                )
                .GET(
                        "/api/v1/identities/by-id", request ->
                                ServerResponse
                                        .status(HttpStatus.OK)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(
                                                this.identityService.findById(
                                                        UUID.fromString(request.param("identityId").orElseThrow())
                                                )
                                        )
                )
                .POST(
                        "/api/v1/identities/create", request ->
                                ServerResponse
                                        .status(HttpStatus.OK)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(this.identityService.create(request.body(IdentityRequest.class)))
                )
                .PUT(
                        "/api/v1/identities/update", request ->
                                ServerResponse
                                        .status(HttpStatus.OK)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(this.identityService.update(request.body(IdentityRequest.class)))
                )
                .DELETE(
                        "/api/v1/identities/delete", request -> {
                            this.identityService.delete(UUID.fromString(request.param("identityId").orElseThrow()));
                            return ServerResponse.ok().build();
                        }
                )
                .onError(
                        Throwable.class,
                        (throwable, _) -> {
                            var exceptionResponse = ExceptionResponse.builder()
                                    .status(HttpStatus.BAD_REQUEST.name())
                                    .code(HttpStatus.BAD_REQUEST.value())
                                    .message(throwable.getLocalizedMessage())
                                    .build();
                            return ServerResponse.badRequest().body(exceptionResponse);
                        }
                )
                .build();
    }
}
