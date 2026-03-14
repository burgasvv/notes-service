package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.exception.ExceptionResponse
import org.burgas.kotlinspring.entity.identity.IdentityDetails
import org.burgas.kotlinspring.entity.identity.IdentityRequest
import org.burgas.kotlinspring.service.IdentityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router
import java.util.UUID

@Configuration
class IdentityRouting {

    private final val identityService: IdentityService
    private final val listParamRoutes: List<String> = listOf(
        "/api/v1/identities/by-id",
        "/api/v1/identities/delete",
        "/api/v1/identities/upload-image",
        "/api/v1/identities/remove-image"
    )
    private final val listBodyRoutes: List<String> = listOf(
        "/api/v1/identities/update",
        "/api/v1/identities/change-password",
        "/api/v1/identities/change-status"
    )

    constructor(identityService: IdentityService) {
        this.identityService = identityService
    }

    @Bean
    fun identityRoutes() = router {

        "/api/v1/identities".nest {

            filter { request, function ->
                if (listParamRoutes.contains(request.path())) {
                    val authentication = request.principal()
                        .orElseThrow { throw IllegalArgumentException("Not authenticated") } as Authentication

                    if (authentication.isAuthenticated) {
                        val identityDetails = authentication.principal as IdentityDetails
                        val identityId = UUID.fromString(request.param("identityId").orElseThrow())

                        if (identityDetails.identity.id == identityId) {
                            function(request)
                        } else {
                            throw IllegalArgumentException("Identity not authorized")
                        }

                    } else {
                        throw IllegalArgumentException("Identity not authenticated")
                    }

                } else if (listBodyRoutes.contains(request.path())) {
                    val authentication = request.principal()
                        .orElseThrow { throw IllegalArgumentException("Not authenticated") } as Authentication

                    if (authentication.isAuthenticated) {
                        val identityDetails = authentication.principal as IdentityDetails
                        val identityRequest = request.body<IdentityRequest>()
                        if (identityRequest.id == null) throw IllegalArgumentException("Identity Request id is null for authenticated")

                        if (identityDetails.identity.id == identityRequest.id) {
                            request.attributes()["identityRequest"] = identityRequest
                            function(request)
                        } else {
                            throw IllegalArgumentException("Identity not authorized")
                        }

                    } else {
                        throw IllegalArgumentException("Identity not authenticated")
                    }

                } else {
                    function(request)
                }
            }

            GET("") { ok().body(identityService.findAll()) }

            GET("/by-id") {
                val identityFullResponse = identityService.findById(UUID.fromString(it.param("identityId").orElseThrow()))
                ok().body(identityFullResponse)
            }

            POST("/create") {
                identityService.create(it.body<IdentityRequest>())
                ok().build()
            }

            PUT("/update") {
                val identityRequest = it.attributes()["identityRequest"] as IdentityRequest
                identityService.update(identityRequest)
                ok().build()
            }

            DELETE("/delete") {
                identityService.delete(UUID.fromString(it.param("identityId").orElseThrow()))
                ok().build()
            }

            POST("/upload-image") {
                val part = it.multipartData().asSingleValueMap()["image"]!!
                val identityId = UUID.fromString(it.param("identityId").orElseThrow())
                identityService.uploadImage(identityId, part)
                ok().build()
            }

            DELETE("/remove-image") {
                val identityId = UUID.fromString(it.param("identityId").orElseThrow())
                identityService.removeImage(identityId)
                ok().build()
            }

            PUT("/change-password") {
                val identityRequest = it.body<IdentityRequest>()
                identityService.changePassword(identityRequest)
                ok().build()
            }

            PUT("/change-status") {
                val identityRequest = it.body<IdentityRequest>()
                identityService.changeStatus(identityRequest)
                ok().build()
            }

            onError({ true }) { throwable, _ ->
                val exception = ExceptionResponse(
                    status = HttpStatus.BAD_REQUEST.name,
                    code = HttpStatus.BAD_REQUEST.ordinal,
                    message = throwable.localizedMessage
                )
                ok().body(exception)
            }
        }
    }
}