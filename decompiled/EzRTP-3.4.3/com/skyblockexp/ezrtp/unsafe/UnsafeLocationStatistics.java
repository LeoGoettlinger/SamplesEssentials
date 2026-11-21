package com.skyblockexp.ezrtp.unsafe;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class UnsafeLocationStatistics {
   private static final int CAUSE_COUNT = UnsafeLocationCause.values().length;
   private final AtomicLong[] allTimeCounts;
   private final AtomicLong[] windowCounts;
   private final AtomicReference<Instant> windowStart = new AtomicReference<>(Instant.now());
   private volatile long windowStartAttemptCount = 0L;

   public UnsafeLocationStatistics() {
      this.allTimeCounts = new AtomicLong[CAUSE_COUNT];
      this.windowCounts = new AtomicLong[CAUSE_COUNT];

      for (int i = 0; i < CAUSE_COUNT; i++) {
         this.allTimeCounts[i] = new AtomicLong(0L);
         this.windowCounts[i] = new AtomicLong(0L);
      }
   }

   public void recordRejection(UnsafeLocationCause cause) {
      if (cause == null) {
         cause = UnsafeLocationCause.OTHER;
      }

      int idx = cause.ordinal();
      this.allTimeCounts[idx].incrementAndGet();
      this.windowCounts[idx].incrementAndGet();
   }

   public UnsafeLocationStatistics.WindowSnapshot snapshotWindow(long currentTotalAttempts, boolean reset) {
      Instant start = this.windowStart.get();
      Instant end = Instant.now();
      Map<UnsafeLocationCause, Long> counts = new EnumMap<>(UnsafeLocationCause.class);

      for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
         counts.put(cause, this.windowCounts[cause.ordinal()].get());
      }

      long windowAttempts = Math.max(0L, currentTotalAttempts - this.windowStartAttemptCount);
      if (reset) {
         for (AtomicLong counter : this.windowCounts) {
            counter.set(0L);
         }

         this.windowStart.set(end);
         this.windowStartAttemptCount = currentTotalAttempts;
      }

      return new UnsafeLocationStatistics.WindowSnapshot(counts, windowAttempts, start, end);
   }

   public Map<UnsafeLocationCause, Long> getAllTimeCounts() {
      Map<UnsafeLocationCause, Long> result = new EnumMap<>(UnsafeLocationCause.class);

      for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
         result.put(cause, this.allTimeCounts[cause.ordinal()].get());
      }

      return result;
   }

   public long getAllTimeTotal() {
      long sum = 0L;

      for (AtomicLong counter : this.allTimeCounts) {
         sum += counter.get();
      }

      return sum;
   }

   public record WindowSnapshot(Map<UnsafeLocationCause, Long> counts, long windowAttempts, Instant windowStart, Instant windowEnd) {
      public long totalUnsafe() {
         long sum = 0L;

         for (long v : this.counts.values()) {
            sum += v;
         }

         return sum;
      }

      public double unsafeRate() {
         return this.windowAttempts > 0L ? (double)this.totalUnsafe() / this.windowAttempts : 0.0;
      }
   }
}
