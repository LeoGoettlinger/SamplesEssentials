package com.fastasyncworldedit.bukkit.adapter;

import java.util.concurrent.Semaphore;

public class DelegateSemaphore extends Semaphore {
   private final Semaphore delegate;

   public DelegateSemaphore(int permits, Semaphore delegate) {
      super(permits);
      this.delegate = delegate;
   }

   @Override
   public synchronized boolean tryAcquire() {
      if (this.delegate == null) {
         return true;
      } else {
         try {
            this.delegate.acquire();
            return true;
         } catch (InterruptedException var2) {
            return true;
         }
      }
   }

   @Override
   public synchronized void acquire() throws InterruptedException {
      if (this.delegate != null) {
         this.delegate.acquire();
      }
   }

   @Override
   public void release() {
      if (this.delegate != null) {
         this.delegate.release();
      }
   }
}
