package org.burgas.kotlinspring.service

import jakarta.servlet.http.Part
import org.burgas.kotlinspring.entity.image.Image
import org.burgas.kotlinspring.repository.ImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
class ImageService {

    private final val imageRepository: ImageRepository

    constructor(imageRepository: ImageRepository) {
        this.imageRepository = imageRepository
    }

    fun findById(id: UUID): Image {
        return this.imageRepository.findById(id).orElseThrow { throw IllegalArgumentException("Image not found") }
    }

    fun upload(part: Part): Image {
        if (!part.contentType.startsWith("image/")) throw IllegalArgumentException("File must be image type")
        val image = Image().apply {
            this.name = part.submittedFileName
            this.contentType = part.contentType
            this.data = part.inputStream.readAllBytes()
        }
        return this.imageRepository.save(image)
    }

    fun remove(image: Image) {
        this.imageRepository.delete(image)
    }
}