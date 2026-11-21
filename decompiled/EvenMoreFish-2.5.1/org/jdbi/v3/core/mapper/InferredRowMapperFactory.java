package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class InferredRowMapperFactory implements RowMapperFactory {
   private final Type maps;
   private final RowMapper<?> mapper;

   InferredRowMapperFactory(RowMapper<?> mapper) {
      this.maps = detectType(mapper).orElseThrow(() -> new UnsupportedOperationException("Must use a concretely typed RowMapper here"));
      this.mapper = mapper;
   }

   static Optional<Type> detectType(RowMapper<?> mapper) {
      Optional<Type> type = GenericTypes.findGenericParameter(mapper.getClass(), RowMapper.class);
      return type.filter(t -> t != Object.class);
   }

   @Override
   public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
      return this.maps.equals(type) ? Optional.of(this.mapper) : Optional.empty();
   }
}
