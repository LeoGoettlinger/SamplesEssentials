package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.argument.internal.StatementBinder;
import org.jdbi.v3.core.argument.internal.strategies.LoggableBinderArgument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

abstract class DelegatingArgumentFactory implements ArgumentFactory.Preparable {
   private final IdentityHashMap<Class<?>, Function<Object, Argument>> builders = new IdentityHashMap<>();

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return Optional.ofNullable(this.builders.get(GenericTypes.getErasedType(type)));
   }

   @Override
   public Optional<Argument> build(Type expectedType, Object value, ConfigRegistry config) {
      Class<?> expectedClass = GenericTypes.getErasedType(expectedType);
      return Optional.ofNullable(this.builders.get(expectedClass)).map(r -> r.apply(value));
   }

   @Deprecated(
      since = "3.39.0",
      forRemoval = true
   )
   @Override
   public Collection<? extends Type> prePreparedTypes() {
      return this.builders.keySet();
   }

   <T> void register(Class<T> klass, int sqlType, StatementBinder<T> binder) {
      this.builders.put(klass, value -> (Argument)(value == null ? new NullArgument(sqlType) : new LoggableBinderArgument<>(value, binder)));
   }
}
