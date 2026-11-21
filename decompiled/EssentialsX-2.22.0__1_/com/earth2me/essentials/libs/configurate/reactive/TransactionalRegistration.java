package com.earth2me.essentials.libs.configurate.reactive;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

interface TransactionalRegistration<V> extends AbstractProcessor.Registration<V> {
   TransactionalProcessorImpl<V> holder();

   @Override
   default void dispose() {
      if (this.holder().registrations.remove(this)) {
         this.holder().subscriberCount.getAndDecrement();
      }
   }

   @Override
   default void submit(final V value) {
      try {
         this.beginTransaction(value);
         this.commit();
      } catch (TransactionFailedException var3) {
         this.rollback();
      }
   }

   void beginTransaction(V value) throws TransactionFailedException;

   void commit();

   void rollback();

   public static class Fully<V> implements TransactionalRegistration<V> {
      private final TransactionalProcessorImpl<V> holder;
      private final TransactionalSubscriber<? super V> sub;
      private final Lock lock = new ReentrantLock();

      Fully(final TransactionalProcessorImpl<V> holder, final TransactionalSubscriber<? super V> sub) {
         this.holder = holder;
         this.sub = sub;
      }

      @Override
      public TransactionalProcessorImpl<V> holder() {
         return this.holder;
      }

      @Override
      public void beginTransaction(final V value) throws TransactionFailedException {
         this.lock.lock();
         this.sub.beginTransaction(value);
      }

      @Override
      public void commit() {
         try {
            this.sub.commit();
         } finally {
            try {
               this.lock.unlock();
            } catch (IllegalMonitorStateException var7) {
            }
         }
      }

      @Override
      public void rollback() {
         try {
            this.sub.rollback();
         } finally {
            try {
               this.lock.unlock();
            } catch (IllegalMonitorStateException var7) {
            }
         }
      }

      @Override
      public void onClose() {
         this.sub.onClose();
      }

      @Override
      public void onError(final Throwable e) {
         this.sub.onError(e);
      }
   }

   public static class Wrapped<V> implements TransactionalRegistration<V> {
      private final AtomicReference<V> active = new AtomicReference<>();
      private final TransactionalProcessorImpl<V> holder;
      private final Subscriber<? super V> sub;

      Wrapped(final TransactionalProcessorImpl<V> holder, final Subscriber<? super V> sub) {
         this.holder = holder;
         this.sub = sub;
      }

      @Override
      public TransactionalProcessorImpl<V> holder() {
         return this.holder;
      }

      @Override
      public void beginTransaction(final V value) {
         this.active.set(value);
      }

      @Override
      public void commit() {
         V active = this.active.getAndSet(null);
         if (active != null) {
            this.sub.submit(active);
         }
      }

      @Override
      public void rollback() {
         this.active.set(null);
      }

      @Override
      public void onClose() {
         this.sub.onClose();
      }

      @Override
      public void onError(final Throwable e) {
         this.sub.onError(e);
      }
   }
}
