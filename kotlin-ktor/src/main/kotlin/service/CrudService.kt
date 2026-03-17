package org.burgas.service

import org.burgas.database.Request
import org.burgas.database.Response
import java.util.UUID

interface CrudService<in R : Request, out S : Response, out F : Response> {

    suspend fun create(request: R): F

    suspend fun findAll(): List<S>

    suspend fun findById(id: UUID): F

    suspend fun update(request: R)

    suspend fun delete(id: UUID)
}