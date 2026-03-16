package org.burgas.service

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.io.readByteArray
import org.burgas.database.DatabaseFactory
import org.burgas.database.ImageEntity
import org.burgas.database.ImageResponse
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection
import java.util.*

fun ImageEntity.toImageResponse(): ImageResponse {
    return ImageResponse(
        id = this.id.value,
        name = this.name,
        contentType = this.contentType
    )
}

class ImageService {

    suspend fun findById(imageId: UUID) = newSuspendedTransaction(
        db = DatabaseFactory.postgres, readOnly = true, context = Dispatchers.Default
    ) {
        ImageEntity.findById(imageId) ?: throw IllegalArgumentException("Image not found")
    }

    @OptIn(InternalAPI::class)
    suspend fun uploadIdentityImage(multiPartData: MultiPartData) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val readPart = multiPartData.readPart()!!
        if (readPart.contentType?.contentType == "image") {

            if (readPart is PartData.FileItem) {
                ImageEntity.new {
                    this.name = readPart.name ?: throw IllegalArgumentException("Part name is null")
                    this.contentType = "${readPart.contentType?.contentType}/${readPart.contentType?.contentSubtype}"
                    this.data = ExposedBlob(readPart.provider().readBuffer.readByteArray())
                }

            } else {
                throw IllegalArgumentException("Image multipart is not a file")
            }

        } else {
            throw IllegalArgumentException("Wrong file content type")
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun uploadNoteImages(multiPartData: MultiPartData) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val images: MutableList<ImageEntity> = mutableListOf()
        multiPartData.forEachPart { partData ->
            if (partData.contentType?.contentType != "image") {

                if (partData is PartData.FileItem) {
                    val imageEntity = ImageEntity.new {
                        this.name = partData.name ?: throw IllegalArgumentException("Part name is null")
                        this.contentType =
                            "${partData.contentType?.contentType}/${partData.contentType?.contentSubtype}"
                        this.data = ExposedBlob(partData.provider().readBuffer.readByteArray())
                    }
                    images.add(imageEntity)

                } else {
                    throw IllegalArgumentException("Image multipart is not a file")
                }
            } else {
                throw IllegalArgumentException("Wrong file content type")
            }
        }
        images
    }

    suspend fun removeImages(imageIds: List<UUID>) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ImageEntity.forIds(imageIds).forEach { imageEntity ->
            imageEntity.delete()
        }
    }
}