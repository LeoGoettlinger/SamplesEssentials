package org.mineskin.options;

import java.util.concurrent.ScheduledExecutorService;
import org.mineskin.request.backoff.RequestInterval;

public interface IJobCheckOptions {
   ScheduledExecutorService scheduler();

   RequestInterval interval();

   @Deprecated
   int intervalMillis();

   int initialDelayMillis();

   int maxAttempts();

   boolean useEta();
}
