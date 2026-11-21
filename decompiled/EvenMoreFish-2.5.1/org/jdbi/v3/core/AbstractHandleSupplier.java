package org.jdbi.v3.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jdbi.v3.core.extension.ExtensionContext;
import org.jdbi.v3.core.extension.HandleSupplier;

abstract class AbstractHandleSupplier implements HandleSupplier {
   private final AtomicBoolean closed = new AtomicBoolean();
   private final Deque<ExtensionContext> extensionContexts = new ArrayDeque<>();

   protected AbstractHandleSupplier() {
   }

   @Override
   public <V> V invokeInContext(ExtensionContext extensionContext, Callable<V> task) throws Exception {
      Object var3;
      try {
         this.pushExtensionContext(extensionContext);
         var3 = task.call();
      } finally {
         this.popExtensionContext();
      }

      return (V)var3;
   }

   protected ExtensionContext currentExtensionContext() {
      return this.extensionContexts.peek();
   }

   protected abstract void withHandle(Consumer<Handle> handleConsumer);

   @Override
   public void close() {
      if (this.closed.getAndSet(true)) {
         throw new IllegalStateException("Handle is closed");
      } else {
         this.extensionContexts.clear();
      }
   }

   private void pushExtensionContext(ExtensionContext extensionContext) {
      this.extensionContexts.addFirst(extensionContext);
      this.withHandle(handle -> handle.acceptExtensionContext(extensionContext));
   }

   private void popExtensionContext() {
      this.extensionContexts.pollFirst();
      this.withHandle(h -> h.acceptExtensionContext(this.currentExtensionContext()));
   }
}
