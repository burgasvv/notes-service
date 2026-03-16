package org.burgas.database

import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import org.burgas.serialization.UUIDSerializer
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object ImageTable : UUIDTable("image") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 250)
    val data = blob("data")
}

class ImageEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : EntityClass<UUID, ImageEntity>(ImageTable)

    var name by ImageTable.name
    var contentType by ImageTable.contentType
    var data by ImageTable.data
}

@Suppress("unused")
enum class Authority {
    ADMIN, USER
}

object IdentityTable : UUIDTable("identity") {
    val authority = enumerationByName<Authority>("authority", 250)
    val username = varchar("username", 250).uniqueIndex()
    val password = varchar("password", 250)
    val email = varchar("email", 250).uniqueIndex()
    val enabled = bool("enabled").default(true)
    val firstname = varchar("firstname", 250)
    val lastname = varchar("lastname", 250)
    val patronymic = varchar("patronymic", 250)
    val imageId = optReference(
        name = "image_id", refColumn = ImageTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    )
}

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : EntityClass<UUID, IdentityEntity>(IdentityTable)

    var authority by IdentityTable.authority
    var username by IdentityTable.username
    var password by IdentityTable.password
    var email by IdentityTable.email
    var enabled by IdentityTable.enabled
    var firstname by IdentityTable.firstname
    var lastname by IdentityTable.lastname
    var patronymic by IdentityTable.patronymic
    var image by ImageEntity optionalReferencedOn IdentityTable.imageId
    val notes by NoteEntity referrersOn NoteTable.identityId
}

object NoteTable : UUIDTable("note") {
    val title = varchar("title", 250)
    val content = text("content")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
}

class NoteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : EntityClass<UUID, NoteEntity>(NoteTable)

    var title by NoteTable.title
    var content by NoteTable.content
    var createdAt by NoteTable.createdAt
    var identity by IdentityEntity referencedOn NoteTable.identityId
    var images by ImageEntity via NoteImageTable
}

object NoteImageTable : Table("note_image") {
    val noteId = reference(
        name = "note_id", refColumn = NoteTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val imageId = reference(
        name = "image_id", refColumn = ImageTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(noteId, imageId))
}

@OptIn(ExperimentalUuidApi::class)
@Suppress("UnusedReceiverParameter")
fun Application.configureDatabase() {

    transaction(db = DatabaseFactory.postgres) {
        SchemaUtils.create(ImageTable, IdentityTable, NoteTable, NoteImageTable)

        val identityId = Uuid.parse("37a4e0be-d873-4776-ad9c-000c57ff99af").toJavaUuid()
        IdentityEntity.findById(identityId) ?: IdentityEntity.new(identityId) {
            this.authority = Authority.ADMIN
            this.username = "burgasvv"
            this.password = BCrypt.hashpw("burgasvv", BCrypt.gensalt())
            this.email = "burgasvv@gmail.com"
            this.enabled = true
            this.firstname = "Бургас"
            this.lastname = "Вячеслав"
            this.patronymic = "Васильевич"
        }
    }
}

@Serializable
data class ImageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null
)

@Serializable
data class IdentityRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val authority: Authority? = null,
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val enabled: Boolean? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null
)

@Serializable
data class IdentityShortResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val username: String? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val image: ImageResponse? = null
)

@Serializable
data class IdentityFullResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val username: String? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val image: ImageResponse? = null,
    val notes: List<NoteShortResponse>? = null
)

@Serializable
data class NoteRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    @Serializable(with = UUIDSerializer::class)
    val identityId: UUID? = null
)

@Serializable
data class NoteShortResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val images: List<ImageResponse>? = null
)

@Serializable
data class NoteFullResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val identity: IdentityShortResponse? = null,
    val images: List<ImageResponse>? = null
)