package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;

class UntypedNullArgumentFactory implements ArgumentFactory.Preparable {
   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return Optional.empty();
   }

   @Override
   public Optional<Argument> build(Type expectedType, Object value, ConfigRegistry config) {
      return value == null ? Optional.of(config.get(Arguments.class).getUntypedNullArgument()) : Optional.empty();
   }
}
