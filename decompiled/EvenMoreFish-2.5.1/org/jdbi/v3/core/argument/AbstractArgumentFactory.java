package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;

public abstract class AbstractArgumentFactory<T> implements ArgumentFactory.Preparable {
   private final int sqlType;
   private final AbstractArgumentFactory.ArgumentPredicate isInstance;
   private final Type argumentType;

   protected AbstractArgumentFactory(int sqlType) {
      this.sqlType = sqlType;
      this.argumentType = GenericTypes.findGenericParameter(this.getClass(), AbstractArgumentFactory.class)
         .orElseThrow(() -> new IllegalStateException(this.getClass().getSimpleName() + " must extend AbstractArgumentFactory with a concrete T parameter"));
      if (this.argumentType instanceof Class<?> argumentClass) {
         this.isInstance = (type, value) -> argumentClass.isAssignableFrom(GenericTypes.getErasedType(type)) || argumentClass.isInstance(value);
      } else {
         this.isInstance = (type, value) -> this.argumentType.equals(type) || GenericTypes.isSuperType(this.argumentType, type);
      }
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return this.isInstance.test(type, null) ? Optional.of(value -> this.innerBuild(type, value, config)) : Optional.empty();
   }

   @Deprecated(
      since = "3.39.0",
      forRemoval = true
   )
   @Override
   public Collection<Type> prePreparedTypes() {
      return Collections.singletonList(this.argumentType);
   }

   @Override
   public final Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
      return this.isInstance.test(type, value) ? Optional.of(this.innerBuild(type, value, config)) : Optional.empty();
   }

   private Argument innerBuild(Type type, Object value, ConfigRegistry config) {
      if (value == null) {
         return new NullArgument(this.sqlType);
      } else {
         Argument argument = this.build((T)value, config);
         if (argument == null) {
            throw new UnableToCreateStatementException("Prepared argument " + value + " of type " + type + " failed to build");
         } else {
            return argument;
         }
      }
   }

   protected abstract Argument build(T value, ConfigRegistry config);

   @FunctionalInterface
   private interface ArgumentPredicate {
      boolean test(Type type, Object value);
   }
}
