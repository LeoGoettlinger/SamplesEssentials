package org.skriptlang.skript.log.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class Frame {
   private final Frame.FrameLimit limits;
   private int printed;
   private final Map<ErrorSource.Location, Integer> lineTotals;
   private final Map<ErrorSource.Location, Integer> lineSkipped;
   private final Map<ErrorSource.Location, Integer> timeouts;

   public Frame(Frame.FrameLimit limits) {
      this.limits = limits;
      this.lineTotals = new ConcurrentHashMap<>();
      this.lineSkipped = new ConcurrentHashMap<>();
      this.timeouts = new ConcurrentHashMap<>();
   }

   public boolean add(@NotNull RuntimeError error) {
      ErrorSource.Location location = error.source().location();
      int lineTotal = this.lineTotals.compute(location, (key, count) -> count == null ? 1 : count + 1);
      if (this.timeouts.containsKey(location)) {
         this.lineSkipped.compute(location, (key, count) -> count == null ? 1 : count + 1);
         return false;
      } else if (this.printed < this.limits.totalLimit && lineTotal <= this.limits.lineLimit) {
         this.printed++;
         return true;
      } else {
         this.lineSkipped.compute(location, (key, count) -> count == null ? 1 : count + 1);
         if (lineTotal == this.limits.lineTimeoutLimit) {
            this.timeouts.put(location, this.limits.timeoutDuration);
         }

         return false;
      }
   }

   public void nextFrame() {
      this.printed = 0;
      this.lineTotals.clear();
      this.lineSkipped.clear();
      Iterator<Entry<ErrorSource.Location, Integer>> it = this.timeouts.entrySet().iterator();

      while (it.hasNext()) {
         Entry<ErrorSource.Location, Integer> entry = it.next();
         if (entry.getValue() > 0) {
            entry.setValue(entry.getValue() - 1);
         } else {
            it.remove();
         }
      }
   }

   @Contract(" -> new")
   @NotNull
   public Frame.FrameOutput getFrameOutput() {
      Set<ErrorSource.Location> newTimeouts = new HashSet<>();

      for (Entry<ErrorSource.Location, Integer> entry : this.timeouts.entrySet()) {
         if (entry.getValue() == this.limits.timeoutDuration) {
            newTimeouts.add(entry.getKey());
         }
      }

      return new Frame.FrameOutput(
         Collections.unmodifiableMap(this.lineTotals), Collections.unmodifiableMap(this.lineSkipped), Collections.unmodifiableSet(newTimeouts), this.limits
      );
   }

   public record FrameLimit(int totalLimit, int lineLimit, int lineTimeoutLimit, int timeoutDuration) {
   }

   public record FrameOutput(
      @UnmodifiableView Map<ErrorSource.Location, Integer> totalErrors,
      @UnmodifiableView Map<ErrorSource.Location, Integer> skippedErrors,
      @UnmodifiableView Set<ErrorSource.Location> newTimeouts,
      Frame.FrameLimit frameLimits
   ) {
   }
}
