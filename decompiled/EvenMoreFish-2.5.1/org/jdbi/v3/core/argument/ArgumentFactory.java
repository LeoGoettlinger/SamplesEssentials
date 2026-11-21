package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.meta.Beta;

@FunctionalInterface
public interface ArgumentFactory {
   Optional<Argument> build(Type type, Object value, ConfigRegistry config);

   @Beta
   public interface Preparable extends ArgumentFactory {
      @Override
      default Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
         return this.prepare(type, config).map(p -> p.apply(value));
      }

      Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config);

      @Deprecated(
         since = "3.15.0",
         forRemoval = true
      )
      default Collection<? extends Type> prePreparedTypes() {
         return Collections.emptyList();
      }
   }
}
