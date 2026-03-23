package org.burgas.javaspring.entity.identity;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {

    ADMIN, USER;

    @Override
    public @NonNull String getAuthority() {
        return this.name();
    }
}
