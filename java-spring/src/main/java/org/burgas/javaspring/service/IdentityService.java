package org.burgas.javaspring.service;

import org.burgas.javaspring.dto.identity.IdentityFullResponse;
import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.dto.identity.IdentityShortResponse;
import org.burgas.javaspring.dto.note.NoteFullResponse;
import org.burgas.javaspring.entity.identity.Identity;
import org.burgas.javaspring.mapper.IdentityMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class IdentityService implements CrudService<IdentityRequest, Identity, IdentityShortResponse, IdentityFullResponse> {

    private final IdentityMapper identityMapper;

    @Qualifier(value = "identityRedisTemplate")
    private final RedisTemplate<String, IdentityFullResponse> identityRedisTemplate;

    @Qualifier(value = "noteRedisTemplate")
    private final RedisTemplate<String, NoteFullResponse> noteRedisTemplate;

    private final String identityKey = "identityFullResponse::%s";
    private final String noteKey = "noteFullResponse::%s";

    public IdentityService(
            IdentityMapper identityMapper,
            RedisTemplate<String, IdentityFullResponse> identityRedisTemplate,
            RedisTemplate<String, NoteFullResponse> noteRedisTemplate
    ) {
        this.identityMapper = identityMapper;
        this.identityRedisTemplate = identityRedisTemplate;
        this.noteRedisTemplate = noteRedisTemplate;
    }

    @Override
    public Identity findEntity(UUID id) {
        return this.identityMapper.identityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Identity not found"));
    }

    @Override
    public IdentityFullResponse findById(UUID id) {
        var identityKeyFormat = String.format(identityKey, id);
        if (this.identityRedisTemplate.hasKey(identityKeyFormat)) {
            return this.identityRedisTemplate.opsForValue().get(identityKeyFormat);
        } else {
            var identity = this.findEntity(id);
            var identityFullResponse = this.identityMapper.toFullResponse(identity);
            this.identityRedisTemplate.opsForValue().set(identityKeyFormat, identityFullResponse);
            return identityFullResponse;
        }
    }

    @Override
    public List<IdentityShortResponse> findAll() {
        return this.identityMapper.identityRepository.findAll()
                .stream()
                .map(this.identityMapper::toShortResponse)
                .toList();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public IdentityFullResponse create(IdentityRequest request) {
        var identityFullResponse = this.identityMapper.toFullResponse(
                this.identityMapper.identityRepository.save(this.identityMapper.toEntity(request))
        );
        var identityKeyFormat = String.format(identityKey, identityFullResponse.getId());
        this.identityRedisTemplate.opsForValue().set(identityKeyFormat, identityFullResponse);
        return identityFullResponse;
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public IdentityFullResponse update(IdentityRequest request) {
        if (request.getId() == null) throw new IllegalArgumentException("Identity Request id is null");
        Identity identity = this.identityMapper.identityRepository.save(this.identityMapper.toEntity(request));
        handleCache(identity);
        return this.identityMapper.toFullResponse(identity);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void delete(UUID id) {
        var identity = this.findEntity(id);
        this.identityMapper.identityRepository.delete(identity);
        handleCache(identity);
    }

    private void handleCache(Identity identity) {
        var identityKeyFormat = String.format(this.identityKey, identity.getId());
        if (this.identityRedisTemplate.hasKey(identityKeyFormat)) this.identityRedisTemplate.delete(identityKeyFormat);
        if (!identity.getNotes().isEmpty()) {
            identity.getNotes().forEach(
                    note -> {
                        var noteKeyFormat = String.format(this.noteKey, note.getId());
                        if (this.noteRedisTemplate.hasKey(noteKeyFormat)) this.noteRedisTemplate.delete(noteKeyFormat);
                    }
            );
        }
    }
}
