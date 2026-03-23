package org.burgas.javaspring.service.contract;

import org.burgas.javaspring.dto.Request;
import org.burgas.javaspring.dto.Response;
import org.burgas.javaspring.entity.Entity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface CrudService<R extends Request, E extends Entity, S extends Response, F extends Response> extends EntityService<E> {

    F findById(UUID id);

    List<S> findAll();

    void create(R request);

    void update(R request);

    void delete(UUID id);
}
