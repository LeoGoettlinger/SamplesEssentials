package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;

public class SetObjectArgumentFactory implements ArgumentFactory.Preparable {
   private final Map<Class<?>, Integer> supportedTypes;

   protected SetObjectArgumentFactory(Map<Class<?>, Integer> types) {
      this.supportedTypes = types;
   }

   public static ArgumentFactory forClasses(Map<Class<?>, Integer> types) {
      return new SetObjectArgumentFactory(new HashMap<>(types));
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return Optional.of(type)
         .filter(Class.class::isInstance)
         .map(Class.class::cast)
         .map(this.supportedTypes::get)
         .map(sqlType -> value -> ObjectArgument.of(value, sqlType));
   }

   @Deprecated(
      since = "3.39.0",
      forRemoval = true
   )
   @Override
   public Collection<? extends Type> prePreparedTypes() {
      return this.supportedTypes.keySet();
   }
}
