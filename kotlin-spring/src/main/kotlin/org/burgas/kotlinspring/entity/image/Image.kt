package org.burgas.kotlinspring.entity.image

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.burgas.kotlinspring.entity.Model
import java.util.UUID

@Entity
@Table(name = "image", schema = "public")
class Image : Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    override lateinit var id: UUID

    @Column(name = "name")
    lateinit var name: String

    @Column(name = "content_type")
    lateinit var contentType: String

    @JsonIgnore
    @Column(name = "data")
    lateinit var data: ByteArray
}