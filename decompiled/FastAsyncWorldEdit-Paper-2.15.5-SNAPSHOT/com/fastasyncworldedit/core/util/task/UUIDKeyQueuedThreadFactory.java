package com.fastasyncworldedit.core.util.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class UUIDKeyQueuedThreadFactory implements ThreadFactory {
   private final ThreadGroup group;
   private final AtomicInteger threadNumber = new AtomicInteger(1);
   private final String namePrefix;

   public UUIDKeyQueuedThreadFactory() {
      this.group = new ThreadGroup("UUIDKeyQueuedThreadGroup");
      this.namePrefix = "FAWE UUID-key-queued - ";
   }

   @Override
   public Thread newThread(@Nonnull Runnable r) {
      Thread t = new UUIDKeyQueuedThreadFactory.UUIDKeyQueuedThread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      if (t.isDaemon()) {
         t.setDaemon(false);
      }

      if (t.getPriority() != 5) {
         t.setPriority(5);
      }

      return t;
   }

   public static final class UUIDKeyQueuedThread extends Thread {
      public UUIDKeyQueuedThread(@Nullable ThreadGroup group, Runnable task, @Nonnull String name, long stackSize) {
         super(group, task, name, stackSize);
      }
   }
}
