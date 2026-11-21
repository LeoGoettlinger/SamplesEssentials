package de.oliver.plugintests.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Delay {
   private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

   public static void delay(Runnable runnable) {
      ScheduledFuture<?> f = EXECUTOR.schedule(runnable, 10L, TimeUnit.MILLISECONDS);

      try {
         f.get(100L, TimeUnit.MILLISECONDS);
      } catch (ExecutionException | TimeoutException | InterruptedException var3) {
         System.out.println("Delay interrupted: " + var3.getMessage());
      }
   }
}
