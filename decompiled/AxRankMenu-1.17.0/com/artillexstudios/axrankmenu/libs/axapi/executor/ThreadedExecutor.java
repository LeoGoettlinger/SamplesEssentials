package com.artillexstudios.axrankmenu.libs.axapi.executor;

import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadedExecutor implements Runnable, Executor {
   private final Queue<Runnable> jobs = new ArrayDeque<>();
   private final Thread thread;
   private final ReentrantLock lock = new ReentrantLock();
   private final Condition condition = this.lock.newCondition();
   private volatile boolean killed = false;

   public ThreadedExecutor(String threadName) {
      this.thread = new Thread(this, threadName);
      this.thread.start();
   }

   public void stop() {
      this.killed = true;
      this.lock.lock();

      try {
         this.condition.signalAll();
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void execute(Runnable task) {
      this.lock.lock();

      try {
         this.jobs.offer(task);
         this.condition.signalAll();
      } finally {
         this.lock.unlock();
      }
   }

   public CompletableFuture<Void> submit(Runnable task) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      this.execute(() -> {
         try {
            task.run();
         } catch (Throwable var3) {
            future.completeExceptionally(var3);
            return;
         }

         future.complete(null);
      });
      return future;
   }

   @Override
   public void run() {
      while (!this.killed) {
         try {
            Runnable next = this.next();
            if (next != null) {
               next.run();
            }
         } catch (Exception var2) {
            LogUtils.error("An unexpected error occurred while running ThreadedExecutor {}!", this.thread.getName(), var2);
         }
      }
   }

   public Runnable next() throws InterruptedException {
      this.lock.lock();

      Object var1;
      try {
         while (this.jobs.isEmpty() && !this.killed) {
            this.condition.await();
         }

         if (!this.jobs.isEmpty()) {
            return this.jobs.remove();
         }

         var1 = null;
      } finally {
         this.lock.unlock();
      }

      return (Runnable)var1;
   }
}
