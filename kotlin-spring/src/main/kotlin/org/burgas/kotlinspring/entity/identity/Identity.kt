package org.burgas.kotlinspring.entity.identity

import jakarta.persistence.*
import org.burgas.kotlinspring.entity.Model
import org.burgas.kotlinspring.entity.Request
import org.burgas.kotlinspring.entity.Response
import org.burgas.kotlinspring.entity.image.Image
import org.burgas.kotlinspring.entity.note.Note
import org.burgas.kotlinspring.entity.note.NoteShortResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Suppress("unused")
enum class Authority : GrantedAuthority {
    ADMIN, USER;

    override fun getAuthority(): String? {
        return this.name
    }
}

@Entity
@Table(name = "identity", schema = "public")
@NamedEntityGraph(
    name = "identity-entity-graph",
    attributeNodes = [
        NamedAttributeNode(value = "image"),
        NamedAttributeNode(value = "notes", subgraph = "notes-subgraph")
    ],
    subgraphs = [
        NamedSubgraph(
            name = "notes-subgraph",
            attributeNodes = [
                NamedAttributeNode(value = "images")
            ]
        )
    ]
)
class Identity : Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    override lateinit var id: UUID

    @Column(name = "authority")
    @Enumerated(value = EnumType.STRING)
    lateinit var authority: Authority

    @Column(name = "username")
    lateinit var username: String

    @Column(name = "password")
    lateinit var password: String

    @Column(name = "email")
    lateinit var email: String

    @Column(name = "enabled")
    var enabled: Boolean = true

    @Column(name = "firstname")
    lateinit var firstname: String

    @Column(name = "lastname")
    lateinit var lastname: String

    @Column(name = "patronymic")
    lateinit var patronymic: String

    @JoinColumn(name = "image_id", referencedColumnName = "id")
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var image: Image? = null

    @OneToMany(mappedBy = "identity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var notes: MutableList<Note> = mutableListOf()
}

class IdentityDetails : UserDetails {

    val identity: Identity

    constructor(identity: Identity) {
        this.identity = identity
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return mutableListOf(this.identity.authority)
    }

    override fun getPassword(): String {
        return this.identity.password
    }

    override fun getUsername(): String {
        return this.identity.email
    }

    override fun isEnabled(): Boolean {
        return this.identity.enabled || !super.isEnabled()
    }
}

data class IdentityRequest(
    override val id: UUID? = null,
    val authority: Authority? = null,
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val enabled: Boolean? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null
) : Request

data class IdentityShortResponse(
    override val id: UUID? = null,
    val username: String? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val image: Image? = null
) : Response

data class IdentityFullResponse(
    override val id: UUID? = null,
    val username: String? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val image: Image? = null,
    val notes: List<NoteShortResponse>? = null
) : Response