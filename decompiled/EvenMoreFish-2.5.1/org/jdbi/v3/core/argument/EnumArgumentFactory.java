package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.enums.DatabaseValue;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.core.internal.EnumStrategies;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.qualifier.QualifiedType;

class EnumArgumentFactory implements QualifiedArgumentFactory {
   @Override
   public Optional<Argument> build(QualifiedType<?> givenType, Object value, ConfigRegistry config) {
      return ifEnum(givenType.getType()).flatMap(clazz -> makeEnumArgument(givenType, (Enum)value, config));
   }

   static <E extends Enum<E>> Optional<Class<E>> ifEnum(Type type) {
      return type instanceof Class<?> clazz && Enum.class.isAssignableFrom(clazz) ? Optional.of((Class<E>)clazz) : Optional.empty();
   }

   private static <E extends Enum<E>> Optional<Argument> makeEnumArgument(QualifiedType<E> givenType, E value, ConfigRegistry config) {
      boolean byName = EnumStrategy.BY_NAME == config.get(EnumStrategies.class).findStrategy(givenType);
      return byName ? byName(value, config) : byOrdinal(value, config);
   }

   private static <E extends Enum<E>> Optional<Argument> byName(E value, ConfigRegistry config) {
      return makeArgument(12, String.class, value, EnumArgumentFactory::annotatedValue, config);
   }

   private static <E extends Enum<E>> String annotatedValue(E e) {
      return Optional.of(e.getDeclaringClass())
         .map(Unchecked.function(type -> type.getField(e.name())))
         .map(field -> field.getAnnotation(DatabaseValue.class))
         .map(DatabaseValue::value)
         .orElse(e.name());
   }

   private static <E extends Enum<E>> Optional<Argument> byOrdinal(E value, ConfigRegistry config) {
      return makeArgument(4, Integer.class, value, Enum::ordinal, config);
   }

   private static <A, E extends Enum<E>> Optional<Argument> makeArgument(
      int nullType, Class<A> attributeType, E value, Function<E, A> transform, ConfigRegistry config
   ) {
      return value == null ? Optional.of(new NullArgument(nullType)) : config.get(Arguments.class).findFor(attributeType, transform.apply(value));
   }
}
