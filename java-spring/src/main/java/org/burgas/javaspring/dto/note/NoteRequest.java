package org.burgas.javaspring.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.dto.Request;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class NoteRequest implements Request {

    private UUID id;
    private String title;
    private String content;
    private UUID identityId;
}
