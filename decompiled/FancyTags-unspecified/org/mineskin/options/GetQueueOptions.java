package org.mineskin.options;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.mineskin.QueueOptions;

public abstract class GetQueueOptions {
   public static QueueOptions create(ScheduledExecutorService scheduler) {
      return new QueueOptions(scheduler, 100, 5);
   }

   public static QueueOptions create() {
      return create(Executors.newSingleThreadScheduledExecutor());
   }
}
