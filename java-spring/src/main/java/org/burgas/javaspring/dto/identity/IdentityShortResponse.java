package org.burgas.javaspring.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.dto.Response;
import org.burgas.javaspring.entity.image.Image;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class IdentityShortResponse implements Response {

    private UUID id;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String patronymic;
    private Image image;
}
