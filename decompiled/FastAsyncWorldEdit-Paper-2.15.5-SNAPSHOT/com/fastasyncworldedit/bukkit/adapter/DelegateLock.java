package com.fastasyncworldedit.bukkit.adapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;

public class DelegateLock extends ReentrantLockWithGetOwner {
   private final ReentrantLock parent;
   private volatile boolean modified;
   private final AtomicInteger count;

   public DelegateLock(@Nonnull ReentrantLock parent) {
      this.parent = parent;
      this.count = null;
   }

   public boolean isModified() {
      return this.modified;
   }

   public void setModified(boolean modified) {
      this.modified = modified;
   }

   @Override
   public synchronized void lock() {
      this.modified = true;
      this.parent.lock();
      if (this.count != null) {
         this.count.incrementAndGet();
      }
   }

   @Override
   public synchronized void lockInterruptibly() throws InterruptedException {
      this.parent.lockInterruptibly();
   }

   @Override
   public synchronized boolean tryLock() {
      return this.parent.tryLock();
   }

   @Override
   public synchronized boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
      return this.parent.tryLock(timeout, unit);
   }

   @Override
   public void unlock() {
      this.modified = true;
      this.parent.unlock();
      if (this.count != null && this.count.getAndDecrement() <= 0) {
         this.count.incrementAndGet();
      }
   }

   public Lock getParent() {
      return this.parent;
   }

   @Override
   public synchronized Condition newCondition() {
      return this.parent.newCondition();
   }

   @Override
   public synchronized int getHoldCount() {
      return this.parent.getHoldCount();
   }

   @Override
   public synchronized boolean isHeldByCurrentThread() {
      return this.parent.isHeldByCurrentThread();
   }

   @Override
   public synchronized boolean isLocked() {
      return this.parent.isLocked();
   }

   public void untilFree() {
      ReentrantLock rl = this.parent;
      if (rl.isLocked()) {
         rl.lock();
         rl.unlock();
      }
   }

   @Override
   public synchronized boolean hasWaiters(Condition condition) {
      return this.parent.hasWaiters(condition);
   }

   @Override
   public synchronized int getWaitQueueLength(Condition condition) {
      return this.parent.getWaitQueueLength(condition);
   }

   @Override
   public synchronized String toString() {
      return this.parent.toString();
   }
}
