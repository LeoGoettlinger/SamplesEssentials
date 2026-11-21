package org.jdbi.v3.core.array;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class InferredSqlArrayTypeFactory implements SqlArrayTypeFactory {
   private final Type inferredElementType;
   private final SqlArrayType<?> arrayType;

   InferredSqlArrayTypeFactory(SqlArrayType<?> arrayType) {
      this.inferredElementType = GenericTypes.findGenericParameter(arrayType.getClass(), SqlArrayType.class)
         .orElseThrow(() -> new UnsupportedOperationException("Must use a concretely typed SqlArrayType here"));
      this.arrayType = arrayType;
   }

   @Override
   public Optional<SqlArrayType<?>> build(Type elementType, ConfigRegistry config) {
      return this.inferredElementType.equals(elementType) ? Optional.of(this.arrayType) : Optional.empty();
   }
}
