package org.jdbi.v3.core.mapper.reflect.internal;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;

public class PojoMapperFactory implements RowMapperFactory {
   @Override
   public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
      return config.get(PojoTypes.class).findFor(type).map(p -> new PojoMapper(type, ""));
   }
}
