package com.fastasyncworldedit.core.internal.io.parallelgzip;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ParallelGZIPEnvironment {
   @Nonnull
   public static ThreadPoolExecutor newThreadPoolExecutor(@Nonnegative int nthreads) {
      ThreadPoolExecutor executor = new ThreadPoolExecutor(
         nthreads,
         nthreads,
         1L,
         TimeUnit.SECONDS,
         new ArrayBlockingQueue<>(nthreads * 20),
         ParallelGZIPEnvironment.ThreadFactoryHolder.THREAD_FACTORY,
         new CallerRunsPolicy()
      );
      executor.allowCoreThreadTimeOut(true);
      return executor;
   }

   @Nonnull
   public static ExecutorService getSharedThreadPool() {
      return ParallelGZIPEnvironment.ThreadPoolHolder.EXECUTOR;
   }

   private static class ThreadFactoryHolder {
      private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
         private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
         private final AtomicLong counter = new AtomicLong(0L);

         @Override
         public Thread newThread(@Nonnull Runnable r) {
            Thread thread = this.defaultThreadFactory.newThread(r);
            thread.setName("parallelgzip-" + this.counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
         }
      };
   }

   private static class ThreadPoolHolder {
      private static final ExecutorService EXECUTOR = ParallelGZIPEnvironment.newThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
   }
}
