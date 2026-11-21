package com.fastasyncworldedit.core.util.task;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FaweBasicThreadFactory implements ThreadFactory {
   private final String nameFormat;
   private final AtomicLong count;

   public FaweBasicThreadFactory(@Nullable String nameFormat) {
      this.nameFormat = nameFormat;
      this.count = nameFormat != null ? new AtomicLong(0L) : null;
   }

   @Override
   public Thread newThread(@Nonnull final Runnable runnable) {
      Thread thread = new FaweBasicThread(runnable);
      if (this.nameFormat != null) {
         thread.setName(String.format(Locale.ROOT, this.nameFormat, this.count.getAndIncrement()));
      }

      if (thread.isDaemon()) {
         thread.setDaemon(false);
      }

      if (thread.getPriority() != 5) {
         thread.setPriority(5);
      }

      return thread;
   }
}
