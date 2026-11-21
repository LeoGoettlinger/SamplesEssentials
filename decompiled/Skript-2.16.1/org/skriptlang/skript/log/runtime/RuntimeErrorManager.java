package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class RuntimeErrorManager implements Closeable {
   private static RuntimeErrorManager instance;
   static RuntimeErrorFilter standardFilter;
   private final Task task;
   private final Map<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> filterMap = new ConcurrentHashMap<>();

   @Internal
   public static RuntimeErrorManager getInstance() {
      return instance;
   }

   public static void refresh() {
      long frameLength = SkriptConfig.runtimeErrorFrameDuration.value().getAs(Timespan.TimePeriod.TICK);
      if (instance == null) {
         instance = new RuntimeErrorManager(frameLength);
      } else {
         Map<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> oldMap = instance.filterMap;
         instance = new RuntimeErrorManager(frameLength);
         instance.filterMap.putAll(oldMap);
      }

      int errorLimit = SkriptConfig.runtimeErrorLimitTotal.value();
      int errorLineLimit = SkriptConfig.runtimeErrorLimitLine.value();
      int errorLineTimeout = SkriptConfig.runtimeErrorLimitLineTimeout.value();
      int errorTimeoutLength = Math.max(SkriptConfig.runtimeErrorTimeoutDuration.value(), 1);
      Frame.FrameLimit errorLimits = new Frame.FrameLimit(errorLimit, errorLineLimit, errorLineTimeout, errorTimeoutLength);
      int warningLimit = SkriptConfig.runtimeWarningLimitTotal.value();
      int warningLineLimit = SkriptConfig.runtimeWarningLimitLine.value();
      int warningLineTimeout = SkriptConfig.runtimeWarningLimitLineTimeout.value();
      int warningTimeoutLength = Math.max(SkriptConfig.runtimeWarningTimeoutDuration.value(), 1);
      Frame.FrameLimit warningsLimits = new Frame.FrameLimit(warningLimit, warningLineLimit, warningLineTimeout, warningTimeoutLength);
      if (standardFilter == null) {
         standardFilter = new RuntimeErrorFilter(errorLimits, warningsLimits);
      } else {
         standardFilter.setErrorFrameLimits(errorLimits);
         standardFilter.setWarningFrameLimits(warningsLimits);
      }
   }

   public RuntimeErrorManager(long frameLength) {
      this.task = new Task(Skript.getInstance(), frameLength, frameLength, true) {
         @Override
         public void run() {
            for (Entry<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> entry : RuntimeErrorManager.this.filterMap.entrySet()) {
               RuntimeErrorFilter filter = entry.getKey();
               if (filter != null) {
                  Set<RuntimeErrorConsumer> consumers = entry.getValue();
                  Frame errorFrame = filter.getErrorFrame();
                  consumers.forEach(consumer -> consumer.printFrameOutput(errorFrame.getFrameOutput(), Level.SEVERE));
                  errorFrame.nextFrame();
                  Frame warningFrame = filter.getErrorFrame();
                  consumers.forEach(consumer -> consumer.printFrameOutput(warningFrame.getFrameOutput(), Level.WARNING));
                  warningFrame.nextFrame();
               }
            }
         }
      };
   }

   public void error(@NotNull RuntimeError error) {
      for (Entry<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> entry : this.filterMap.entrySet()) {
         RuntimeErrorFilter filter = entry.getKey();
         Set<RuntimeErrorConsumer> consumers = entry.getValue();
         if (filter == null || filter.test(error)) {
            consumers.forEach(consumer -> consumer.printError(error));
         }
      }
   }

   @Deprecated(
      since = "2.13",
      forRemoval = true
   )
   public Frame getErrorFrame() {
      return standardFilter.getErrorFrame();
   }

   @Deprecated(
      since = "2.13",
      forRemoval = true
   )
   public Frame getWarningFrame() {
      return standardFilter.getWarningFrame();
   }

   public void addConsumer(RuntimeErrorConsumer consumer) {
      synchronized (this.filterMap) {
         this.filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
      }
   }

   public void addConsumers(RuntimeErrorConsumer... newConsumers) {
      synchronized (this.filterMap) {
         for (RuntimeErrorConsumer consumer : newConsumers) {
            this.filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
         }
      }
   }

   public boolean removeConsumer(RuntimeErrorConsumer consumer) {
      synchronized (this.filterMap) {
         Set<RuntimeErrorConsumer> set = this.filterMap.get(consumer.getFilter());
         if (set == null) {
            return false;
         } else {
            boolean removed = set.remove(consumer);
            if (set.isEmpty()) {
               this.filterMap.remove(consumer.getFilter());
            }

            return removed;
         }
      }
   }

   public List<RuntimeErrorConsumer> removeAllConsumers() {
      synchronized (this.filterMap) {
         List<RuntimeErrorConsumer> currentConsumers = new ArrayList<>();

         for (Set<RuntimeErrorConsumer> set : this.filterMap.values()) {
            currentConsumers.addAll(set);
         }

         this.filterMap.clear();
         return currentConsumers;
      }
   }

   @Override
   public void close() {
      this.task.close();
   }
}
