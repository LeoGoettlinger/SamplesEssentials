package org.jdbi.v3.core.internal;

import java.util.Objects;
import org.jdbi.v3.core.HandleScope;
import org.jdbi.v3.core.extension.HandleSupplier;

public final class ThreadLocalHandleScope implements HandleScope {
   private final ThreadLocal<HandleSupplier> threadLocal = new ThreadLocal<>();

   @Override
   public HandleSupplier get() {
      return this.threadLocal.get();
   }

   @Override
   public void set(HandleSupplier handleSupplier) {
      Objects.requireNonNull(handleSupplier, "handleSupplier is null");
      this.threadLocal.set(handleSupplier);
   }

   @Override
   public void clear() {
      this.threadLocal.remove();
   }
}
