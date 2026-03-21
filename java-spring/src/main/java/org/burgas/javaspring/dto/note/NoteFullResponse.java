package org.burgas.javaspring.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.dto.Response;
import org.burgas.javaspring.dto.identity.IdentityShortResponse;
import org.burgas.javaspring.entity.image.Image;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class NoteFullResponse implements Response {

    private UUID id;
    private String title;
    private String content;
    private String createdAt;
    private IdentityShortResponse identity;
    private List<Image> images;
}
