package com.fastasyncworldedit.core.util.task;

import com.fastasyncworldedit.core.Fawe;
import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class AsyncNotifyQueue implements Closeable {
   private final Lock lock = new ReentrantLock(true);
   private final UncaughtExceptionHandler handler;
   private boolean closed;

   public AsyncNotifyQueue(UncaughtExceptionHandler handler) {
      this.handler = handler;
   }

   public UncaughtExceptionHandler getHandler() {
      return this.handler;
   }

   public <T> Future<T> run(Runnable task) {
      return this.call(() -> {
         task.run();
         return null;
      });
   }

   public <T> Future<T> supply(Supplier<T> task) {
      return this.call(task::get);
   }

   public <T> Future<T> call(Callable<T> task) {
      Future[] self = new Future[1];
      Callable<T> wrapped = () -> {
         if (!this.closed) {
            this.lock.lock();

            try {
               if (!this.closed) {
                  try {
                     return task.call();
                  } catch (Throwable var7) {
                     this.handler.uncaughtException(Thread.currentThread(), var7);
                  }
               }
            } finally {
               this.lock.unlock();
            }
         }

         if (self[0] != null) {
            self[0].cancel(true);
         }

         return null;
      };
      self[0] = Fawe.instance().getQueueHandler().async(wrapped);
      return self[0];
   }

   @Override
   public void close() {
      this.closed = true;
   }

   public boolean isClosed() {
      return this.closed;
   }
}
