package org.burgas.kotlinspring.mapper

import org.burgas.kotlinspring.entity.Model
import org.burgas.kotlinspring.entity.Request
import org.burgas.kotlinspring.entity.Response
import org.springframework.stereotype.Component

@Component
interface Mapper<in R : Request, M : Model, out S : Response, out F : Response> {

    fun toEntity(request: R): M

    fun toShortResponse(entity: M): S

    fun toFullResponse(entity: M): F
}