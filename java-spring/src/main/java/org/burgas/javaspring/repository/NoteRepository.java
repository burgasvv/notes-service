package org.burgas.javaspring.repository;

import org.burgas.javaspring.entity.note.Note;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    @Override
    @EntityGraph(value = "note-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    @NonNull Optional<Note> findById(UUID uuid);
}