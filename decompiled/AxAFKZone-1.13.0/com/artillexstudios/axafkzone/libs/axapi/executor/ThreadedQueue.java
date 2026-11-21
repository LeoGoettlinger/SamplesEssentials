package com.artillexstudios.axafkzone.libs.axapi.executor;

import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadedQueue<T extends Runnable> implements Runnable {
   private final Queue<T> jobs = new ArrayDeque<>();
   private final Thread thread;
   private final ReentrantLock lock = new ReentrantLock();
   private final Condition condition = this.lock.newCondition();
   private volatile boolean killed = false;

   public ThreadedQueue(String threadName) {
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

   public void submit(T task) {
      this.lock.lock();

      try {
         this.jobs.offer(task);
         this.condition.signalAll();
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void run() {
      while (!this.killed) {
         try {
            T next = this.next();
            if (next != null) {
               next.run();
            }
         } catch (Exception var2) {
            LogUtils.error("An unexpected error occurred while running ThreadedQueue {}!", this.thread.getName(), var2);
         }
      }
   }

   public T next() throws InterruptedException {
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

      return (T)var1;
   }
}
