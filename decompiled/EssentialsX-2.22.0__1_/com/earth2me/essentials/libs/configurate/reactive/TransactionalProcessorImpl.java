package com.earth2me.essentials.libs.configurate.reactive;

import java.util.Iterator;
import java.util.concurrent.Executor;

class TransactionalProcessorImpl<V> extends AbstractProcessor<V, TransactionalRegistration<V>> implements Processor.TransactionalIso<V> {
   protected TransactionalProcessorImpl(final Executor executor) {
      super(executor);
   }

   @Override
   public void submit(final V item) {
      this.executor.execute(() -> Processor.TransactionalIso.super.submit(item));
   }

   @Override
   public void beginTransaction(final V newValue) throws TransactionFailedException {
      if (this.subscriberCount.get() >= 0) {
         boolean handled = false;
         Iterator<TransactionalRegistration<V>> it = this.registrations.iterator();

         while (it.hasNext()) {
            TransactionalRegistration<V> reg = it.next();

            try {
               handled = true;
               reg.beginTransaction(newValue);
            } catch (TransactionFailedException var6) {
               throw var6;
            } catch (Exception var7) {
               it.remove();
               this.subscriberCount.getAndDecrement();
               reg.onError(var7);
            }
         }

         if (!handled) {
            Subscriber<V> fallback = this.fallbackHandler;
            if (fallback != null) {
               fallback.submit(newValue);
            }
         }
      }
   }

   @Override
   public void commit() {
      this.forEachOrRemove(TransactionalRegistration::commit);
   }

   @Override
   public void rollback() {
      this.forEachOrRemove(TransactionalRegistration::rollback);
   }

   protected TransactionalRegistration<V> createRegistration(final Subscriber<? super V> sub) {
      return (TransactionalRegistration<V>)(sub instanceof TransactionalSubscriber
         ? new TransactionalRegistration.Fully<>(this, (TransactionalSubscriber<? super V>)sub)
         : new TransactionalRegistration.Wrapped<>(this, sub));
   }
}
