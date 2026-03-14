package org.burgas.kotlinspring.entity.note

import jakarta.persistence.*
import org.burgas.kotlinspring.entity.Model
import org.burgas.kotlinspring.entity.Request
import org.burgas.kotlinspring.entity.Response
import org.burgas.kotlinspring.entity.identity.Identity
import org.burgas.kotlinspring.entity.identity.IdentityShortResponse
import org.burgas.kotlinspring.entity.image.Image
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "note", schema = "public")
@NamedEntityGraph(
    name = "note-entity-graph",
    attributeNodes = [
        NamedAttributeNode(value = "identity", subgraph = "identity-subgraph"),
        NamedAttributeNode(value = "images"),
    ],
    subgraphs = [
        NamedSubgraph(
            name = "identity-subgraph",
            attributeNodes = [
                NamedAttributeNode(value = "image")
            ]
        )
    ]
)
class Note : Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    override lateinit var id: UUID

    @Column(name = "title")
    lateinit var title: String

    @Column(name = "content")
    lateinit var content: String

    @Column(name = "created_at")
    lateinit var createdAt: LocalDateTime

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", referencedColumnName = "id")
    var identity: Identity? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "note_image",
        joinColumns = [JoinColumn(name = "note_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "image_id", referencedColumnName = "id")]
    )
    var images: MutableList<Image> = mutableListOf()
}

data class NoteRequest(
    override val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    val identityId: UUID? = null
) : Request

data class NoteShortResponse(
    override val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val images: List<Image>? = null
) : Response

data class NoteFullResponse(
    override val id: UUID? = null,
    val title: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val identity: IdentityShortResponse? = null,
    val images: List<Image>? = null
) : Response