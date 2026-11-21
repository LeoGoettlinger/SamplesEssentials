package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class OptionalRowMapperFactory implements RowMapperFactory {
   @Override
   public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
      return Optional.of(type).filter(t -> GenericTypes.getErasedType(t) == Optional.class).flatMap(t -> create(t, config));
   }

   private static Optional<RowMapper<?>> create(Type type, ConfigRegistry config) {
      return config.get(RowMappers.class)
         .findFor(GenericTypes.findGenericParameter(type, Optional.class).orElseThrow(() -> new NoSuchMapperException("No mapper for raw Optional type")))
         .map(mapper -> (r, ctx) -> Optional.ofNullable(mapper.map(r, ctx)));
   }
}
