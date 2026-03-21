package org.burgas.javaspring.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.dto.identity.IdentityFullResponse;
import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.dto.identity.IdentityShortResponse;
import org.burgas.javaspring.entity.identity.Authority;
import org.burgas.javaspring.entity.identity.Identity;
import org.burgas.javaspring.repository.IdentityRepository;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public final class IdentityMapper implements Mapper<IdentityRequest, Identity, IdentityShortResponse, IdentityFullResponse> {

    public final IdentityRepository identityRepository;
    private final ObjectFactory<NoteMapper> noteMapperObjectFactory;

    private NoteMapper getNoteMapper() {
        return this.noteMapperObjectFactory.getObject();
    }

    @Override
    public Identity toEntity(IdentityRequest request) {
        return this.identityRepository.findById(request.getId() == null ? new UUID(0, 0) : request.getId())
                .map(
                        identity -> Identity.builder()
                                .id(identity.getId())
                                .authority(Optional.ofNullable(request.getAuthority()).orElse(identity.getAuthority()))
                                .username(Optional.ofNullable(request.getUsername()).orElse(identity.getUsername()))
                                .password(identity.getPassword())
                                .email(Optional.ofNullable(request.getEmail()).orElse(identity.getEmail()))
                                .enabled(identity.getEnabled())
                                .firstname(Optional.ofNullable(request.getFirstname()).orElse(identity.getFirstname()))
                                .lastname(Optional.ofNullable(request.getLastname()).orElse(identity.getLastname()))
                                .patronymic(Optional.ofNullable(request.getPatronymic()).orElse(identity.getPatronymic()))
                                .build()
                )
                .orElseGet(
                        () -> Identity.builder()
                                .authority(Optional.ofNullable(request.getAuthority()).orElse(Authority.USER))
                                .username(Optional.ofNullable(request.getUsername()).orElseThrow())
                                .password(Optional.ofNullable(request.getPassword()).orElseThrow())
                                .email(Optional.ofNullable(request.getEmail()).orElseThrow())
                                .enabled(Optional.ofNullable(request.getEnabled()).orElse(true))
                                .firstname(Optional.ofNullable(request.getFirstname()).orElseThrow())
                                .lastname(Optional.ofNullable(request.getLastname()).orElseThrow())
                                .patronymic(Optional.ofNullable(request.getPatronymic()).orElseThrow())
                                .build()
                );
    }

    @Override
    public IdentityShortResponse toShortResponse(Identity entity) {
        return IdentityShortResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstname(entity.getFirstname())
                .lastname(entity.getLastname())
                .patronymic(entity.getPatronymic())
                .image(entity.getImage())
                .build();
    }

    @Override
    public IdentityFullResponse toFullResponse(Identity entity) {
        return IdentityFullResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstname(entity.getFirstname())
                .lastname(entity.getLastname())
                .patronymic(entity.getPatronymic())
                .image(entity.getImage())
                .notes(
                        Optional.ofNullable(entity.getNotes())
                                .map(notes -> notes.stream()
                                        .map(note -> this.getNoteMapper().toShortResponse(note)).toList())
                                .orElseGet(ArrayList::new)
                )
                .build();
    }
}
