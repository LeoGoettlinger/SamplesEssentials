package com.earth2me.essentials.libs.configurate.reference;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class PrefixedNameThreadFactory implements ThreadFactory {
   private final String name;
   private final boolean daemon;
   private final AtomicInteger counter = new AtomicInteger();

   PrefixedNameThreadFactory(final String prefix, final boolean daemon) {
      this.name = prefix.endsWith("-") ? prefix : prefix + "-";
      this.daemon = daemon;
   }

   @Override
   public Thread newThread(final Runnable runnable) {
      Thread ret = new Thread(runnable, this.name + this.counter.getAndIncrement());
      ret.setDaemon(this.daemon);
      return ret;
   }
}
