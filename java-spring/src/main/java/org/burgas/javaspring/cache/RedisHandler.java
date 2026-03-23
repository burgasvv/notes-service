package org.burgas.javaspring.cache;

import org.burgas.javaspring.entity.Entity;
import org.springframework.stereotype.Component;

@Component
public interface RedisHandler<E extends Entity> {

    void handleCache(final E entity);
}
