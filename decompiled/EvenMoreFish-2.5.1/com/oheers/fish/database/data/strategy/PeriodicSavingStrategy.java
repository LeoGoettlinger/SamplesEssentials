package com.oheers.fish.database.data.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PeriodicSavingStrategy<T> implements DataSavingStrategy<T> {
   private final Consumer<Collection<T>> batchSaveFunction;
   private final Queue<T> pendingSaves = new ConcurrentLinkedQueue<>();
   private final ScheduledExecutorService scheduler;

   public PeriodicSavingStrategy(Consumer<Collection<T>> batchSaveFunction, long interval, TimeUnit unit) {
      this.batchSaveFunction = batchSaveFunction;
      this.scheduler = Executors.newSingleThreadScheduledExecutor(namedDaemonThreadFactory("emf-db-save"));
      this.scheduler.scheduleAtFixedRate(this::flush, interval, interval, unit);
   }

   @Override
   public void save(T data) {
      this.pendingSaves.add(data);
   }

   @Override
   public void saveAll(Collection<T> data) {
      this.pendingSaves.addAll(data);
   }

   private void flush() {
      if (!this.pendingSaves.isEmpty()) {
         List<T> toSave = new ArrayList<>();

         while (!this.pendingSaves.isEmpty()) {
            toSave.add(this.pendingSaves.poll());
         }

         this.batchSaveFunction.accept(toSave);
      }
   }

   public void shutdown() {
      this.scheduler.shutdown();
      this.flush();
   }

   private static ThreadFactory namedDaemonThreadFactory(String prefix) {
      String safePrefix = prefix != null && !prefix.isBlank() ? prefix.trim() : "emf-db-save";
      AtomicInteger counter = new AtomicInteger(1);
      return runnable -> {
         Thread thread = new Thread(runnable);
         thread.setName(safePrefix + "-" + counter.getAndIncrement());
         thread.setDaemon(true);
         return thread;
      };
   }
}
