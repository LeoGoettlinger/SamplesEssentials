package org.apache.commons.lang3.mutable;

import java.util.function.Supplier;

public interface Mutable<T> extends Supplier<T> {
   @Override
   default T get() {
      return this.getValue();
   }

   @Deprecated
   T getValue();

   void setValue(T var1);
}
