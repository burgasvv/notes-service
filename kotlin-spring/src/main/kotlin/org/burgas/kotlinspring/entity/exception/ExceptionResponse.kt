package org.burgas.kotlinspring.entity.exception

data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String
)