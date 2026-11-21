package org.mineskin.options;

import java.util.concurrent.ScheduledExecutorService;

public interface IQueueOptions {
   ScheduledExecutorService scheduler();

   int intervalMillis();

   int concurrency();

   default void reportFailure() {
   }
}
