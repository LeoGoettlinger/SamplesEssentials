package com.artillexstudios.axtrade.libs.axapi.libs.hikari.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentBag<T extends ConcurrentBag.IConcurrentBagEntry> implements AutoCloseable {
   private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentBag.class);
   private final CopyOnWriteArrayList<T> sharedList;
   private final boolean useWeakThreadLocals;
   private final ThreadLocal<List<Object>> threadLocalList;
   private final ConcurrentBag.IBagStateListener listener;
   private final AtomicInteger waiters;
   private volatile boolean closed;
   private final SynchronousQueue<T> handoffQueue;

   public ConcurrentBag(ConcurrentBag.IBagStateListener listener) {
      this.listener = listener;
      this.useWeakThreadLocals = this.useWeakThreadLocals();
      this.handoffQueue = new SynchronousQueue<>(true);
      this.waiters = new AtomicInteger();
      this.sharedList = new CopyOnWriteArrayList<>();
      this.threadLocalList = ThreadLocal.withInitial(
         () -> (List<Object>)(this.useWeakThreadLocals ? new ArrayList<>(16) : new FastList<>(ConcurrentBag.IConcurrentBagEntry.class, 16))
      );
   }

   public T borrow(long timeout, TimeUnit timeUnit) throws InterruptedException {
      List<Object> list = this.threadLocalList.get();

      for (int i = list.size() - 1; i >= 0; i--) {
         Object entry = list.remove(i);
         T bagEntry = (T)(this.useWeakThreadLocals ? ((WeakReference)entry).get() : entry);
         if (bagEntry != null && bagEntry.compareAndSet(0, 1)) {
            return bagEntry;
         }
      }

      int waiting = this.waiters.incrementAndGet();

      T bagEntry;
      try {
         Iterator var15 = this.sharedList.iterator();

         T bagEntryx;
         do {
            if (!var15.hasNext()) {
               this.listener.addBagItem(waiting);
               timeout = timeUnit.toNanos(timeout);

               do {
                  long start = ClockSource.currentTime();
                  bagEntry = this.handoffQueue.poll(timeout, TimeUnit.NANOSECONDS);
                  if (bagEntry == null || bagEntry.compareAndSet(0, 1)) {
                     return bagEntry;
                  }

                  timeout -= ClockSource.elapsedNanos(start);
               } while (timeout > 10000L);

               return null;
            }

            bagEntryx = (T)var15.next();
         } while (!bagEntryx.compareAndSet(0, 1));

         if (waiting > 1) {
            this.listener.addBagItem(waiting - 1);
         }

         bagEntry = bagEntryx;
      } finally {
         this.waiters.decrementAndGet();
      }

      return bagEntry;
   }

   public void requite(T bagEntry) {
      bagEntry.setState(0);

      for (int i = 0; this.waiters.get() > 0; i++) {
         if (bagEntry.getState() != 0 || this.handoffQueue.offer(bagEntry)) {
            return;
         }

         if ((i & 0xFF) == 255) {
            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(10L));
         } else {
            Thread.yield();
         }
      }

      List<Object> threadLocalEntries = this.threadLocalList.get();
      if (threadLocalEntries.size() < 16) {
         threadLocalEntries.add(this.useWeakThreadLocals ? new WeakReference(bagEntry) : bagEntry);
      }
   }

   public void add(T bagEntry) {
      if (this.closed) {
         LOGGER.info("ConcurrentBag has been closed, ignoring add()");
         throw new IllegalStateException("ConcurrentBag has been closed, ignoring add()");
      } else {
         this.sharedList.add(bagEntry);

         while (this.waiters.get() > 0 && bagEntry.getState() == 0 && !this.handoffQueue.offer(bagEntry)) {
            Thread.yield();
         }
      }
   }

   public boolean remove(T bagEntry) {
      if (!bagEntry.compareAndSet(1, -1) && !bagEntry.compareAndSet(-2, -1) && !this.closed) {
         LOGGER.warn("Attempt to remove an object from the bag that was not borrowed or reserved: {}", bagEntry);
         return false;
      } else {
         boolean removed = this.sharedList.remove(bagEntry);
         if (!removed && !this.closed) {
            LOGGER.warn("Attempt to remove an object from the bag that does not exist: {}", bagEntry);
         }

         this.threadLocalList.get().remove(bagEntry);
         return removed;
      }
   }

   @Override
   public void close() {
      this.closed = true;
   }

   public List<T> values(int state) {
      List<T> list = this.sharedList.stream().filter(e -> e.getState() == state).collect(Collectors.toList());
      Collections.reverse(list);
      return list;
   }

   public List<T> values() {
      return (List<T>)this.sharedList.clone();
   }

   public boolean reserve(T bagEntry) {
      return bagEntry.compareAndSet(0, -2);
   }

   public void unreserve(T bagEntry) {
      if (bagEntry.compareAndSet(-2, 0)) {
         while (this.waiters.get() > 0 && bagEntry.getState() == 0 && !this.handoffQueue.offer(bagEntry)) {
            Thread.yield();
         }
      } else {
         LOGGER.warn("Attempt to relinquish an object to the bag that was not reserved: {}", bagEntry);
      }
   }

   public int getWaitingThreadCount() {
      return this.waiters.get();
   }

   public int getCount(int state) {
      int count = 0;

      for (T e : this.sharedList) {
         if (e.getState() == state) {
            count++;
         }
      }

      return count;
   }

   public int[] getStateCounts() {
      int[] states = new int[6];

      for (T e : this.sharedList) {
         states[e.getState()]++;
      }

      states[4] = this.sharedList.size();
      states[5] = this.waiters.get();
      return states;
   }

   public int size() {
      return this.sharedList.size();
   }

   public void dumpState() {
      this.sharedList.forEach(entry -> LOGGER.info(entry.toString()));
   }

   private boolean useWeakThreadLocals() {
      try {
         return System.getProperty("com.artillexstudios.axtrade.libs.axapi.libs.hikari.useWeakReferences") != null
            ? Boolean.getBoolean("com.artillexstudios.axtrade.libs.axapi.libs.hikari.useWeakReferences")
            : this.getClass().getClassLoader() != ClassLoader.getSystemClassLoader();
      } catch (SecurityException var2) {
         return true;
      }
   }

   public interface IBagStateListener {
      void addBagItem(int var1);
   }

   public interface IConcurrentBagEntry {
      int STATE_NOT_IN_USE = 0;
      int STATE_IN_USE = 1;
      int STATE_REMOVED = -1;
      int STATE_RESERVED = -2;

      boolean compareAndSet(int var1, int var2);

      void setState(int var1);

      int getState();
   }
}
