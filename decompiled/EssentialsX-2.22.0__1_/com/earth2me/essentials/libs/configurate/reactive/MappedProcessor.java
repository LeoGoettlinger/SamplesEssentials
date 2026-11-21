package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

class MappedProcessor<I, O> implements Processor.Transactional<I, O> {
   private final Processor.TransactionalIso<O> processor;
   private final AtomicReference<Disposable> disposable = new AtomicReference<>();
   private final CheckedFunction<? super I, ? extends O, TransactionFailedException> mapper;
   @Nullable
   private final Publisher<I> parent;

   MappedProcessor(final CheckedFunction<? super I, ? extends O, TransactionFailedException> mapper, @Nullable final Publisher<I> parent) {
      this.processor = parent == null ? Processor.createTransactional() : Processor.createTransactional(parent.executor());
      this.mapper = mapper;
      this.parent = parent;
   }

   @Override
   public Disposable subscribe(final Subscriber<? super O> subscriber) {
      Disposable ret = this.processor.subscribe(subscriber);
      if (ret != NoOpDisposable.INSTANCE) {
         Disposable ours = this.disposable.updateAndGet(it -> it == null && this.parent != null ? this.parent.subscribe(this) : it);
         if (ours == NoOpDisposable.INSTANCE) {
            this.processor.onClose();
            return NoOpDisposable.INSTANCE;
         } else {
            return () -> {
               ret.dispose();
               if (!this.hasSubscribers()) {
                  Disposable disposable = this.disposable.getAndSet(null);
                  disposable.dispose();
               }
            };
         }
      } else {
         return ret;
      }
   }

   @Override
   public boolean hasSubscribers() {
      return this.processor.hasSubscribers();
   }

   @Override
   public Executor executor() {
      return this.processor.executor();
   }

   @Override
   public void beginTransaction(final I newValue) throws TransactionFailedException {
      this.processor.beginTransaction((O)this.mapper.apply(newValue));
   }

   @Override
   public void commit() {
      this.processor.commit();
   }

   @Override
   public void rollback() {
      this.processor.rollback();
   }

   @Override
   public void onError(final Throwable thrown) {
      this.processor.onError(thrown);
   }

   @Override
   public void onClose() {
      Disposable disposable = this.disposable.getAndSet(null);
      if (disposable != null) {
         disposable.dispose();
      }

      this.processor.onClose();
   }

   @Override
   public void inject(final O element) {
      this.processor.submit(element);
   }

   @Override
   public void fallbackHandler(@Nullable final Subscriber<O> subscriber) {
      this.processor.fallbackHandler(subscriber);
   }

   @Override
   public boolean closeIfUnsubscribed() {
      if (this.processor.closeIfUnsubscribed()) {
         Disposable disposable = this.disposable.getAndSet(null);
         if (disposable != null) {
            disposable.dispose();
         }

         return true;
      } else {
         return false;
      }
   }
}
