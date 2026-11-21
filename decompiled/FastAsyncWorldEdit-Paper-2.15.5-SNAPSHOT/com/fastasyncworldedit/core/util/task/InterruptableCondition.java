package com.fastasyncworldedit.core.util.task;

import com.google.common.base.Preconditions;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class InterruptableCondition {
   private final Condition condition;
   private final Thread thread;
   private final Lock lock;

   public InterruptableCondition(Lock lock, Condition condition, Thread thread) {
      Preconditions.checkNotNull(condition);
      Preconditions.checkNotNull(thread);
      Preconditions.checkNotNull(lock);
      this.lock = lock;
      this.condition = condition;
      this.thread = thread;
   }

   public void signal() {
      try {
         this.lock.lock();
         this.condition.signalAll();
      } finally {
         this.lock.unlock();
      }
   }

   public void interrupt() {
      this.thread.interrupt();
   }
}
