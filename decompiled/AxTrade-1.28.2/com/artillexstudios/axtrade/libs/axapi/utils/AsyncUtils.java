package com.artillexstudios.axtrade.libs.axapi.utils;

import com.artillexstudios.axtrade.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NonNull;

public final class AsyncUtils {
   public static void run(Runnable runnable, boolean async) {
      if (async) {
         AsyncUtils.Holder.EXECUTOR.submit(runnable);
      } else {
         runnable.run();
      }
   }

   public static Future<?> submit(Runnable runnable, boolean async) {
      if (async) {
         return AsyncUtils.Holder.EXECUTOR.submit(runnable);
      } else {
         CompletableFuture<?> future = new CompletableFuture();
         runnable.run();
         future.complete(null);
         return future;
      }
   }

   public static Future<?> submit(Runnable runnable) {
      return AsyncUtils.Holder.EXECUTOR.submit(runnable);
   }

   public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initDelay, long period, TimeUnit timeUnit) {
      return AsyncUtils.Holder.EXECUTOR.scheduleAtFixedRate(runnable, initDelay, period, timeUnit);
   }

   public static ScheduledFuture<?> runLater(Runnable runnable, long delay, TimeUnit timeUnit) {
      return AsyncUtils.Holder.EXECUTOR.schedule(runnable, delay, timeUnit);
   }

   public static ScheduledExecutorService executor() {
      return AsyncUtils.Holder.EXECUTOR;
   }

   public static void stop() {
      try {
         AsyncUtils.Holder.EXECUTOR.shutdown();
         AsyncUtils.Holder.EXECUTOR.awaitTermination(1L, TimeUnit.MINUTES);
      } catch (InterruptedException var1) {
         LogUtils.error("An unexpected error occurred while stopping AsyncUtils!", var1);
      }
   }

   private static final class Holder {
      private static final ScheduledExecutorService EXECUTOR = new ExceptionReportingScheduledThreadPool(
         Math.max(1, FeatureFlags.ASYNC_UTILS_POOL_SIZE.get()), new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(@NonNull Runnable runnable) {
               return new Thread(null, runnable, Nameable.getInstance().getName() + "-Async-Processor-Thread-" + this.counter.getAndIncrement());
            }
         }
      );
   }
}
