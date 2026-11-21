package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;

public class DirectArgumentFactory implements ArgumentFactory {
   @Override
   public Optional<Argument> build(Type expectedType, Object value, ConfigRegistry config) {
      return value instanceof Argument argument ? Optional.of(argument) : Optional.empty();
   }
}
