package org.burgas.javaspring.service;

import jakarta.servlet.http.Part;
import org.burgas.javaspring.cache.KeyUtil;
import org.burgas.javaspring.cache.RedisHandler;
import org.burgas.javaspring.dto.identity.IdentityFullResponse;
import org.burgas.javaspring.dto.identity.IdentityRequest;
import org.burgas.javaspring.dto.identity.IdentityShortResponse;
import org.burgas.javaspring.dto.note.NoteFullResponse;
import org.burgas.javaspring.entity.identity.Identity;
import org.burgas.javaspring.mapper.IdentityMapper;
import org.burgas.javaspring.service.contract.CrudService;
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
public class IdentityService implements
        CrudService<IdentityRequest, Identity, IdentityShortResponse, IdentityFullResponse>, RedisHandler<Identity> {

    private final IdentityMapper identityMapper;
    private final ImageService imageService;

    @Qualifier(value = "identityRedisTemplate")
    private final RedisTemplate<String, IdentityFullResponse> identityRedisTemplate;

    @Qualifier(value = "noteRedisTemplate")
    private final RedisTemplate<String, NoteFullResponse> noteRedisTemplate;

    public IdentityService(
            IdentityMapper identityMapper, ImageService imageService,
            RedisTemplate<String, IdentityFullResponse> identityRedisTemplate,
            RedisTemplate<String, NoteFullResponse> noteRedisTemplate
    ) {
        this.identityMapper = identityMapper;
        this.imageService = imageService;
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
        var identityKeyFormat = String.format(KeyUtil.IDENTITY_KEY, id);
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
    public void create(IdentityRequest request) {
        var identityFullResponse = this.identityMapper.toFullResponse(
                this.identityMapper.identityRepository.save(this.identityMapper.toEntity(request))
        );
        var identityKeyFormat = String.format(KeyUtil.IDENTITY_KEY, identityFullResponse.getId());
        this.identityRedisTemplate.opsForValue().set(identityKeyFormat, identityFullResponse);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void update(IdentityRequest request) {
        if (request.getId() == null) throw new IllegalArgumentException("Identity Request id is null");
        Identity identity = this.identityMapper.identityRepository.save(this.identityMapper.toEntity(request));
        this.handleCache(identity);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void delete(UUID id) {
        var identity = this.findEntity(id);
        this.identityMapper.identityRepository.delete(identity);
        this.handleCache(identity);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void changePassword(final IdentityRequest identityRequest) {
        if (identityRequest.getId() == null) throw new IllegalArgumentException("Identity Request id is null");
        if (identityRequest.getPassword() == null || identityRequest.getPassword().isEmpty())
            throw new IllegalArgumentException("Identity password is null or empty");

        var identity = this.findEntity(identityRequest.getId());
        if (this.identityMapper.passwordEncoder.matches(identityRequest.getPassword(), identity.getPassword()))
            throw new IllegalArgumentException("Passwords matched");

        identity.setPassword(this.identityMapper.passwordEncoder.encode(identityRequest.getPassword()));
        this.handleCache(identity);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void changeStatus(final IdentityRequest identityRequest) {
        if (identityRequest.getId() == null) throw new IllegalArgumentException("Identity Request id is null");
        if (identityRequest.getEnabled() == null) throw new IllegalArgumentException("Identity Request status is null");

        var identity = this.findEntity(identityRequest.getId());
        if (identityRequest.getEnabled() == identity.getEnabled())
            throw new IllegalArgumentException("Statuses matched");

        identity.setEnabled(identityRequest.getEnabled());
        this.handleCache(identity);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void uploadImage(final UUID identityId, final Part part) {
        var identity = this.findEntity(identityId);
        identity.setImage(this.imageService.uploadSingle(part));
        this.handleCache(identity);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void removeImages(final UUID identityId) {
        var identity = this.findEntity(identityId);
        var image = identity.getImage();
        if (image != null) {
            identity.setImage(null);
            this.imageService.remove(List.of(image.getId()));
            this.handleCache(identity);
        } else {
            throw new IllegalArgumentException("Identity image is null");
        }
    }

    @Override
    public void handleCache(Identity identity) {
        var identityKeyFormat = String.format(KeyUtil.IDENTITY_KEY, identity.getId());
        if (this.identityRedisTemplate.hasKey(identityKeyFormat)) this.identityRedisTemplate.delete(identityKeyFormat);
        if (!identity.getNotes().isEmpty()) {
            identity.getNotes().forEach(
                    note -> {
                        var noteKeyFormat = String.format(KeyUtil.NOTE_KEY, note.getId());
                        if (this.noteRedisTemplate.hasKey(noteKeyFormat)) this.noteRedisTemplate.delete(noteKeyFormat);
                    }
            );
        }
    }
}
