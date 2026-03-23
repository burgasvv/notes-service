package org.burgas.javaspring.filter;

import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.entity.identity.IdentityDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public final class IdentityFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final List<String> paramUrls = new ArrayList<>(
            List.of(
                    "/api/v1/identities/by-id", "/api/v1/identities/delete",
                    "/api/v1/identities/upload-image", "/api/v1/identities/remove-image"
            )
    );
    private final List<String> bodyUrls = new ArrayList<>(
            List.of(
                    "/api/v1/identities/update", "/api/v1/identities/change-password", "/api/v1/identities/change-status"
            )
    );

    @Override
    public ServerResponse filter(@NonNull ServerRequest request, @NonNull HandlerFunction<ServerResponse> next) throws Exception {

        if (paramUrls.contains(request.path())) {
            var authentication = (Authentication) request.principal().orElseThrow();

            if (authentication.isAuthenticated()) {

                var identityDetails = (IdentityDetails) authentication.getPrincipal();
                var identityId = UUID.fromString(request.param("identityId").orElseThrow());

                assert identityDetails != null;
                if (identityDetails.identity().getId().equals(identityId)) {
                    return next.handle(request);

                } else {
                    throw new IllegalArgumentException("Not authorized");
                }

            } else {
                throw new IllegalArgumentException("Note authenticated");
            }

        } else if (bodyUrls.contains(request.path())) {
            var authentication = (Authentication) request.principal().orElseThrow();

            if (authentication.isAuthenticated()) {

                var identityDetails = (IdentityDetails) authentication.getPrincipal();
                IdentityRequest identityRequest = request.body(IdentityRequest.class);

                assert identityDetails != null;
                if (identityDetails.identity().getId().equals(identityRequest.getId())) {
                    request.attributes().put("identityRequest", identityRequest);
                    return next.handle(request);

                } else {
                    throw new IllegalArgumentException("Not authorized");
                }

            } else {
                throw new IllegalArgumentException("Not authenticated");
            }

        } else {
            return next.handle(request);
        }
    }
}
