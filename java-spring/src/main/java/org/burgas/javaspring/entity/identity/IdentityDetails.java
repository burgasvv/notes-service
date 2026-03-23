package org.burgas.javaspring.entity.identity;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record IdentityDetails(Identity identity) implements UserDetails {

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(this.identity.getAuthority());
    }

    @Override
    public @Nullable String getPassword() {
        return this.identity.getPassword();
    }

    @Override
    public @NonNull String getUsername() {
        return this.identity.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return this.identity.getEnabled() || !UserDetails.super.isEnabled();
    }
}
