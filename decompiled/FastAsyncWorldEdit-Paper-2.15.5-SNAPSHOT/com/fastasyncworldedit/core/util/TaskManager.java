package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public abstract class TaskManager {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static TaskManager IMP;
   static TaskManager INSTANCE;
   private final ForkJoinPool pool = new ForkJoinPool();

   protected TaskManager() {
      INSTANCE = this;
   }

   public static TaskManager taskManager() {
      if (INSTANCE == null) {
         INSTANCE = Fawe.<IFawe>platform().getTaskManager();
      }

      return INSTANCE;
   }

   public abstract int repeat(@Nonnull final Runnable runnable, final int interval);

   public abstract int repeatAsync(@Nonnull final Runnable runnable, final int interval);

   public abstract void async(@Nonnull final Runnable runnable);

   public abstract void task(@Nonnull final Runnable runnable);

   public ForkJoinPool getPublicForkJoinPool() {
      return this.pool;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.7.0"
   )
   public void parallel(Collection<Runnable> runables) {
      for (Runnable run : runables) {
         this.pool.submit(run);
      }

      this.pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.7.0"
   )
   public void parallel(Collection<Runnable> runnables, @Nullable Integer numThreads) {
      if (runnables != null) {
         if (numThreads == null) {
            numThreads = Settings.settings().QUEUE.PARALLEL_THREADS;
         }

         if (numThreads <= 1) {
            for (Runnable run : runnables) {
               if (run != null) {
                  run.run();
               }
            }
         } else {
            int numRuns = runnables.size();
            int amountPerThread = 1 + numRuns / numThreads;
            Runnable[][] split = new Runnable[numThreads][amountPerThread];
            Thread[] threads = new Thread[numThreads];
            int i = 0;
            int j = 0;

            for (Runnable runx : runnables) {
               split[i][j] = runx;
               if (++i >= numThreads) {
                  i = 0;
                  j++;
               }
            }

            for (int var17 = 0; var17 < threads.length; var17++) {
               Runnable[] toRun = split[var17];
               Thread thread = threads[var17] = new Thread(() -> {
                  for (Runnable runxx : toRun) {
                     if (runxx != null) {
                        runxx.run();
                     }
                  }
               });
               thread.start();
            }

            for (Thread thread : threads) {
               try {
                  thread.join();
               } catch (InterruptedException var14) {
                  var14.printStackTrace();
               }
            }
         }
      }
   }

   public void runUnsafe(Runnable run) {
      QueueHandler queue = Fawe.instance().getQueueHandler();
      queue.startUnsafe(Fawe.isMainThread());

      try {
         run.run();
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

      queue.endUnsafe(Fawe.isMainThread());
   }

   public void taskNow(@Nonnull final Runnable runnable, boolean async) {
      if (async) {
         this.async(runnable);
      } else {
         runnable.run();
      }
   }

   public void taskNowMain(@Nonnull final Runnable runnable) {
      if (Fawe.isMainThread()) {
         runnable.run();
      } else {
         this.task(runnable);
      }
   }

   public void taskNowAsync(@Nonnull final Runnable runnable) {
      this.taskNow(runnable, Fawe.isMainThread());
   }

   public void taskSoonMain(@Nonnull final Runnable runnable, boolean async) {
      if (async) {
         this.async(runnable);
      } else {
         this.task(runnable);
      }
   }

   public abstract void later(@Nonnull final Runnable runnable, final int delay);

   public abstract void laterAsync(@Nonnull final Runnable runnable, final int delay);

   public abstract void cancel(final int task);

   public <T> void objectTask(Collection<T> objects, final RunnableVal<T> task, final Runnable whenDone) {
      final Iterator<T> iterator = objects.iterator();
      this.task(new Runnable() {
         @Override
         public void run() {
            long start = System.currentTimeMillis();

            boolean hasNext;
            while ((hasNext = iterator.hasNext()) && System.currentTimeMillis() - start < 5L) {
               task.value = iterator.next();
               task.run();
            }

            if (!hasNext) {
               TaskManager.this.later(whenDone, 1);
            } else {
               TaskManager.this.later(this, 1);
            }
         }
      });
   }

   @Deprecated(
      forRemoval = true,
      since = "2.7.0"
   )
   public void wait(AtomicBoolean running, int timeout) {
      try {
         long start = System.currentTimeMillis();
         synchronized (running) {
            while (running.get()) {
               running.wait(timeout);
               if (running.get() && System.currentTimeMillis() - start > 60000L) {
                  new RuntimeException("FAWE is taking a long time to execute a task (might just be a symptom): ").printStackTrace();
                  LOGGER.info("For full debug information use: /fawe threads");
               }
            }
         }
      } catch (InterruptedException var8) {
         var8.printStackTrace();
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.7.0"
   )
   public void notify(AtomicBoolean running) {
      running.set(false);
      synchronized (running) {
         running.notifyAll();
      }
   }

   public void taskWhenFree(@Nonnull Runnable run) {
      if (Fawe.isMainThread()) {
         run.run();
      } else {
         Fawe.instance().getQueueHandler().sync(run);
      }
   }

   public <T> T syncWhenFree(@Nonnull final RunnableVal<T> function) {
      if (Fawe.isMainThread()) {
         function.run();
         return function.value;
      } else {
         try {
            return Fawe.instance().getQueueHandler().sync((Supplier<T>)function).get();
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public <T> T syncWhenFree(@Nonnull final Supplier<T> supplier) {
      if (Fawe.isMainThread()) {
         return supplier.get();
      } else {
         try {
            return Fawe.instance().getQueueHandler().sync(supplier).get();
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public <T> T sync(@Nonnull final RunnableVal<T> function) {
      return this.sync(function);
   }

   public <T> T sync(final Supplier<T> function) {
      if (Fawe.isMainThread()) {
         return function.get();
      } else {
         try {
            return Fawe.instance().getQueueHandler().sync(function).get();
         } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
         }
      }
   }
}
