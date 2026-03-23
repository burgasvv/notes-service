package org.burgas.javaspring.service;

import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.burgas.javaspring.entity.image.Image;
import org.burgas.javaspring.repository.ImageRepository;
import org.burgas.javaspring.service.contract.EntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class ImageService implements EntityService<Image> {

    private final ImageRepository imageRepository;

    @Override
    public Image findEntity(UUID id) {
        return this.imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }

    @SneakyThrows
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public Image uploadSingle(Part part) {
        if (part.getContentType().startsWith("image/")) {

            var image = Image.builder()
                    .name(part.getSubmittedFileName())
                    .contentType(part.getContentType())
                    .data(part.getInputStream().readAllBytes())
                    .build();
            return this.imageRepository.save(image);
        } else  {
            throw new IllegalArgumentException("Wrong part content type");
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public List<Image> uploadMultiple(List<Part> parts) {
        return new LinkedList<>() {{
            parts.forEach(
                    part -> {
                        if (part.getContentType().startsWith("image/")) {
                            this.add(uploadSingle(part));
                        } else {
                            throw new IllegalArgumentException("Wrong part content type");
                        }
                    }
            );
        }};
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = {Exception.class, Throwable.class, RuntimeException.class}
    )
    public void remove(final List<UUID> imageIds) {
        var allImages = this.imageRepository.findAllById(imageIds);
        this.imageRepository.deleteAll(allImages);
    }
}
