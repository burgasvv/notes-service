package org.burgas.javaspring.entity.note;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.entity.Entity;
import org.burgas.javaspring.entity.identity.Identity;
import org.burgas.javaspring.entity.image.Image;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@jakarta.persistence.Entity
@Table(name = "note", schema = "public")
@NamedEntityGraph(
        name = "note-entity-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "identity", subgraph = "identity-subgraph"),
                @NamedAttributeNode(value = "images")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "identity-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode(value = "image")
                        }
                )
        }
)
public final class Note implements Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JoinColumn(name = "identity_id", referencedColumnName = "id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Identity identity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "note_image",
            joinColumns = {
                    @JoinColumn(name = "note_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "image_id", referencedColumnName = "id")
            }
    )
    private List<Image> images = new ArrayList<>();
}