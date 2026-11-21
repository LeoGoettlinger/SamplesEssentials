package org.jdbi.v3.core;

import java.util.function.Consumer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.HandleSupplier;

final class ConstantHandleSupplier extends AbstractHandleSupplier {
   private final Handle handle;

   static HandleSupplier of(Handle handle) {
      return new ConstantHandleSupplier(handle);
   }

   ConstantHandleSupplier(Handle handle) {
      this.handle = handle;
   }

   @Override
   public Jdbi getJdbi() {
      return this.handle.getJdbi();
   }

   @Override
   public ConfigRegistry getConfig() {
      return this.handle.getConfig();
   }

   @Override
   public Handle getHandle() {
      return this.handle;
   }

   @Override
   protected void withHandle(Consumer<Handle> handleConsumer) {
      handleConsumer.accept(this.handle);
   }
}
