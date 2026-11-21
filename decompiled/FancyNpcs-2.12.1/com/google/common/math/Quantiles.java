package com.google.common.math;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;

@ElementTypesAreNonnullByDefault
@J2ktIncompatible
@GwtIncompatible
public final class Quantiles {
   public static Quantiles.ScaleAndIndex median() {
      return scale(2).index(1);
   }

   public static Quantiles.Scale quartiles() {
      return scale(4);
   }

   public static Quantiles.Scale percentiles() {
      return scale(100);
   }

   public static Quantiles.Scale scale(int scale) {
      return new Quantiles.Scale(scale);
   }

   private static boolean containsNaN(double... dataset) {
      for (double value : dataset) {
         if (Double.isNaN(value)) {
            return true;
         }
      }

      return false;
   }

   private static double interpolate(double lower, double upper, double remainder, double scale) {
      if (lower == Double.NEGATIVE_INFINITY) {
         return upper == Double.POSITIVE_INFINITY ? Double.NaN : Double.NEGATIVE_INFINITY;
      } else {
         return upper == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : lower + (upper - lower) * remainder / scale;
      }
   }

   private static void checkIndex(int index, int scale) {
      if (index < 0 || index > scale) {
         throw new IllegalArgumentException("Quantile indexes must be between 0 and the scale, which is " + scale);
      }
   }

   private static double[] longsToDoubles(long[] longs) {
      int len = longs.length;
      double[] doubles = new double[len];

      for (int i = 0; i < len; i++) {
         doubles[i] = longs[i];
      }

      return doubles;
   }

   private static double[] intsToDoubles(int[] ints) {
      int len = ints.length;
      double[] doubles = new double[len];

      for (int i = 0; i < len; i++) {
         doubles[i] = ints[i];
      }

      return doubles;
   }

   private static void selectInPlace(int required, double[] array, int from, int to) {
      if (required == from) {
         int min = from;

         for (int index = from + 1; index <= to; index++) {
            if (array[min] > array[index]) {
               min = index;
            }
         }

         if (min != from) {
            swap(array, min, from);
         }
      } else {
         while (to > from) {
            int partitionPoint = partition(array, from, to);
            if (partitionPoint >= required) {
               to = partitionPoint - 1;
            }

            if (partitionPoint <= required) {
               from = partitionPoint + 1;
            }
         }
      }
   }

   private static int partition(double[] array, int from, int to) {
      movePivotToStartOfSlice(array, from, to);
      double pivot = array[from];
      int partitionPoint = to;

      for (int i = to; i > from; i--) {
         if (array[i] > pivot) {
            swap(array, partitionPoint, i);
            partitionPoint--;
         }
      }

      swap(array, from, partitionPoint);
      return partitionPoint;
   }

   private static void movePivotToStartOfSlice(double[] array, int from, int to) {
      int mid = from + to >>> 1;
      boolean toLessThanMid = array[to] < array[mid];
      boolean midLessThanFrom = array[mid] < array[from];
      boolean toLessThanFrom = array[to] < array[from];
      if (toLessThanMid == midLessThanFrom) {
         swap(array, mid, from);
      } else if (toLessThanMid != toLessThanFrom) {
         swap(array, from, to);
      }
   }

   private static void selectAllInPlace(int[] allRequired, int requiredFrom, int requiredTo, double[] array, int from, int to) {
      int requiredChosen = chooseNextSelection(allRequired, requiredFrom, requiredTo, from, to);
      int required = allRequired[requiredChosen];
      selectInPlace(required, array, from, to);
      int requiredBelow = requiredChosen - 1;

      while (requiredBelow >= requiredFrom && allRequired[requiredBelow] == required) {
         requiredBelow--;
      }

      if (requiredBelow >= requiredFrom) {
         selectAllInPlace(allRequired, requiredFrom, requiredBelow, array, from, required - 1);
      }

      int requiredAbove = requiredChosen + 1;

      while (requiredAbove <= requiredTo && allRequired[requiredAbove] == required) {
         requiredAbove++;
      }

      if (requiredAbove <= requiredTo) {
         selectAllInPlace(allRequired, requiredAbove, requiredTo, array, required + 1, to);
      }
   }

   private static int chooseNextSelection(int[] allRequired, int requiredFrom, int requiredTo, int from, int to) {
      if (requiredFrom == requiredTo) {
         return requiredFrom;
      } else {
         int centerFloor = from + to >>> 1;
         int low = requiredFrom;
         int high = requiredTo;

         while (high > low + 1) {
            int mid = low + high >>> 1;
            if (allRequired[mid] > centerFloor) {
               high = mid;
            } else {
               if (allRequired[mid] >= centerFloor) {
                  return mid;
               }

               low = mid;
            }
         }

         return from + to - allRequired[low] - allRequired[high] > 0 ? high : low;
      }
   }

   private static void swap(double[] array, int i, int j) {
      double temp = array[i];
      array[i] = array[j];
      array[j] = temp;
   }

   public static final class Scale {
      private final int scale;

      private Scale(int scale) {
         Preconditions.checkArgument(scale > 0, "Quantile scale must be positive");
         this.scale = scale;
      }

      public Quantiles.ScaleAndIndex index(int index) {
         return new Quantiles.ScaleAndIndex(this.scale, index);
      }

      public Quantiles.ScaleAndIndexes indexes(int... indexes) {
         return new Quantiles.ScaleAndIndexes(this.scale, (int[])indexes.clone());
      }

      public Quantiles.ScaleAndIndexes indexes(Collection<Integer> indexes) {
         return new Quantiles.ScaleAndIndexes(this.scale, Ints.toArray(indexes));
      }
   }

   public static final class ScaleAndIndex {
      private final int scale;
      private final int index;

      private ScaleAndIndex(int scale, int index) {
         Quantiles.checkIndex(index, scale);
         this.scale = scale;
         this.index = index;
      }

      public double compute(Collection<? extends Number> dataset) {
         return this.computeInPlace(Doubles.toArray(dataset));
      }

      public double compute(double... dataset) {
         return this.computeInPlace((double[])dataset.clone());
      }

      public double compute(long... dataset) {
         return this.computeInPlace(Quantiles.longsToDoubles(dataset));
      }

      public double compute(int... dataset) {
         return this.computeInPlace(Quantiles.intsToDoubles(dataset));
      }

      public double computeInPlace(double... dataset) {
         Preconditions.checkArgument(dataset.length > 0, "Cannot calculate quantiles of an empty dataset");
         if (Quantiles.containsNaN(dataset)) {
            return Double.NaN;
         } else {
            long numerator = (long)this.index * (dataset.length - 1);
            int quotient = (int)LongMath.divide(numerator, this.scale, RoundingMode.DOWN);
            int remainder = (int)(numerator - (long)quotient * this.scale);
            Quantiles.selectInPlace(quotient, dataset, 0, dataset.length - 1);
            if (remainder == 0) {
               return dataset[quotient];
            } else {
               Quantiles.selectInPlace(quotient + 1, dataset, quotient + 1, dataset.length - 1);
               return Quantiles.interpolate(dataset[quotient], dataset[quotient + 1], (double)remainder, (double)this.scale);
            }
         }
      }
   }

   public static final class ScaleAndIndexes {
      private final int scale;
      private final int[] indexes;

      private ScaleAndIndexes(int scale, int[] indexes) {
         for (int index : indexes) {
            Quantiles.checkIndex(index, scale);
         }

         Preconditions.checkArgument(indexes.length > 0, "Indexes must be a non empty array");
         this.scale = scale;
         this.indexes = indexes;
      }

      public Map<Integer, Double> compute(Collection<? extends Number> dataset) {
         return this.computeInPlace(Doubles.toArray(dataset));
      }

      public Map<Integer, Double> compute(double... dataset) {
         return this.computeInPlace((double[])dataset.clone());
      }

      public Map<Integer, Double> compute(long... dataset) {
         return this.computeInPlace(Quantiles.longsToDoubles(dataset));
      }

      public Map<Integer, Double> compute(int... dataset) {
         return this.computeInPlace(Quantiles.intsToDoubles(dataset));
      }

      public Map<Integer, Double> computeInPlace(double... dataset) {
         // $VF: Couldn't be decompiled
         // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
         // java.lang.OutOfMemoryError: Java heap space
         //   at java.base/java.util.ArrayList.iterator(ArrayList.java:1029)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:256)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:257)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:257)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:257)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:257)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:257)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:147)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:141)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:141)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:141)
         //   at org.jetbrains.java.decompiler.modules.decompiler.SecondaryFunctionsHelper.identifySecondaryFunctions(SecondaryFunctionsHelper.java:48)
         //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:309)
         //
         // Bytecode:
         // 000: aload 1
         // 001: arraylength
         // 002: ifle 009
         // 005: bipush 1
         // 006: goto 00a
         // 009: bipush 0
         // 00a: ldc "Cannot calculate quantiles of an empty dataset"
         // 00c: invokestatic com/google/common/base/Preconditions.checkArgument (ZLjava/lang/Object;)V
         // 00f: aload 1
         // 010: invokestatic com/google/common/math/Quantiles.access$600 ([D)Z
         // 013: ifeq 054
         // 016: new java/util/LinkedHashMap
         // 019: dup
         // 01a: invokespecial java/util/LinkedHashMap.<init> ()V
         // 01d: astore 2
         // 01e: aload 0
         // 01f: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 022: astore 3
         // 023: aload 3
         // 024: arraylength
         // 025: istore 4
         // 027: bipush 0
         // 028: istore 5
         // 02a: iload 5
         // 02c: iload 4
         // 02e: if_icmpge 04f
         // 031: aload 3
         // 032: iload 5
         // 034: iaload
         // 035: istore 6
         // 037: aload 2
         // 038: iload 6
         // 03a: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
         // 03d: ldc2_w NaN
         // 040: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
         // 043: invokeinterface java/util/Map.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; 3
         // 048: pop
         // 049: iinc 5 1
         // 04c: goto 02a
         // 04f: aload 2
         // 050: invokestatic java/util/Collections.unmodifiableMap (Ljava/util/Map;)Ljava/util/Map;
         // 053: areturn
         // 054: aload 0
         // 055: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 058: arraylength
         // 059: newarray 10
         // 05b: astore 2
         // 05c: aload 0
         // 05d: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 060: arraylength
         // 061: newarray 10
         // 063: astore 3
         // 064: aload 0
         // 065: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 068: arraylength
         // 069: bipush 2
         // 06a: imul
         // 06b: newarray 10
         // 06d: astore 4
         // 06f: bipush 0
         // 070: istore 5
         // 072: bipush 0
         // 073: istore 6
         // 075: iload 6
         // 077: aload 0
         // 078: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 07b: arraylength
         // 07c: if_icmpge 0db
         // 07f: aload 0
         // 080: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 083: iload 6
         // 085: iaload
         // 086: i2l
         // 087: aload 1
         // 088: arraylength
         // 089: bipush 1
         // 08a: isub
         // 08b: i2l
         // 08c: lmul
         // 08d: lstore 7
         // 08f: lload 7
         // 091: aload 0
         // 092: getfield com/google/common/math/Quantiles$ScaleAndIndexes.scale I
         // 095: i2l
         // 096: getstatic java/math/RoundingMode.DOWN Ljava/math/RoundingMode;
         // 099: invokestatic com/google/common/math/LongMath.divide (JJLjava/math/RoundingMode;)J
         // 09c: l2i
         // 09d: istore 9
         // 09f: lload 7
         // 0a1: iload 9
         // 0a3: i2l
         // 0a4: aload 0
         // 0a5: getfield com/google/common/math/Quantiles$ScaleAndIndexes.scale I
         // 0a8: i2l
         // 0a9: lmul
         // 0aa: lsub
         // 0ab: l2i
         // 0ac: istore 10
         // 0ae: aload 2
         // 0af: iload 6
         // 0b1: iload 9
         // 0b3: iastore
         // 0b4: aload 3
         // 0b5: iload 6
         // 0b7: iload 10
         // 0b9: iastore
         // 0ba: aload 4
         // 0bc: iload 5
         // 0be: iload 9
         // 0c0: iastore
         // 0c1: iinc 5 1
         // 0c4: iload 10
         // 0c6: ifeq 0d5
         // 0c9: aload 4
         // 0cb: iload 5
         // 0cd: iload 9
         // 0cf: bipush 1
         // 0d0: iadd
         // 0d1: iastore
         // 0d2: iinc 5 1
         // 0d5: iinc 6 1
         // 0d8: goto 075
         // 0db: aload 4
         // 0dd: bipush 0
         // 0de: iload 5
         // 0e0: invokestatic java/util/Arrays.sort ([III)V
         // 0e3: aload 4
         // 0e5: bipush 0
         // 0e6: iload 5
         // 0e8: bipush 1
         // 0e9: isub
         // 0ea: aload 1
         // 0eb: bipush 0
         // 0ec: aload 1
         // 0ed: arraylength
         // 0ee: bipush 1
         // 0ef: isub
         // 0f0: invokestatic com/google/common/math/Quantiles.access$900 ([III[DII)V
         // 0f3: new java/util/LinkedHashMap
         // 0f6: dup
         // 0f7: invokespecial java/util/LinkedHashMap.<init> ()V
         // 0fa: astore 6
         // 0fc: bipush 0
         // 0fd: istore 7
         // 0ff: iload 7
         // 101: aload 0
         // 102: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 105: arraylength
         // 106: if_icmpge 166
         // 109: aload 2
         // 10a: iload 7
         // 10c: iaload
         // 10d: istore 8
         // 10f: aload 3
         // 110: iload 7
         // 112: iaload
         // 113: istore 9
         // 115: iload 9
         // 117: ifne 136
         // 11a: aload 6
         // 11c: aload 0
         // 11d: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 120: iload 7
         // 122: iaload
         // 123: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
         // 126: aload 1
         // 127: iload 8
         // 129: daload
         // 12a: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
         // 12d: invokeinterface java/util/Map.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; 3
         // 132: pop
         // 133: goto 160
         // 136: aload 6
         // 138: aload 0
         // 139: getfield com/google/common/math/Quantiles$ScaleAndIndexes.indexes [I
         // 13c: iload 7
         // 13e: iaload
         // 13f: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
         // 142: aload 1
         // 143: iload 8
         // 145: daload
         // 146: aload 1
         // 147: iload 8
         // 149: bipush 1
         // 14a: iadd
         // 14b: daload
         // 14c: iload 9
         // 14e: i2d
         // 14f: aload 0
         // 150: getfield com/google/common/math/Quantiles$ScaleAndIndexes.scale I
         // 153: i2d
         // 154: invokestatic com/google/common/math/Quantiles.access$800 (DDDD)D
         // 157: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
         // 15a: invokeinterface java/util/Map.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; 3
         // 15f: pop
         // 160: iinc 7 1
         // 163: goto 0ff
         // 166: aload 6
         // 168: invokestatic java/util/Collections.unmodifiableMap (Ljava/util/Map;)Ljava/util/Map;
         // 16b: areturn
      }
   }
}
