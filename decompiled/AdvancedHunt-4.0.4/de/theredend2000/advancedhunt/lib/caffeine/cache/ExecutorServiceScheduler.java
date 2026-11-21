package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class ExecutorServiceScheduler implements Scheduler, Serializable {
   private static final Logger logger = System.getLogger(ExecutorServiceScheduler.class.getName());
   private static final long serialVersionUID = 1L;
   final ScheduledExecutorService scheduledExecutorService;

   ExecutorServiceScheduler(ScheduledExecutorService scheduledExecutorService) {
      this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService);
   }

   @Override
   public Future<? extends Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Objects.requireNonNull(executor);
      Objects.requireNonNull(command);
      Objects.requireNonNull(unit);
      return (Future<? extends Object>)(this.scheduledExecutorService.isShutdown() ? DisabledFuture.instance() : this.scheduledExecutorService.schedule(() -> {
         try {
            executor.execute(command);
         } catch (Throwable var3) {
            logger.log(Level.WARNING, "Exception thrown when submitting scheduled task", var3);
            throw var3;
         }
      }, delay, unit));
   }
}
