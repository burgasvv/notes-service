package org.burgas.kotlinspring.service

import org.burgas.kotlinspring.entity.Model
import org.burgas.kotlinspring.entity.Request
import org.burgas.kotlinspring.entity.Response
import org.springframework.stereotype.Service
import java.util.UUID

@Service
interface CrudService<R : Request, M : Model, S : Response, F : Response> {

    fun findEntity(id: UUID): M

    fun findAll(): List<S>

    fun findById(id: UUID): F

    fun create(request: R)

    fun update(request: R)

    fun delete(id: UUID)
}