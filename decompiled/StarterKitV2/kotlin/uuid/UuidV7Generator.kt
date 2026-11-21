package kotlin.uuid

import kotlin.concurrent.atomics.AtomicLong
import kotlin.time.Clock

private object UuidV7Generator {
   private const val TIMESTAMP_BIAS_BITS: Int = 16
   private const val VERSION_MASK: Int = 28672
   private const val OVERFLOW_MASK: Long = 32768L
   private final val timestampAndCounter: AtomicLong

   @ExperimentalUuidApi
   public fun generate(clock: Clock): Uuid {
      val newCounter: ByteArray = new byte[10];
      UuidKt.secureRandomBytes(newCounter);
      val var12: Int = (newCounter[8] and 7) shl 8 or newCounter[9] and 255 or 28672;

      var var13: Long;
      while (true) {
         val variantAndRandB: Long = timestampAndCounter.get();
         val currentTimeMillis: Long = clock.now().toEpochMilliseconds();
         val previousTimeMillis: Long = variantAndRandB ushr 16;
         if (variantAndRandB ushr 16 < currentTimeMillis) {
            var13 = currentTimeMillis shl 16 or var12;
            if (timestampAndCounter.compareAndSet(variantAndRandB, currentTimeMillis shl 16 or (long)var12)) {
               break;
            }
         } else {
            var13 = variantAndRandB + 1L;
            if ((variantAndRandB + 1L and 32768L) != 0L) {
               var13 = previousTimeMillis + 1L shl 16 or var12;
            }

            if (timestampAndCounter.compareAndSet(variantAndRandB, var13)) {
               break;
            }
         }
      }

      newCounter[0] = (byte)((byte)(newCounter[0] and 63) or -128);
      return Uuid.Companion.fromLongs(var13, UuidKt.getLongAt(newCounter, 0));
   }
}
