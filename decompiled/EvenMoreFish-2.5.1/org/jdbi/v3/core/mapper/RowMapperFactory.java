package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;

@FunctionalInterface
public interface RowMapperFactory {
   Optional<RowMapper<?>> build(Type type, ConfigRegistry config);

   static RowMapperFactory of(Type type, RowMapper<?> mapper) {
      return (t, ctx) -> t.equals(type) ? Optional.of(mapper) : Optional.empty();
   }
}
