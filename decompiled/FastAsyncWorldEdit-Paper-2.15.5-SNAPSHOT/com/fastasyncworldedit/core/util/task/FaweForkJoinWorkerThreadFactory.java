package com.fastasyncworldedit.core.util.task;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FaweForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
   private final String nameFormat;
   private final AtomicInteger idCounter;

   public FaweForkJoinWorkerThreadFactory(String nameFormat) {
      this.nameFormat = nameFormat;
      this.idCounter = new AtomicInteger(0);
   }

   @Override
   public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
      ForkJoinWorkerThread worker = new FaweForkJoinThread(pool);
      worker.setName(String.format(this.nameFormat, this.idCounter.getAndIncrement()));
      return worker;
   }
}
