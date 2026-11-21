package org.jdbi.v3.core.array;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.internal.IterableLike;

public class SqlArrayArgumentFactory implements ArgumentFactory.Preparable {
   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return IterableLike.elementTypeOf(type)
         .flatMap(config.get(SqlArrayTypes.class)::findFor)
         .map(arrayType -> value -> this.arrayArgument(value, (SqlArrayType<?>)arrayType));
   }

   private Argument arrayArgument(Object value, SqlArrayType<?> arrayType) {
      return (Argument)(value == null ? new NullArgument(2003) : new SqlArrayArgument<>(arrayType, value));
   }
}
