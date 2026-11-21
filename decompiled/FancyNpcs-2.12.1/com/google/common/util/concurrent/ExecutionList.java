package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@J2ktIncompatible
@GwtIncompatible
public final class ExecutionList {
   private static final Logger log = Logger.getLogger(ExecutionList.class.getName());
   @CheckForNull
   @GuardedBy("this")
   private ExecutionList.RunnableExecutorPair runnables;
   @GuardedBy("this")
   private boolean executed;

   public void add(Runnable runnable, Executor executor) {
      Preconditions.checkNotNull(runnable, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");
      synchronized (this) {
         if (!this.executed) {
            this.runnables = new ExecutionList.RunnableExecutorPair(runnable, executor, this.runnables);
            return;
         }
      }

      executeListener(runnable, executor);
   }

   public void execute() {
      ExecutionList.RunnableExecutorPair list;
      synchronized (this) {
         if (this.executed) {
            return;
         }

         this.executed = true;
         list = this.runnables;
         this.runnables = null;
      }

      ExecutionList.RunnableExecutorPair reversedList = null;

      while (list != null) {
         ExecutionList.RunnableExecutorPair tmp = list;
         list = list.next;
         tmp.next = reversedList;
         reversedList = tmp;
      }

      while (reversedList != null) {
         executeListener(reversedList.runnable, reversedList.executor);
         reversedList = reversedList.next;
      }
   }

   private static void executeListener(Runnable runnable, Executor executor) {
      try {
         executor.execute(runnable);
      } catch (RuntimeException var3) {
         log.log(Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, (Throwable)var3);
      }
   }

   private static final class RunnableExecutorPair {
      final Runnable runnable;
      final Executor executor;
      @CheckForNull
      ExecutionList.RunnableExecutorPair next;

      RunnableExecutorPair(Runnable runnable, Executor executor, @CheckForNull ExecutionList.RunnableExecutorPair next) {
         this.runnable = runnable;
         this.executor = executor;
         this.next = next;
      }
   }
}
