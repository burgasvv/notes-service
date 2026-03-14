package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.service.ImageService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router
import java.io.ByteArrayInputStream
import java.util.*

@Configuration
class ImageRouting {

    private final val imageService: ImageService

    constructor(imageService: ImageService) {
        this.imageService = imageService
    }

    @Bean
    fun imageRouter() = router {
        "/api/v1/images".nest {

            GET("/by-id") {
                val imageId = UUID.fromString(it.param("imageId").orElseThrow())
                val image = imageService.findById(imageId)
                val resource = InputStreamResource(ByteArrayInputStream(image.data))
                ServerResponse.status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType(image.contentType))
                    .body(resource)
            }
        }
    }
}