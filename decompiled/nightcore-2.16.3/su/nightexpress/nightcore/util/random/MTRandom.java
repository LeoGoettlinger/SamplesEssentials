package su.nightexpress.nightcore.util.random;

import java.util.Random;

@Deprecated
public class MTRandom extends Random {
   private static final int UPPER_MASK = Integer.MIN_VALUE;
   private static final int LOWER_MASK = Integer.MAX_VALUE;
   private static final int N = 624;
   private static final int M = 397;
   private static final int[] MAGIC = new int[]{0, -1727483681};
   private static final int MAGIC_FACTOR1 = 1812433253;
   private static final int MAGIC_FACTOR2 = 1664525;
   private static final int MAGIC_FACTOR3 = 1566083941;
   private static final int MAGIC_MASK1 = -1658038656;
   private static final int MAGIC_MASK2 = -272236544;
   private static final int MAGIC_SEED = 19650218;
   private transient int[] mt;
   private transient int mti;
   private transient int[] ibuf;

   private void setSeed() {
      if (this.mt == null) {
         this.mt = new int[624];
      }

      this.mt[0] = 19650218;

      for (this.mti = 1; this.mti < 624; this.mti++) {
         this.mt[this.mti] = 1812433253 * (this.mt[this.mti - 1] ^ this.mt[this.mti - 1] >>> 30) + this.mti;
      }
   }

   @Override
   public final synchronized void setSeed(long seed) {
      if (this.ibuf == null) {
         this.ibuf = new int[2];
      }

      this.ibuf[0] = (int)seed;
      this.ibuf[1] = (int)(seed >>> 32);
      this.setSeed(this.ibuf);
   }

   @Override
   protected final synchronized int next(int bits) {
      if (this.mti >= 624) {
         int kk;
         for (kk = 0; kk < 227; kk++) {
            int y = this.mt[kk] & -2147483648 | this.mt[kk + 1] & 2147483647;
            this.mt[kk] = this.mt[kk + 397] ^ y >>> 1 ^ MAGIC[y & 1];
         }

         while (kk < 623) {
            int y = this.mt[kk] & -2147483648 | this.mt[kk + 1] & 2147483647;
            this.mt[kk] = this.mt[kk + -227] ^ y >>> 1 ^ MAGIC[y & 1];
            kk++;
         }

         int y = this.mt[623] & -2147483648 | this.mt[0] & 2147483647;
         this.mt[623] = this.mt[396] ^ y >>> 1 ^ MAGIC[y & 1];
         this.mti = 0;
      }

      int y = this.mt[this.mti++];
      y ^= y >>> 11;
      y ^= y << 7 & -1658038656;
      y ^= y << 15 & -272236544;
      y ^= y >>> 18;
      return y >>> 32 - bits;
   }

   public final synchronized void setSeed(int[] buf) {
      int length = buf.length;
      if (length == 0) {
         throw new IllegalArgumentException("Seed buffer may not be empty");
      } else {
         int i = 1;
         int j = 0;
         int k = Math.max(624, length);
         this.setSeed();

         for (; k > 0; k--) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1664525) + buf[j] + j;
            i++;
            j++;
            if (i >= 624) {
               this.mt[0] = this.mt[623];
               i = 1;
            }

            if (j >= length) {
               j = 0;
            }
         }

         for (int var6 = 623; var6 > 0; var6--) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1566083941) - i;
            if (++i >= 624) {
               this.mt[0] = this.mt[623];
               i = 1;
            }
         }

         this.mt[0] = Integer.MIN_VALUE;
      }
   }
}
