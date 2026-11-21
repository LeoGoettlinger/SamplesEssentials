package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;

@FunctionalInterface
public interface ColumnMapperFactory {
   Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config);

   static ColumnMapperFactory of(Type type, ColumnMapper<?> mapper) {
      return (t, c) -> t.equals(type) ? Optional.of(mapper) : Optional.empty();
   }
}
