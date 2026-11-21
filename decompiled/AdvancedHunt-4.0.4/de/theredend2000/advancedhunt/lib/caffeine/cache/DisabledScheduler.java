package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

enum DisabledScheduler implements Scheduler {
   INSTANCE;

   @Override
   public Future<? extends Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Objects.requireNonNull(executor);
      Objects.requireNonNull(command);
      Objects.requireNonNull(unit);
      return DisabledFuture.instance();
   }
}
