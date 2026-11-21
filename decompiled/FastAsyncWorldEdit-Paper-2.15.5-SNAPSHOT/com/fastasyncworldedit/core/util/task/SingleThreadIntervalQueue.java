package com.fastasyncworldedit.core.util.task;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.util.TaskManager;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SingleThreadIntervalQueue<T> {
   private final ConcurrentMap<T, Long> objMap = new ConcurrentHashMap<>();
   private final Runnable task;
   private final AtomicBoolean queued = new AtomicBoolean();

   public SingleThreadIntervalQueue(int interval) {
      this.task = new Runnable() {
         @Override
         public void run() {
            long allowedTick = Fawe.instance().getTimer().getTick() - 1L;
            Iterator<Entry<T, Long>> iter = SingleThreadIntervalQueue.this.objMap.entrySet().iterator();

            while (iter.hasNext()) {
               Entry<T, Long> entry = iter.next();
               Long time = entry.getValue();
               if (time < allowedTick) {
                  T obj = entry.getKey();
                  iter.remove();
                  SingleThreadIntervalQueue.this.operate(obj);
               }
            }

            synchronized (SingleThreadIntervalQueue.this.objMap) {
               if (!SingleThreadIntervalQueue.this.objMap.isEmpty()) {
                  TaskManager.taskManager().laterAsync(this, interval);
               } else {
                  SingleThreadIntervalQueue.this.queued.set(false);
               }
            }
         }
      };
   }

   public abstract void operate(T obj);

   public boolean dequeue(T obj) {
      synchronized (this.objMap) {
         return this.objMap.remove(obj) != null;
      }
   }

   public void queue(T obj) {
      synchronized (this.objMap) {
         this.objMap.put(obj, Fawe.instance().getTimer().getTick());
         if (!this.queued.get()) {
            this.queued.set(true);
            TaskManager.taskManager().laterAsync(this.task, 3);
         }
      }
   }
}
