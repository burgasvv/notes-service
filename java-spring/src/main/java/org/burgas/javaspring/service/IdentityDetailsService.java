package org.burgas.javaspring.service;

import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.entity.identity.IdentityDetails;
import org.burgas.javaspring.mapper.IdentityMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class IdentityDetailsService implements UserDetailsService {

    private final IdentityMapper identityMapper;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return new IdentityDetails(
                this.identityMapper.identityRepository.findIdentityByEmail(username)
                        .orElseThrow(() -> new IllegalArgumentException("Identity not found for authentication"))
        );
    }
}
