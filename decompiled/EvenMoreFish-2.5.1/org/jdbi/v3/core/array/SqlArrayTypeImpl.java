package org.jdbi.v3.core.array;

import java.util.function.Function;

class SqlArrayTypeImpl<T> implements SqlArrayType<T> {
   private final String typeName;
   private final Function<T, ?> conversion;

   SqlArrayTypeImpl(String typeName, Function<T, ?> conversion) {
      this.typeName = typeName;
      this.conversion = conversion;
   }

   @Override
   public String getTypeName() {
      return this.typeName;
   }

   @Override
   public Object convertArrayElement(T element) {
      return this.conversion.apply(element);
   }
}
