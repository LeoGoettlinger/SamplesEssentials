package org.mineskin.options;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.mineskin.QueueOptions;

public abstract class GenerateQueueOptions {
   public static QueueOptions create(ScheduledExecutorService scheduler) {
      return new QueueOptions(scheduler, 200, 1);
   }

   public static QueueOptions create() {
      return create(Executors.newSingleThreadScheduledExecutor());
   }

   public static AutoGenerateQueueOptions createAuto(ScheduledExecutorService scheduler) {
      return new AutoGenerateQueueOptions(scheduler);
   }

   public static AutoGenerateQueueOptions createAuto() {
      return createAuto(Executors.newSingleThreadScheduledExecutor());
   }
}
