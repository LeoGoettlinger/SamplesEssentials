package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.StringJoiner;

public final class ExtensionMethod {
   private final Class<?> type;
   private final Method method;

   public ExtensionMethod(Class<?> type, Method method) {
      this.type = Objects.requireNonNull(type);
      this.method = Objects.requireNonNull(method);
   }

   public Class<?> getType() {
      return this.type;
   }

   public Method getMethod() {
      return this.method;
   }

   @Override
   public String toString() {
      return new StringJoiner(", ", ExtensionMethod.class.getSimpleName() + "[", "]").add("type=" + this.type).add("method=" + this.method).toString();
   }
}
