package org.jdbi.v3.core.array;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

@FunctionalInterface
public interface SqlArrayTypeFactory {
   Optional<SqlArrayType<?>> build(Type elementType, ConfigRegistry config);

   static <T> SqlArrayTypeFactory of(Class<T> elementType, String typeName, Function<T, ?> conversion) {
      Optional<SqlArrayType<?>> arrayType = Optional.of(SqlArrayType.of(typeName, conversion));
      return (t, c) -> elementType == GenericTypes.getErasedType(t) ? arrayType : Optional.empty();
   }
}
