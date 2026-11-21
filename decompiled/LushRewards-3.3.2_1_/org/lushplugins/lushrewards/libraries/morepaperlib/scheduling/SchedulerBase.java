package org.lushplugins.lushrewards.libraries.morepaperlib.scheduling;

import java.util.function.Consumer;

public interface SchedulerBase {
   ScheduledTask run(Runnable var1);

   void run(Consumer<ScheduledTask> var1);
}
