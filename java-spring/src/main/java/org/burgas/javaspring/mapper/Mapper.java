package org.burgas.javaspring.mapper;

import org.burgas.javaspring.dto.Request;
import org.burgas.javaspring.dto.Response;
import org.burgas.javaspring.entity.Entity;
import org.springframework.stereotype.Component;

@Component
public interface Mapper<R extends Request, E extends Entity, S extends Response, F extends Response> {

    E toEntity(R request);

    S toShortResponse(E entity);

    F toFullResponse(E entity);

    default <D> D handleData(D requestData, String message) {
        if (requestData == null) {
            throw new IllegalArgumentException(message);
        } else {
            return requestData;
        }
    }
}
