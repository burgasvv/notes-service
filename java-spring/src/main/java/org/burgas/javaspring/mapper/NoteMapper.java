package org.burgas.javaspring.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.javaspring.dto.note.NoteFullResponse;
import org.burgas.javaspring.dto.note.NoteRequest;
import org.burgas.javaspring.dto.note.NoteShortResponse;
import org.burgas.javaspring.entity.note.Note;
import org.burgas.javaspring.repository.NoteRepository;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class NoteMapper implements Mapper<NoteRequest, Note, NoteShortResponse, NoteFullResponse> {

    final NoteRepository noteRepository;
    private final ObjectFactory<IdentityMapper> identityMapperObjectFactory;

    private IdentityMapper getIdentityMapper() {
        return this.identityMapperObjectFactory.getObject();
    }

    @Override
    public Note toEntity(NoteRequest request) {
        return this.noteRepository.findById(request.getId() == null ? new UUID(0, 0) : request.getId())
                .map(
                        note -> {
                            var identityId = Optional.ofNullable(request.getId()).orElse(new UUID(0, 0));
                            var findIdentity = this.getIdentityMapper().identityRepository.findById(identityId)
                                    .orElse(null);
                            var identity = Optional.ofNullable(findIdentity).orElse(note.getIdentity());
                            return Note.builder()
                                    .id(note.getId())
                                    .title(Optional.ofNullable(request.getTitle()).orElse(note.getTitle()))
                                    .content(Optional.ofNullable(request.getContent()).orElse(note.getContent()))
                                    .createdAt(note.getCreatedAt())
                                    .identity(identity)
                                    .build();
                        }
                )
                .orElseGet(
                        () -> {
                            var identityId = Optional.ofNullable(request.getIdentityId()).orElseThrow();
                            var findIdentity = this.getIdentityMapper().identityRepository.findById(identityId)
                                    .orElse(null);
                            var identity = Optional.ofNullable(findIdentity).orElseThrow();
                            return Note.builder()
                                    .title(Optional.ofNullable(request.getTitle()).orElseThrow())
                                    .content(Optional.ofNullable(request.getContent()).orElseThrow())
                                    .createdAt(LocalDateTime.now())
                                    .identity(identity)
                                    .build();
                        }
                );
    }

    @Override
    public NoteShortResponse toShortResponse(Note entity) {
        return NoteShortResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")))
                .images(entity.getImages())
                .build();
    }

    @Override
    public NoteFullResponse toFullResponse(Note entity) {
        return NoteFullResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")))
                .identity(
                        Optional.ofNullable(entity.getIdentity())
                                .map(identity -> this.getIdentityMapper().toShortResponse(identity)).orElse(null)
                )
                .images(entity.getImages())
                .build();
    }
}
