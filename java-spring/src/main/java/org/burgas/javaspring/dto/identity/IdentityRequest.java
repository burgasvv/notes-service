package org.burgas.javaspring.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.dto.Request;
import org.burgas.javaspring.entity.identity.Authority;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class IdentityRequest implements Request {

    private UUID id;
    private Authority authority;
    private String username;
    private String password;
    private String email;
    private Boolean enabled;
    private String firstname;
    private String lastname;
    private String patronymic;
}
