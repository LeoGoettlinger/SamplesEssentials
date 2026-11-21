package org.jdbi.v3.core.generic;

import java.lang.reflect.Type;

public abstract class GenericType<T> {
   private final Type type = GenericTypes.findGenericParameter(this.getClass(), GenericType.class)
      .orElseThrow(() -> new UnsupportedOperationException("Missing generic type parameter."));

   protected GenericType() {
   }

   public final Type getType() {
      return this.type;
   }
}
