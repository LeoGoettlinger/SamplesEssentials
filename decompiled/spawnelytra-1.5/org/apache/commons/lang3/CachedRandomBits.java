package org.apache.commons.lang3;

import java.util.Objects;
import java.util.Random;

final class CachedRandomBits {
   private static final int MAX_CACHE_SIZE = 268435455;
   private static final int MAX_BITS = 32;
   private static final int BIT_INDEX_MASK = 7;
   private static final int BITS_PER_BYTE = 8;
   private final Random random;
   private final byte[] cache;
   private int bitIndex;

   CachedRandomBits(int cacheSize, Random random) {
      if (cacheSize <= 0) {
         throw new IllegalArgumentException("cacheSize must be positive");
      } else {
         this.cache = cacheSize <= 268435455 ? new byte[cacheSize] : new byte[268435455];
         this.random = Objects.requireNonNull(random, "random");
         this.random.nextBytes(this.cache);
         this.bitIndex = 0;
      }
   }

   public int nextBits(int bits) {
      if (bits <= 32 && bits > 0) {
         int result = 0;
         int generatedBits = 0;

         while (generatedBits < bits) {
            if (this.bitIndex >> 3 >= this.cache.length) {
               assert this.bitIndex == this.cache.length * 8;

               this.random.nextBytes(this.cache);
               this.bitIndex = 0;
            }

            int generatedBitsInIteration = Math.min(8 - (this.bitIndex & 7), bits - generatedBits);
            result <<= generatedBitsInIteration;
            result |= this.cache[this.bitIndex >> 3] >> (this.bitIndex & 7) & (1 << generatedBitsInIteration) - 1;
            generatedBits += generatedBitsInIteration;
            this.bitIndex += generatedBitsInIteration;
         }

         return result;
      } else {
         throw new IllegalArgumentException("number of bits must be between 1 and 32");
      }
   }
}
