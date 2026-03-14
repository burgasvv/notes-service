package org.burgas.kotlinspring.repository

import org.burgas.kotlinspring.entity.note.Note
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface NoteRepository : JpaRepository<Note, UUID> {

    @EntityGraph(value = "note-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    override fun findById(id: UUID): Optional<Note>
}