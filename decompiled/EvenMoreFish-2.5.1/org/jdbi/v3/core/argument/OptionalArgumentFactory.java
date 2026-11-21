package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class OptionalArgumentFactory extends DelegatingArgumentFactory {
   OptionalArgumentFactory() {
      this.register(OptionalInt.class, 4, (p, i, v) -> {
         if (v.isPresent()) {
            p.setInt(i, v.getAsInt());
         } else {
            p.setNull(i, 4);
         }
      });
      this.register(OptionalLong.class, -5, (p, i, v) -> {
         if (v.isPresent()) {
            p.setLong(i, v.getAsLong());
         } else {
            p.setNull(i, -5);
         }
      });
      this.register(OptionalDouble.class, 8, (p, i, v) -> {
         if (v.isPresent()) {
            p.setDouble(i, v.getAsDouble());
         } else {
            p.setNull(i, 8);
         }
      });
   }

   @Override
   public Optional<Argument> build(Type expectedType, Object value, ConfigRegistry config) {
      if (value instanceof Optional<?> maybeValue) {
         Object nestedValue = maybeValue.orElse(null);
         Type nestedType = findOptionalType(expectedType, nestedValue);
         return config.get(Arguments.class).findFor(nestedType, nestedValue);
      } else {
         return super.build(expectedType, value, config);
      }
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return Optional.class.equals(GenericTypes.getErasedType(type))
         ? config.get(Arguments.class).prepareFor(findOptionalType(type, null)).map(af -> opt -> af.apply(((Optional)opt).orElse(null)))
         : super.prepare(type, config);
   }

   private static Type findOptionalType(Type wrapperType, Object nestedValue) {
      if (GenericTypes.getErasedType(wrapperType).equals(Optional.class)) {
         Optional<Type> nestedType = GenericTypes.findGenericParameter(wrapperType, Optional.class);
         if (nestedType.isPresent()) {
            return nestedType.get();
         }
      }

      return nestedValue == null ? Object.class : nestedValue.getClass();
   }
}
