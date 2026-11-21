package org.jdbi.v3.core.array;

import java.util.function.Function;

public interface SqlArrayType<T> {
   String getTypeName();

   Object convertArrayElement(T element);

   default Class<?> getArrayElementClass() {
      return Object.class;
   }

   static <T> SqlArrayType<T> of(String typeName, Function<T, ?> conversion) {
      return new SqlArrayTypeImpl<>(typeName, conversion);
   }
}
