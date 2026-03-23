package org.burgas.javaspring.routing;

import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.dto.exception.ExceptionResponse;
import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.filter.IdentityFilter;
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
    private final IdentityFilter identityFilter;

    @Bean
    public RouterFunction<ServerResponse> identityRouter() {
        return RouterFunctions.route()
                .path(
                        "/api/v1/identities", builder -> builder
                                .filter(this.identityFilter)
                                .GET("", _ ->
                                        ServerResponse
                                                .status(HttpStatus.OK)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(this.identityService.findAll())
                                )
                                .GET("/by-id", request ->
                                        ServerResponse
                                                .status(HttpStatus.OK)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(
                                                        this.identityService.findById(
                                                                UUID.fromString(request.param("identityId").orElseThrow())
                                                        )
                                                )
                                )
                                .POST("/create", request -> {
                                            this.identityService.create(request.body(IdentityRequest.class));
                                            return ServerResponse
                                                    .status(HttpStatus.OK)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .build();
                                        }
                                )
                                .PUT("/update", request -> {
                                            var identityRequest = (IdentityRequest) request.attribute("identityRequest").orElseThrow();
                                            this.identityService.update(identityRequest);
                                            return ServerResponse
                                                    .status(HttpStatus.OK)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .build();
                                        }
                                )
                                .DELETE("/delete", request -> {
                                            this.identityService.delete(UUID.fromString(request.param("identityId").orElseThrow()));
                                            return ServerResponse.ok().build();
                                        }
                                )
                                .POST("/upload-image", request -> {
                                            UUID identityId = UUID.fromString(request.param("identityId").orElseThrow());
                                            Part part = request.multipartData().asSingleValueMap().get("image");
                                            this.identityService.uploadImage(identityId, part);
                                            return ServerResponse.ok().build();
                                        }
                                )
                                .DELETE("/remove-image", request -> {
                                            UUID identityId = UUID.fromString(request.param("identityId").orElseThrow());
                                            this.identityService.removeImages(identityId);
                                            return ServerResponse.ok().build();
                                        }
                                )
                                .PUT(
                                        "/change-password", request -> {
                                            var identityRequest = (IdentityRequest) request.attribute("identityRequest").orElseThrow();
                                            this.identityService.changePassword(identityRequest);
                                            return ServerResponse.ok().build();
                                        }
                                )
                                .PUT(
                                        "/change-status", request -> {
                                            var identityRequest = (IdentityRequest) request.attribute("identityRequest").orElseThrow();
                                            this.identityService.changeStatus(identityRequest);
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
                )
                .build();
    }
}
