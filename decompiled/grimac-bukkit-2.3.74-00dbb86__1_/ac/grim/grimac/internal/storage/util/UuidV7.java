package ac.grim.grimac.internal.storage.util;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@ApiStatus.Internal
public final class UuidV7 {
   private static final long TS_MASK = 281474976710655L;
   private static final long VERSION_BITS = 28672L;
   private static final long VARIANT_BITS = Long.MIN_VALUE;
   private static final long RAND_A_MASK = 4095L;
   private static final long RAND_B_MASK = 4611686018427387903L;
   private static final long DETERMINISTIC_LOW_MASK = 2251799813685247L;
   private static final int SEQ_MAX = 16383;
   private static final int SEQ_BITS = 14;
   private static final long SEQ_MASK = 16383L;
   private static final AtomicLong STATE = new AtomicLong(-16384L);

   private UuidV7() {
   }

   public static UUID next() {
      long now = Math.min(System.currentTimeMillis(), 281474976710655L);

      long ts;
      int seq;
      long packed;
      do {
         packed = STATE.get();
         long lastMs = packed >> 14;
         int lastSeq = (int)(packed & 16383L);
         if (now > lastMs) {
            ts = now;
            seq = 0;
         } else if (lastSeq < 16383) {
            ts = lastMs;
            seq = lastSeq + 1;
         } else {
            if (lastMs == 281474976710655L) {
               throw new IllegalStateException("UUIDv7 timestamp exhausted");
            }

            ts = lastMs + 1L;
            seq = 0;
         }
      } while (!STATE.compareAndSet(packed, ts << 14 | seq));

      packed = seq >>> 2 & 4095L;
      long randBHigh = (long)(seq & 3) << 60;
      long randBLow = ThreadLocalRandom.current().nextLong() & 1152921504606846975L;
      long msb = ts << 16 | 28672L | packed;
      long lsb = Long.MIN_VALUE | randBHigh | randBLow;
      return new UUID(msb, lsb);
   }

   public static UUID fromTimestampMs(long tsMs) {
      long ts = clampTimestamp(tsMs);
      ThreadLocalRandom r = ThreadLocalRandom.current();
      long msb = ts << 16 | 28672L | r.nextLong() & 4095L;
      long lsb = Long.MIN_VALUE | r.nextLong() & 4611686018427387903L;
      return new UUID(msb, lsb);
   }

   public static UUID fromTimestampMs(long tsMs, long sequence) {
      long ts = clampTimestamp(tsMs);
      long seq = sequence & Long.MAX_VALUE;
      long randA = seq >>> 51 & 4095L;
      long randB = seq & 2251799813685247L;
      long msb = ts << 16 | 28672L | randA;
      long lsb = Long.MIN_VALUE | randB;
      return new UUID(msb, lsb);
   }

   private static long clampTimestamp(long tsMs) {
      return tsMs <= 0L ? 0L : Math.min(tsMs, 281474976710655L);
   }
}
