package com.fastasyncworldedit.core.math.sparsebits;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SparseBitSet implements Cloneable, Serializable {
   protected transient int compactionCount;
   static int compactionCountDefault = 2;
   protected transient long[][][] bits;
   protected transient int bitsLength;
   protected static final int LENGTH4 = 64;
   protected static final int INDEX_SIZE = 31;
   protected static final int LEVEL4 = 6;
   protected static final int LEVEL3 = 5;
   protected static final int LEVEL2 = 5;
   protected static final int LEVEL1 = 15;
   protected static final int MAX_LENGTH1 = 32768;
   protected static final int LENGTH2 = 32;
   protected static final int LENGTH3 = 32;
   protected static final int SHIFT3 = 6;
   protected static final int MASK3 = 31;
   protected static final int SHIFT2 = 5;
   protected static final int UNIT = 65536;
   protected static final int MASK2 = 31;
   protected static final int SHIFT1 = 10;
   protected static final int LENGTH2_SIZE = 31;
   protected static final int LENGTH3_SIZE = 31;
   protected static final int LENGTH4_SIZE = 63;
   protected transient SparseBitSet.Cache cache;
   protected transient long[] spare;
   static final long[] ZERO_BLOCK = new long[32];
   private static final long serialVersionUID = -6663013367427929992L;
   protected static final transient SparseBitSet.AndStrategy andStrategy = new SparseBitSet.AndStrategy();
   protected static final transient SparseBitSet.AndNotStrategy andNotStrategy = new SparseBitSet.AndNotStrategy();
   protected static final transient SparseBitSet.ClearStrategy clearStrategy = new SparseBitSet.ClearStrategy();
   protected static final transient SparseBitSet.CopyStrategy copyStrategy = new SparseBitSet.CopyStrategy();
   protected transient SparseBitSet.EqualsStrategy equalsStrategy;
   protected static final transient SparseBitSet.FlipStrategy flipStrategy = new SparseBitSet.FlipStrategy();
   protected static transient SparseBitSet.IntersectsStrategy intersectsStrategy = new SparseBitSet.IntersectsStrategy();
   protected static final transient SparseBitSet.OrStrategy orStrategy = new SparseBitSet.OrStrategy();
   protected static final transient SparseBitSet.SetStrategy setStrategy = new SparseBitSet.SetStrategy();
   protected transient SparseBitSet.UpdateStrategy updateStrategy;
   protected static final transient SparseBitSet.XorStrategy xorStrategy = new SparseBitSet.XorStrategy();

   protected SparseBitSet(int capacity, int compactionCount) throws NegativeArraySizeException {
      if (capacity < 0) {
         throw new NegativeArraySizeException("(requested capacity=" + capacity + ") < 0");
      } else {
         this.resize(capacity - 1);
         this.compactionCount = compactionCount;
         this.constructorHelper();
         this.statisticsUpdate();
      }
   }

   public SparseBitSet() {
      this(1, compactionCountDefault);
   }

   public SparseBitSet(int nbits) throws NegativeArraySizeException {
      this(nbits, compactionCountDefault);
   }

   public void and(int i, boolean value) throws IndexOutOfBoundsException {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         if (!value) {
            this.clear(i);
         }
      }
   }

   public void and(int i, int j, SparseBitSet b) throws IndexOutOfBoundsException {
      this.setScanner(i, j, b, andStrategy);
   }

   public void and(SparseBitSet b) {
      this.nullify(Math.min(this.bits.length, b.bits.length));
      this.setScanner(0, Math.min(this.bitsLength, b.bitsLength), b, andStrategy);
   }

   public static SparseBitSet and(SparseBitSet a, SparseBitSet b) {
      SparseBitSet result = a.clone();
      result.and(b);
      return result;
   }

   public void andNot(int i, boolean value) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         if (value) {
            this.clear(i);
         }
      }
   }

   public void andNot(int i, int j, SparseBitSet b) throws IndexOutOfBoundsException {
      this.setScanner(i, j, b, andNotStrategy);
   }

   public void andNot(SparseBitSet b) {
      this.setScanner(0, Math.min(this.bitsLength, b.bitsLength), b, andNotStrategy);
   }

   public static SparseBitSet andNot(SparseBitSet a, SparseBitSet b) {
      SparseBitSet result = a.clone();
      result.andNot(b);
      return result;
   }

   public int cardinality() {
      this.statisticsUpdate();
      return this.cache.cardinality;
   }

   public void clear(int i) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else if (i < this.bitsLength) {
         int w = i >> 6;
         long[][] a2;
         if ((a2 = this.bits[w >> 10]) != null) {
            long[] a3;
            if ((a3 = a2[w >> 5 & 31]) != null) {
               a3[w & 31] = a3[w & 31] & ~(1L << i);
               this.cache.hash = 0;
            }
         }
      }
   }

   public void clear(int i, int j) throws IndexOutOfBoundsException {
      this.setScanner(i, j, null, clearStrategy);
   }

   public void clear() {
      this.nullify(0);
   }

   public SparseBitSet clone() {
      try {
         SparseBitSet result = (SparseBitSet)super.clone();
         result.bits = null;
         result.resize(1);
         result.constructorHelper();
         result.equalsStrategy = null;
         result.setScanner(0, this.bitsLength, this, copyStrategy);
         return result;
      } catch (CloneNotSupportedException var2) {
         throw new InternalError(var2.getMessage());
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof SparseBitSet)) {
         return false;
      } else {
         SparseBitSet b = (SparseBitSet)obj;
         if (this == b) {
            return true;
         } else {
            if (this.equalsStrategy == null) {
               this.equalsStrategy = new SparseBitSet.EqualsStrategy();
            }

            this.setScanner(0, Math.max(this.bitsLength, b.bitsLength), b, this.equalsStrategy);
            return this.equalsStrategy.result;
         }
      }
   }

   public void flip(int i) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         int w = i >> 6;
         int w1 = w >> 10;
         int w2 = w >> 5 & 31;
         if (i >= this.bitsLength) {
            this.resize(i);
         }

         long[][] a2;
         if ((a2 = this.bits[w1]) == null) {
            a2 = this.bits[w1] = new long[32][];
         }

         long[] a3;
         if ((a3 = a2[w2]) == null) {
            a3 = a2[w2] = new long[32];
         }

         a3[w & 31] = a3[w & 31] ^ 1L << i;
         this.cache.hash = 0;
      }
   }

   public void flip(int i, int j) throws IndexOutOfBoundsException {
      this.setScanner(i, j, null, flipStrategy);
   }

   public boolean get(int i) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         int w = i >> 6;
         long[][] a2;
         long[] a3;
         return i < this.bitsLength && (a2 = this.bits[w >> 10]) != null && (a3 = a2[w >> 5 & 31]) != null && (a3[w & 31] & 1L << i) != 0L;
      }
   }

   public SparseBitSet get(int i, int j) throws IndexOutOfBoundsException {
      SparseBitSet result = new SparseBitSet(j, this.compactionCount);
      result.setScanner(i, j, this, copyStrategy);
      return result;
   }

   @Override
   public int hashCode() {
      this.statisticsUpdate();
      return this.cache.hash;
   }

   public boolean intersects(int i, int j, SparseBitSet b) throws IndexOutOfBoundsException {
      this.setScanner(i, j, b, intersectsStrategy);
      return intersectsStrategy.result;
   }

   public boolean intersects(SparseBitSet b) {
      this.setScanner(0, Math.max(this.bitsLength, b.bitsLength), b, intersectsStrategy);
      return intersectsStrategy.result;
   }

   public boolean isEmpty() {
      this.statisticsUpdate();
      return this.cache.cardinality == 0;
   }

   public int length() {
      this.statisticsUpdate();
      return this.cache.length;
   }

   public int nextClearBit(int i) {
      if (i < 0) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         int w = i >> 6;
         int w3 = w & 31;
         int w2 = w >> 5 & 31;
         int w1 = w >> 10;
         long nword = -1L << i;
         int aLength = this.bits.length;
         long[][] a2;
         long[] a3;
         if (w1 < aLength && (a2 = this.bits[w1]) != null && (a3 = a2[w2]) != null && (nword = ~a3[w3] & -1L << i) == 0L) {
            w3 = ++w & 31;
            w2 = w >> 5 & 31;
            w1 = w >> 10;

            label49:
            for (nword = -1L; w1 != aLength && (a2 = this.bits[w1]) != null; w1++) {
               while (w2 != 32) {
                  if ((a3 = a2[w2]) == null) {
                     break label49;
                  }

                  while (w3 != 32) {
                     if ((nword = ~a3[w3]) != 0L) {
                        break label49;
                     }

                     w3++;
                  }

                  w3 = 0;
                  w2++;
               }

               w3 = 0;
               w2 = 0;
            }
         }

         int result = ((w1 << 10) + (w2 << 5) + w3 << 6) + Long.numberOfTrailingZeros(nword);
         return result == Integer.MAX_VALUE ? -1 : result;
      }
   }

   public int nextSetBit(int i) {
      if (i < 0) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         int w = i >> 6;
         int w3 = w & 31;
         int w2 = w >> 5 & 31;
         int w1 = w >> 10;
         long word = 0L;
         int aLength = this.bits.length;
         long[][] a2;
         long[] a3;
         if (w1 < aLength && ((a2 = this.bits[w1]) == null || (a3 = a2[w2]) == null || (word = a3[w3] & -1L << i) == 0L)) {
            w3 = ++w & 31;
            w2 = w >> 5 & 31;

            for (w1 = w >> 10; w1 != aLength; w1++) {
               if ((a2 = this.bits[w1]) != null) {
                  while (w2 != 32) {
                     if ((a3 = a2[w2]) != null) {
                        while (w3 != 32) {
                           if ((word = a3[w3]) != 0L) {
                              return w1 >= aLength ? -1 : ((w1 << 10) + (w2 << 5) + w3 << 6) + Long.numberOfTrailingZeros(word);
                           }

                           w3++;
                        }
                     }

                     w3 = 0;
                     w2++;
                  }
               }

               w3 = 0;
               w2 = 0;
            }
         }

         return w1 >= aLength ? -1 : ((w1 << 10) + (w2 << 5) + w3 << 6) + Long.numberOfTrailingZeros(word);
      }
   }

   public int previousClearBit(int i) {
      if (i < 0) {
         if (i == -1) {
            return -1;
         } else {
            throw new IndexOutOfBoundsException("i=" + i);
         }
      } else {
         long[][][] bits = this.bits;
         int aSize = bits.length - 1;
         int w = i >> 6;
         int w3 = w & 31;
         int w2 = w >> 5 & 31;
         int w1 = w >> 10;
         if (w1 > aSize) {
            return i;
         } else {
            for (int w4 = i % 64; w1 >= 0; w1--) {
               long[][] a2;
               if ((a2 = bits[w1]) == null) {
                  return ((w1 << 10) + (w2 << 5) + w3 << 6) + w4;
               }

               while (w2 >= 0) {
                  long[] a3;
                  if ((a3 = a2[w2]) == null) {
                     return ((w1 << 10) + (w2 << 5) + w3 << 6) + w4;
                  }

                  while (w3 >= 0) {
                     long word;
                     if ((word = a3[w3]) == 0L) {
                        return ((w1 << 10) + (w2 << 5) + w3 << 6) + w4;
                     }

                     for (int bitIdx = w4; bitIdx >= 0; bitIdx--) {
                        if ((word & 1L << bitIdx) == 0L) {
                           return ((w1 << 10) + (w2 << 5) + w3 << 6) + bitIdx;
                        }
                     }

                     w4 = 63;
                     w3--;
                  }

                  w3 = 31;
                  w2--;
               }

               w2 = 31;
            }

            return -1;
         }
      }
   }

   public int previousSetBit(int i) {
      if (i < 0) {
         if (i == -1) {
            return -1;
         } else {
            throw new IndexOutOfBoundsException("i=" + i);
         }
      } else {
         long[][][] bits = this.bits;
         int aSize = bits.length - 1;
         int w = i >> 6;
         int w1 = w >> 10;
         int w2;
         int w3;
         int w4;
         if (w1 > aSize) {
            w1 = aSize;
            w2 = 31;
            w3 = 31;
            w4 = 63;
         } else {
            w2 = w >> 5 & 31;
            w3 = w & 31;
            w4 = i % 64;
         }

         while (w1 >= 0) {
            long[][] a2;
            if ((a2 = bits[w1]) != null) {
               while (w2 >= 0) {
                  long[] a3;
                  if ((a3 = a2[w2]) != null) {
                     while (w3 >= 0) {
                        long word;
                        if ((word = a3[w3]) != 0L) {
                           for (int bitIdx = w4; bitIdx >= 0; bitIdx--) {
                              if ((word & 1L << bitIdx) != 0L) {
                                 return ((w1 << 10) + (w2 << 5) + w3 << 6) + bitIdx;
                              }
                           }
                        }

                        w4 = 63;
                        w3--;
                     }
                  }

                  w3 = 31;
                  w4 = 63;
                  w2--;
               }
            }

            w2 = 31;
            w3 = 31;
            w4 = 63;
            w1--;
         }

         return -1;
      }
   }

   public void or(int i, boolean value) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         if (value) {
            this.set(i);
         }
      }
   }

   public void or(int i, int j, SparseBitSet b) throws IndexOutOfBoundsException {
      this.setScanner(i, j, b, orStrategy);
   }

   public void or(SparseBitSet b) {
      this.setScanner(0, b.bitsLength, b, orStrategy);
   }

   public static SparseBitSet or(SparseBitSet a, SparseBitSet b) {
      SparseBitSet result = a.clone();
      result.or(b);
      return result;
   }

   public void set(int i) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         int w = i >> 6;
         int w1 = w >> 10;
         int w2 = w >> 5 & 31;
         if (i >= this.bitsLength) {
            this.resize(i);
         }

         long[][] a2;
         if ((a2 = this.bits[w1]) == null) {
            a2 = this.bits[w1] = new long[32][];
         }

         long[] a3;
         if ((a3 = a2[w2]) == null) {
            a3 = a2[w2] = new long[32];
         }

         a3[w & 31] = a3[w & 31] | 1L << i;
         this.cache.hash = 0;
      }
   }

   public void set(int i, boolean value) {
      if (value) {
         this.set(i);
      } else {
         this.clear(i);
      }
   }

   public void set(int i, int j) throws IndexOutOfBoundsException {
      this.setScanner(i, j, null, setStrategy);
   }

   public void set(int i, int j, boolean value) {
      if (value) {
         this.set(i, j);
      } else {
         this.clear(i, j);
      }
   }

   public int size() {
      this.statisticsUpdate();
      return this.cache.size;
   }

   public String statistics() {
      return this.statistics(null);
   }

   public String statistics(String[] values) {
      this.statisticsUpdate();
      String[] v = new String[SparseBitSet.Statistics.values().length];
      v[SparseBitSet.Statistics.Size.ordinal()] = Integer.toString(this.size());
      v[SparseBitSet.Statistics.Length.ordinal()] = Integer.toString(this.length());
      v[SparseBitSet.Statistics.Cardinality.ordinal()] = Integer.toString(this.cardinality());
      v[SparseBitSet.Statistics.Total_words.ordinal()] = Integer.toString(this.cache.count);
      v[SparseBitSet.Statistics.Set_array_length.ordinal()] = Integer.toString(this.bits.length);
      v[SparseBitSet.Statistics.Set_array_max_length.ordinal()] = Integer.toString(32768);
      v[SparseBitSet.Statistics.Level2_areas.ordinal()] = Integer.toString(this.cache.a2Count);
      v[SparseBitSet.Statistics.Level2_area_length.ordinal()] = Integer.toString(32);
      v[SparseBitSet.Statistics.Level3_blocks.ordinal()] = Integer.toString(this.cache.a3Count);
      v[SparseBitSet.Statistics.Level3_block_length.ordinal()] = Integer.toString(32);
      v[SparseBitSet.Statistics.Compaction_count_value.ordinal()] = Integer.toString(this.compactionCount);
      int longestLabel = 0;

      for (SparseBitSet.Statistics s : SparseBitSet.Statistics.values()) {
         longestLabel = Math.max(longestLabel, s.name().length());
      }

      StringBuilder result = new StringBuilder();

      for (SparseBitSet.Statistics s : SparseBitSet.Statistics.values()) {
         result.append(s.name());

         for (int i = 0; i != longestLabel - s.name().length(); i++) {
            result.append(' ');
         }

         result.append(" = ");
         result.append(v[s.ordinal()]);
         result.append('\n');
      }

      for (int i = 0; i != result.length(); i++) {
         if (result.charAt(i) == '_') {
            result.setCharAt(i, ' ');
         }
      }

      if (values != null) {
         int len = Math.min(values.length, v.length);
         System.arraycopy(v, 0, values, 0, len);
      }

      return result.toString();
   }

   @Override
   public String toString() {
      StringBuilder p = new StringBuilder(200);
      p.append('{');
      int i = this.nextSetBit(0);

      while (i >= 0) {
         p.append(i);
         int j = this.nextSetBit(i + 1);
         if (this.compactionCount > 0) {
            if (j < 0) {
               break;
            }

            int last = this.nextClearBit(i);
            last = last < 0 ? Integer.MAX_VALUE : last;
            if (i + this.compactionCount < last) {
               p.append("..").append(last - 1);
               j = this.nextSetBit(last);
            }
         }

         if (j >= 0) {
            p.append(", ");
         }

         i = j;
      }

      p.append('}');
      return p.toString();
   }

   public void toStringCompaction(int count) {
      this.compactionCount = count;
   }

   public void toStringCompaction(boolean change) {
      if (change) {
         compactionCountDefault = this.compactionCount;
      }
   }

   public void xor(int i, boolean value) {
      if (i + 1 < 1) {
         throw new IndexOutOfBoundsException("i=" + i);
      } else {
         if (value) {
            this.flip(i);
         }
      }
   }

   public void xor(int i, int j, SparseBitSet b) throws IndexOutOfBoundsException {
      this.setScanner(i, j, b, xorStrategy);
   }

   public void xor(SparseBitSet b) {
      this.setScanner(0, b.bitsLength, b, xorStrategy);
   }

   public static SparseBitSet xor(SparseBitSet a, SparseBitSet b) {
      SparseBitSet result = a.clone();
      result.xor(b);
      return result;
   }

   protected static void throwIndexOutOfBoundsException(int i, int j) throws IndexOutOfBoundsException {
      String s = "";
      if (i < 0) {
         s = s + "(i=" + i + ") < 0";
      }

      if (i == Integer.MAX_VALUE) {
         s = s + "(i=" + i + ")";
      }

      if (j < 0) {
         s = s + (s.isEmpty() ? "" : ", ") + "(j=" + j + ") < 0";
      }

      if (i > j) {
         s = s + (s.isEmpty() ? "" : ", ") + "(i=" + i + ") > (j=" + j + ")";
      }

      throw new IndexOutOfBoundsException(s);
   }

   protected final void constructorHelper() {
      this.spare = new long[32];
      this.cache = new SparseBitSet.Cache();
      this.updateStrategy = new SparseBitSet.UpdateStrategy();
   }

   protected final void nullify(int start) {
      int aLength = this.bits.length;
      if (start < aLength) {
         for (int w = start; w != aLength; w++) {
            this.bits[w] = null;
         }

         this.cache.hash = 0;
      }
   }

   protected final void resize(int index) {
      int w1 = index >> 6 >> 10;
      int newSize = Integer.highestOneBit(w1);
      if (newSize == 0) {
         newSize = 1;
      }

      if (w1 >= newSize) {
         newSize <<= 1;
      }

      if (newSize > 32768) {
         newSize = 32768;
      }

      int aLength1 = this.bits != null ? this.bits.length : 0;
      if (newSize != aLength1 || this.bits == null) {
         long[][][] temp = new long[newSize][][];
         if (aLength1 != 0) {
            System.arraycopy(this.bits, 0, temp, 0, Math.min(aLength1, newSize));
            this.nullify(0);
         }

         this.bits = temp;
         this.bitsLength = newSize == 32768 ? Integer.MAX_VALUE : newSize * 65536;
      }
   }

   protected final void setScanner(int i, int j, SparseBitSet b, SparseBitSet.AbstractStrategy op) throws IndexOutOfBoundsException {
      if (op.start(b)) {
         this.cache.hash = 0;
      }

      if (j < i || i + 1 < 1) {
         throwIndexOutOfBoundsException(i, j);
      }

      if (i != j) {
         int properties = op.properties();
         boolean f_op_f_eq_f = (properties & 1) != 0;
         boolean f_op_x_eq_f = (properties & 2) != 0;
         boolean x_op_f_eq_f = (properties & 4) != 0;
         boolean x_op_f_eq_x = (properties & 8) != 0;
         int u = i >> 6;
         long um = -1L << i;
         int v = j - 1 >> 6;
         long vm = -1L >>> -j;
         long[][][] a1 = this.bits;
         int aLength1 = this.bits.length;
         long[][][] b1 = b != null ? b.bits : null;
         int bLength1 = b1 != null ? b.bits.length : 0;
         int u1 = u >> 10;
         int u2 = u >> 5 & 31;
         int u3 = u & 31;
         int v1 = v >> 10;
         int v2 = v >> 5 & 31;
         int v3 = v & 31;
         int lastA3Block = (v1 << 5) + v2;
         int a2CountLocal = 0;
         int a3CountLocal = 0;
         boolean notFirstBlock = u == 0 && um == -1L;
         boolean a2IsEmpty = u2 == 0;

         while (i < j) {
            long[][] a2 = null;
            boolean haveA2 = u1 < aLength1 && (a2 = a1[u1]) != null;
            long[][] b2 = null;
            boolean haveB2 = u1 < bLength1 && b1 != null && (b2 = b1[u1]) != null;
            if ((!haveA2 && !haveB2 && f_op_f_eq_f || !haveA2 && f_op_x_eq_f || !haveB2 && x_op_f_eq_f) && notFirstBlock && u1 != v1) {
               if (u1 < aLength1) {
                  a1[u1] = null;
               }
            } else {
               for (int limit2 = u1 == v1 ? v2 + 1 : 32; u2 != limit2; u3 = 0) {
                  long[] a3 = null;
                  boolean haveA3 = haveA2 && (a3 = a2[u2]) != null;
                  long[] b3 = null;
                  boolean haveB3 = haveB2 && (b3 = b2[u2]) != null;
                  int a3Block = (u1 << 5) + u2;
                  boolean notLastBlock = lastA3Block != a3Block;
                  if ((!haveA3 && !haveB3 && f_op_f_eq_f || !haveA3 && f_op_x_eq_f || !haveB3 && x_op_f_eq_f) && notFirstBlock && notLastBlock) {
                     if (haveA2) {
                        a2[u2] = null;
                     }
                  } else {
                     int base3 = a3Block << 5;
                     int limit3 = notLastBlock ? 32 : v3;
                     if (!haveA3) {
                        a3 = this.spare;
                     }

                     if (!haveB3) {
                        b3 = ZERO_BLOCK;
                     }

                     boolean isZero;
                     if (notFirstBlock && notLastBlock) {
                        if (x_op_f_eq_x && !haveB3) {
                           isZero = op.isZeroBlock(a3);
                        } else {
                           isZero = op.block(base3, 0, 32, a3, b3);
                        }
                     } else {
                        if (notFirstBlock) {
                           isZero = op.block(base3, 0, limit3, a3, b3);
                           isZero &= op.word(base3, limit3, a3, b3, vm);
                        } else {
                           if (u == v) {
                              isZero = op.word(base3, u3, a3, b3, um & vm);
                           } else {
                              isZero = op.word(base3, u3, a3, b3, um);
                              isZero &= op.block(base3, u3 + 1, limit3, a3, b3);
                              if (limit3 != 32) {
                                 isZero &= op.word(base3, limit3, a3, b3, vm);
                              }
                           }

                           notFirstBlock = true;
                        }

                        if (isZero) {
                           isZero = op.isZeroBlock(a3);
                        }
                     }

                     if (isZero) {
                        if (haveA2) {
                           a2[u2] = null;
                        }
                     } else {
                        if (a3 == this.spare) {
                           if (i >= this.bitsLength) {
                              this.resize(i);
                              a1 = this.bits;
                              aLength1 = a1.length;
                           }

                           if (a2 == null) {
                              a1[u1] = a2 = new long[32][];
                              haveA2 = true;
                           }

                           a2[u2] = a3;
                           this.spare = new long[32];
                        }

                        a3CountLocal++;
                     }

                     a2IsEmpty &= !haveA2 || a2[u2] == null;
                  }

                  u2++;
               }

               if (u2 == 32 && a2IsEmpty && u1 < aLength1) {
                  a1[u1] = null;
               } else {
                  a2CountLocal++;
               }
            }

            i = (u = ++u1 << 10) << 6;
            u2 = 0;
            if (i < 0) {
               i = Integer.MAX_VALUE;
            }
         }

         op.finish(a2CountLocal, a3CountLocal);
      }
   }

   protected final void statisticsUpdate() {
      if (this.cache.hash == 0) {
         this.setScanner(0, this.bitsLength, null, this.updateStrategy);
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException, InternalError {
      this.statisticsUpdate();
      s.defaultWriteObject();
      s.writeInt(this.compactionCount);
      s.writeInt(this.cache.length);
      int count = this.cache.count;
      s.writeInt(count);
      long[][][] a1 = this.bits;
      int aLength1 = a1.length;

      for (int w1 = 0; w1 != aLength1; w1++) {
         long[][] a2;
         if ((a2 = a1[w1]) != null) {
            for (int w2 = 0; w2 != 32; w2++) {
               long[] a3;
               if ((a3 = a2[w2]) != null) {
                  int base = (w1 << 10) + (w2 << 5);

                  for (int w3 = 0; w3 != 32; w3++) {
                     long word;
                     if ((word = a3[w3]) != 0L) {
                        s.writeInt(base + w3);
                        s.writeLong(word);
                        count--;
                     }
                  }
               }
            }
         }
      }

      if (count != 0) {
         throw new InternalError("count of entries not consistent");
      } else {
         s.writeInt(this.cache.hash);
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.compactionCount = s.readInt();
      int aLength = s.readInt();
      this.resize(aLength);
      int count = s.readInt();

      for (int n = 0; n != count; n++) {
         int w = s.readInt();
         int w3 = w & 31;
         int w2 = w >> 5 & 31;
         int w1 = w >> 10;
         long word = s.readLong();
         long[][] a2;
         if ((a2 = this.bits[w1]) == null) {
            a2 = this.bits[w1] = new long[32][];
         }

         long[] a3;
         if ((a3 = a2[w2]) == null) {
            a3 = a2[w2] = new long[32];
         }

         a3[w3] = word;
      }

      this.constructorHelper();
      this.statisticsUpdate();
      if (count != this.cache.count) {
         throw new InternalError("count of entries not consistent");
      } else {
         int hash = s.readInt();
         if (hash != this.cache.hash) {
            throw new IOException("deserialized hashCode mis-match");
         }
      }
   }

   protected abstract static class AbstractStrategy {
      static final int F_OP_F_EQ_F = 1;
      static final int F_OP_X_EQ_F = 2;
      static final int X_OP_F_EQ_F = 4;
      static final int X_OP_F_EQ_X = 8;

      protected abstract int properties();

      protected abstract boolean start(SparseBitSet var1);

      protected abstract boolean word(int var1, int var2, long[] var3, long[] var4, long var5);

      protected abstract boolean block(int var1, int var2, int var3, long[] var4, long[] var5);

      protected void finish(int a2Count, int a3Count) {
      }

      protected final boolean isZeroBlock(long[] a3) {
         for (long word : a3) {
            if (word != 0L) {
               return false;
            }
         }

         return true;
      }
   }

   protected static class AndNotStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 11;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            return true;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] &= ~(b3[u3] & mask)) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] &= ~b3[w3]) == 0L;
         }

         return isZero;
      }
   }

   protected static class AndStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 7;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            return true;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] &= b3[u3] | ~mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] &= b3[w3]) == 0L;
         }

         return isZero;
      }
   }

   protected class Cache {
      protected transient int hash;
      protected transient int size;
      protected transient int cardinality;
      protected transient int length;
      protected transient int count;
      protected transient int a2Count;
      protected transient int a3Count;
   }

   protected static class ClearStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 3;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         return true;
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] &= ~mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         if (u3 != 0 || v3 != 32) {
            for (int w3 = u3; w3 != v3; w3++) {
               a3[w3] = 0L;
            }
         }

         return true;
      }
   }

   protected static class CopyStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 5;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         return true;
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] = b3[u3] & mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] = b3[w3]) == 0L;
         }

         return isZero;
      }
   }

   protected static class EqualsStrategy extends SparseBitSet.AbstractStrategy {
      boolean result;

      @Override
      protected int properties() {
         return 1;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            this.result = true;
            return false;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         long word = a3[u3];
         this.result = this.result & (word & mask) == (b3[u3] & mask);
         return word == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            long word = a3[w3];
            this.result = this.result & word == b3[w3];
            isZero &= word == 0L;
         }

         return isZero;
      }
   }

   protected static class FlipStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 0;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         return true;
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] ^= mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] = ~a3[w3]) == 0L;
         }

         return isZero;
      }
   }

   protected static class IntersectsStrategy extends SparseBitSet.AbstractStrategy {
      protected boolean result;

      @Override
      protected int properties() {
         return 3;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            this.result = false;
            return false;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         long word = a3[u3];
         this.result = this.result | (word & b3[u3] & mask) != 0L;
         return word == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            long word = a3[w3];
            this.result = this.result | (word & b3[w3]) != 0L;
            isZero &= word == 0L;
         }

         return isZero;
      }
   }

   protected static class OrStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 9;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            return true;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] |= b3[u3] & mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] |= b3[w3]) == 0L;
         }

         return isZero;
      }
   }

   protected static class SetStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 0;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         return true;
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         a3[u3] |= mask;
         return false;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         for (int w3 = u3; w3 != v3; w3++) {
            a3[w3] = -1L;
         }

         return false;
      }
   }

   public static enum Statistics {
      Size,
      Length,
      Cardinality,
      Total_words,
      Set_array_length,
      Set_array_max_length,
      Level2_areas,
      Level2_area_length,
      Level3_blocks,
      Level3_block_length,
      Compaction_count_value;
   }

   protected class UpdateStrategy extends SparseBitSet.AbstractStrategy {
      protected transient int wMin;
      protected transient long wordMin;
      protected transient int wMax;
      protected transient long wordMax;
      protected transient long hash;
      protected transient int count;
      protected transient int cardinality;

      @Override
      protected int properties() {
         return 3;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         this.hash = 1234L;
         this.wMin = -1;
         this.wordMin = 0L;
         this.wMax = 0;
         this.wordMax = 0L;
         this.count = 0;
         this.cardinality = 0;
         return false;
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         long word = a3[u3];
         long word1 = word & mask;
         if (word1 != 0L) {
            this.compute(base + u3, word1);
         }

         return word == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = 0; w3 != v3; w3++) {
            long word = a3[w3];
            if (word != 0L) {
               isZero = false;
               this.compute(base + w3, word);
            }
         }

         return isZero;
      }

      @Override
      protected void finish(int a2Count, int a3Count) {
         SparseBitSet.this.cache.a2Count = a2Count;
         SparseBitSet.this.cache.a3Count = a3Count;
         SparseBitSet.this.cache.count = this.count;
         SparseBitSet.this.cache.cardinality = this.cardinality;
         SparseBitSet.this.cache.length = (this.wMax + 1) * 64 - Long.numberOfLeadingZeros(this.wordMax);
         SparseBitSet.this.cache.size = SparseBitSet.this.cache.length - this.wMin * 64 - Long.numberOfTrailingZeros(this.wordMin);
         SparseBitSet.this.cache.hash = (int)(this.hash >> 32 ^ this.hash);
      }

      private void compute(int index, long word) {
         this.count++;
         this.hash ^= word * (index + 1);
         if (this.wMin < 0) {
            this.wMin = index;
            this.wordMin = word;
         }

         this.wMax = index;
         this.wordMax = word;
         this.cardinality = this.cardinality + Long.bitCount(word);
      }
   }

   protected static class XorStrategy extends SparseBitSet.AbstractStrategy {
      @Override
      protected int properties() {
         return 9;
      }

      @Override
      protected boolean start(SparseBitSet b) {
         if (b == null) {
            throw new NullPointerException();
         } else {
            return true;
         }
      }

      @Override
      protected boolean word(int base, int u3, long[] a3, long[] b3, long mask) {
         return (a3[u3] ^= b3[u3] & mask) == 0L;
      }

      @Override
      protected boolean block(int base, int u3, int v3, long[] a3, long[] b3) {
         boolean isZero = true;

         for (int w3 = u3; w3 != v3; w3++) {
            isZero &= (a3[w3] ^= b3[w3]) == 0L;
         }

         return isZero;
      }
   }
}
