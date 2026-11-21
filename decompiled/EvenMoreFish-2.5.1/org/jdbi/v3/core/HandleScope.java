package org.jdbi.v3.core;

import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.core.internal.ThreadLocalHandleScope;

public interface HandleScope {
   static HandleScope threadLocal() {
      return new ThreadLocalHandleScope();
   }

   HandleSupplier get();

   void set(HandleSupplier handleSupplier);

   default void set(Handle handle) {
      this.set(ConstantHandleSupplier.of(handle));
   }

   void clear();
}
