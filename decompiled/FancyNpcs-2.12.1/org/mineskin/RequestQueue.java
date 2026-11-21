package org.mineskin;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.mineskin.options.IQueueOptions;

public class RequestQueue {
   private final Queue<Supplier<CompletableFuture<?>>> queue = new ConcurrentLinkedQueue<>();
   private final AtomicInteger running = new AtomicInteger(0);
   private final ScheduledExecutorService executor;
   private final Supplier<Integer> interval;
   private final Supplier<Integer> concurrency;
   private long nextRequest = 0L;

   public RequestQueue(IQueueOptions options) {
      this(options.scheduler(), options::intervalMillis, options::concurrency);
   }

   public RequestQueue(ScheduledExecutorService executor, int interval, int concurrency) {
      this(executor, () -> interval, () -> concurrency);
   }

   public RequestQueue(ScheduledExecutorService executor, Supplier<Integer> interval, Supplier<Integer> concurrency) {
      this.executor = executor;
      this.interval = interval;
      this.concurrency = concurrency;
      this.tickAndSchedule();
   }

   private void tickAndSchedule() {
      try {
         this.tick();
      } catch (Throwable var2) {
         MineSkinClientImpl.LOGGER.log(Level.SEVERE, "Error in request queue tick", var2);
      }

      this.executor.schedule(this::tickAndSchedule, (long)this.interval.get().intValue(), TimeUnit.MILLISECONDS);
   }

   private void tick() {
      if (System.currentTimeMillis() < this.nextRequest) {
         MineSkinClientImpl.LOGGER
            .log(Level.FINER, "Waiting for next request in {0}ms ({1})", new Object[]{this.nextRequest - System.currentTimeMillis(), this.hashCode()});
      } else if (this.running.get() >= this.concurrency.get()) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Skipping request, already running {0} tasks", this.running.get());
      } else {
         Supplier<CompletableFuture<?>> supplier;
         if ((supplier = this.queue.poll()) != null) {
            MineSkinClientImpl.LOGGER.log(Level.FINER, "Processing request, running {0} tasks", this.running.get());
            this.running.incrementAndGet();

            try {
               supplier.get();
            } catch (Throwable var3) {
               this.running.decrementAndGet();
               throw var3;
            }
         }
      }
   }

   public int getInterval() {
      return this.interval.get();
   }

   public int getConcurrency() {
      return this.concurrency.get();
   }

   public void setNextRequest(long nextRequest) {
      this.nextRequest = nextRequest;
   }

   public long getNextRequest() {
      return this.nextRequest;
   }

   public int getRunning() {
      return this.running.get();
   }

   public <T> CompletableFuture<T> submit(Supplier<CompletableFuture<T>> supplier) {
      CompletableFuture<T> future = new CompletableFuture<>();
      this.queue.add(() -> supplier.get().whenComplete((result, throwable) -> {
         this.running.decrementAndGet();
         if (throwable != null) {
            future.completeExceptionally(throwable);
         } else {
            future.complete((T)result);
         }
      }));
      return future;
   }

   public <T> CompletableFuture<T> submit(Supplier<T> supplier, Executor executor) {
      return this.submit(() -> CompletableFuture.supplyAsync(supplier, executor));
   }
}
