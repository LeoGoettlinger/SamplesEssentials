package org.jdbi.v3.core;

import java.util.function.Consumer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ExtensionContext;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jdbi.v3.core.internal.OnDemandHandleSupplier;

final class LazyHandleSupplier extends AbstractHandleSupplier implements OnDemandHandleSupplier {
   private final Jdbi jdbi;
   private final MemoizingSupplier<Handle> handleHolder = MemoizingSupplier.of(this::createHandle);

   LazyHandleSupplier(Jdbi jdbi) {
      this.jdbi = jdbi;
   }

   @Override
   public ConfigRegistry getConfig() {
      ExtensionContext extensionContext = this.currentExtensionContext();
      return extensionContext != null ? extensionContext.getConfig() : this.jdbi.getConfig();
   }

   @Override
   public Jdbi getJdbi() {
      return this.jdbi;
   }

   @Override
   public Handle getHandle() {
      return this.handleHolder.get();
   }

   private Handle createHandle() {
      return this.jdbi.open().acceptExtensionContext(this.currentExtensionContext());
   }

   @Override
   protected void withHandle(Consumer<Handle> handleConsumer) {
      this.handleHolder.ifInitialized(handleConsumer);
   }

   @Override
   public void close() {
      try {
         super.close();
      } finally {
         this.handleHolder.ifInitialized(Handle::close);
      }
   }
}
