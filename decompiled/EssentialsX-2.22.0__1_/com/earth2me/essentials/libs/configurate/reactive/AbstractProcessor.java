package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

abstract class AbstractProcessor<V, R extends AbstractProcessor.Registration<V>> implements Processor.Iso<V> {
   private static final int CLOSED_VALUE = -1073741824;
   final AtomicInteger subscriberCount = new AtomicInteger();
   @Nullable
   volatile Subscriber<V> fallbackHandler;
   protected final Set<R> registrations = ConcurrentHashMap.newKeySet();
   protected final Executor executor;

   protected AbstractProcessor(final Executor executor) {
      this.executor = executor;
   }

   @Override
   public Executor executor() {
      return this.executor;
   }

   protected abstract R createRegistration(Subscriber<? super V> sub);

   @Override
   public Disposable subscribe(final Subscriber<? super V> subscriber) {
      if (this.subscriberCount.get() >= 0 && this.subscriberCount.incrementAndGet() > 0) {
         R reg = this.createRegistration(subscriber);
         this.registrations.add(reg);
         return reg;
      } else {
         subscriber.onError(new IllegalStateException("Processor " + this + " is already closed!"));
         this.subscriberCount.set(-1073741824);
         return NoOpDisposable.INSTANCE;
      }
   }

   @Override
   public boolean hasSubscribers() {
      return this.subscriberCount.get() > 0;
   }

   @Override
   public void onError(final Throwable thrown) {
      Processor.Iso.super.onError(thrown);
      this.onClose();
   }

   @Override
   public void onClose() {
      this.executor.execute(() -> {
         this.subscriberCount.set(-1073741824);

         for (AbstractProcessor.Registration<V> reg : this.registrations) {
            try {
               reg.onClose();
            } catch (Exception var4) {
            }
         }

         this.registrations.clear();
      });
   }

   protected void forEachOrRemove(final Consumer<R> processor) {
      Iterator<R> it = this.registrations.iterator();

      while (it.hasNext()) {
         R reg = it.next();

         try {
            processor.accept(reg);
         } catch (Exception var7) {
            Exception t = var7;
            it.remove();
            this.subscriberCount.getAndDecrement();

            try {
               reg.onError(t);
            } catch (Exception var6) {
               Processor.Iso.super.onError(var6);
            }
         }
      }
   }

   @Override
   public void fallbackHandler(@Nullable final Subscriber<V> subscriber) {
      this.fallbackHandler = subscriber;
   }

   @Override
   public boolean closeIfUnsubscribed() {
      this.executor.execute(() -> {
         if (this.subscriberCount.compareAndSet(0, -1073741824)) {
            for (AbstractProcessor.Registration<V> reg : this.registrations) {
               reg.onClose();
            }

            this.registrations.clear();
         }
      });
      return this.subscriberCount.get() <= 0;
   }

   protected interface Registration<V> extends Disposable {
      void submit(V value);

      void onClose();

      void onError(Throwable e);
   }
}
