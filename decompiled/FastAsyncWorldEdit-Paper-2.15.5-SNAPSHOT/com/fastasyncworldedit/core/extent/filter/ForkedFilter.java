package com.fastasyncworldedit.core.extent.filter;

import com.fastasyncworldedit.core.queue.Filter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ForkedFilter<T extends ForkedFilter<T>> implements Filter {
   protected final Map<Thread, T> children;

   public ForkedFilter(T root) {
      if (root != null) {
         this.children = root.children;
      } else {
         this.children = new ConcurrentHashMap<>();
         this.children.put(Thread.currentThread(), (T)this);
      }
   }

   @Override
   public final Filter fork() {
      return this.children.computeIfAbsent(Thread.currentThread(), thread -> this.init());
   }

   public abstract T init();

   @Override
   public void join() {
      for (Entry<Thread, T> entry : this.children.entrySet()) {
         T filter = entry.getValue();
         if (filter != this) {
            this.join(filter);
         }
      }

      this.children.clear();
   }

   public abstract void join(T filter);
}
