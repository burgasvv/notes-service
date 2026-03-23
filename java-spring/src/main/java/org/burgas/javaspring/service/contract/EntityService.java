package org.burgas.javaspring.service.contract;

import org.burgas.javaspring.entity.Entity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface EntityService<E extends Entity> {

    E findEntity(UUID id);
}
