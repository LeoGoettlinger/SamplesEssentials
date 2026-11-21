package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface Processor<I, O> extends Publisher<O>, Subscriber<I> {
   static <V> Processor.Iso<V> create() {
      return create(ForkJoinPool.commonPool());
   }

   static <V> Processor.Iso<V> create(Executor executor) {
      return new ProcessorImpl<>(executor);
   }

   static <V> Processor.TransactionalIso<V> createTransactional() {
      return createTransactional(ForkJoinPool.commonPool());
   }

   static <V> Processor.TransactionalIso<V> createTransactional(final Executor exec) {
      return new TransactionalProcessorImpl<>(exec);
   }

   default <R> Processor<O, R> map(CheckedFunction<? super O, ? extends R, TransactionFailedException> mapper) {
      return new MappedProcessor<>(mapper, this);
   }

   void inject(O element);

   void fallbackHandler(@Nullable Subscriber<O> subscriber);

   boolean closeIfUnsubscribed();

   public interface Iso<V> extends Processor<V, V> {
      @Override
      default void inject(V element) {
         this.submit(element);
      }
   }

   public interface Transactional<I, O> extends Processor<I, O>, Publisher<O>, TransactionalSubscriber<I> {
   }

   public interface TransactionalIso<V> extends Processor.Transactional<V, V>, Processor.Iso<V> {
   }
}
