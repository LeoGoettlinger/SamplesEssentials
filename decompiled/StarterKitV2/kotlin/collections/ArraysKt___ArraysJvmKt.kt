package kotlin.collections

import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.RandomAccess
import java.util.SortedSet
import java.util.TreeSet
import kotlin.internal.InlineOnly
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\n_ArraysJvm.kt\nKotlin\n*S Kotlin\n*F\n+ 1 _ArraysJvm.kt\nkotlin/collections/ArraysKt___ArraysJvmKt\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,3069:1\n14727#2,14:3070\n14757#2,14:3084\n14787#2,14:3098\n14817#2,14:3112\n14847#2,14:3126\n14877#2,14:3140\n14907#2,14:3154\n14937#2,14:3168\n14967#2,14:3182\n17699#2,14:3196\n17729#2,14:3210\n17759#2,14:3224\n17789#2,14:3238\n17819#2,14:3252\n17849#2,14:3266\n17879#2,14:3280\n17909#2,14:3294\n17939#2,14:3308\n*S KotlinDebug\n*F\n+ 1 _ArraysJvm.kt\nkotlin/collections/ArraysKt___ArraysJvmKt\n*L\n2461#1:3070,14\n2468#1:3084,14\n2475#1:3098,14\n2482#1:3112,14\n2489#1:3126,14\n2496#1:3140,14\n2503#1:3154,14\n2510#1:3168,14\n2517#1:3182,14\n2659#1:3196,14\n2666#1:3210,14\n2673#1:3224,14\n2680#1:3238,14\n2687#1:3252,14\n2694#1:3266,14\n2701#1:3280,14\n2708#1:3294,14\n2715#1:3308,14\n*E\n"])
internal class ArraysKt___ArraysJvmKt : ArraysKt__ArraysKt {
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>.elementAt(index: Int): T {
      return (T)`$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.elementAt(index: Int): Byte {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.elementAt(index: Int): Short {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun IntArray.elementAt(index: Int): Int {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun LongArray.elementAt(index: Int): Long {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.elementAt(index: Int): Float {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.elementAt(index: Int): Double {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.elementAt(index: Int): Boolean {
      return `$this$elementAt`[index];
   }

   @InlineOnly
   @JvmStatic
   public inline fun CharArray.elementAt(index: Int): Char {
      return `$this$elementAt`[index];
   }

   @JvmStatic
   public fun <R> Array<*>.filterIsInstance(klass: Class<R>): List<R> {
      return ArraysKt.filterIsInstanceTo(`$this$filterIsInstance`, new ArrayList(), klass) as MutableList<R>;
   }

   @IgnorableReturnValue
   @JvmStatic
   public fun <C : MutableCollection<in R>, R> Array<*>.filterIsInstanceTo(destination: C, klass: Class<R>): C {
      for (Object element : $this$filterIsInstanceTo) {
         if (klass.isInstance(element)) {
            destination.add(element);
         }
      }

      return (C)destination;
   }

   @JvmStatic
   public fun <T> Array<out T>.asList(): List<T> {
      val var10000: java.util.List = ArraysUtilJVM.asList(`$this$asList`);
      return var10000;
   }

   @JvmStatic
   public fun ByteArray.asList(): List<Byte> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(byte element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public java.lang.Byte get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(byte element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(byte element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun ShortArray.asList(): List<Short> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(short element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public java.lang.Short get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(short element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(short element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun IntArray.asList(): List<Int> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(int element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public Integer get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(int element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(int element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun LongArray.asList(): List<Long> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(long element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public java.lang.Long get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(long element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(long element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun FloatArray.asList(): List<Float> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(float element) {
            val `$this$any$iv`: FloatArray = this.$this_asList;
            var var4: Int = 0;
            val var5: Int = this.$this_asList.length;

            var var10000: Boolean;
            while (true) {
               if (var4 >= var5) {
                  var10000 = false;
                  break;
               }

               if (java.lang.Float.floatToIntBits(`$this$any$iv`[var4]) == java.lang.Float.floatToIntBits(element)) {
                  var10000 = true;
                  break;
               }

               var4++;
            }

            return var10000;
         }

         public java.lang.Float get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(float element) {
            val `$this$indexOfFirst$iv`: FloatArray = this.$this_asList;
            var `index$iv`: Int = 0;
            val var5: Int = this.$this_asList.length;

            var var10000: Int;
            while (true) {
               if (`index$iv` >= var5) {
                  var10000 = -1;
                  break;
               }

               if (java.lang.Float.floatToIntBits(`$this$indexOfFirst$iv`[`index$iv`]) == java.lang.Float.floatToIntBits(element)) {
                  var10000 = `index$iv`;
                  break;
               }

               `index$iv`++;
            }

            return var10000;
         }

         public int lastIndexOf(float element) {
            val `$this$indexOfLast$iv`: FloatArray = this.$this_asList;
            var var4: Int = this.$this_asList.length + -1;
            if (0 <= this.$this_asList.length + -1) {
               do {
                  val `index$iv`: Int = var4--;
                  if (java.lang.Float.floatToIntBits(`$this$indexOfLast$iv`[`index$iv`]) == java.lang.Float.floatToIntBits(element)) {
                     return `index$iv`;
                  }
               } while (0 <= var4);
            }

            return -1;
         }
      };
   }

   @JvmStatic
   public fun DoubleArray.asList(): List<Double> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(double element) {
            val `$this$any$iv`: DoubleArray = this.$this_asList;
            var var5: Int = 0;
            val var6: Int = this.$this_asList.length;

            var var10000: Boolean;
            while (true) {
               if (var5 >= var6) {
                  var10000 = false;
                  break;
               }

               if (java.lang.Double.doubleToLongBits(`$this$any$iv`[var5]) == java.lang.Double.doubleToLongBits(element)) {
                  var10000 = true;
                  break;
               }

               var5++;
            }

            return var10000;
         }

         public java.lang.Double get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(double element) {
            val `$this$indexOfFirst$iv`: DoubleArray = this.$this_asList;
            var `index$iv`: Int = 0;
            val var6: Int = this.$this_asList.length;

            var var10000: Int;
            while (true) {
               if (`index$iv` >= var6) {
                  var10000 = -1;
                  break;
               }

               if (java.lang.Double.doubleToLongBits(`$this$indexOfFirst$iv`[`index$iv`]) == java.lang.Double.doubleToLongBits(element)) {
                  var10000 = `index$iv`;
                  break;
               }

               `index$iv`++;
            }

            return var10000;
         }

         public int lastIndexOf(double element) {
            val `$this$indexOfLast$iv`: DoubleArray = this.$this_asList;
            var var5: Int = this.$this_asList.length + -1;
            if (0 <= this.$this_asList.length + -1) {
               do {
                  val `index$iv`: Int = var5--;
                  if (java.lang.Double.doubleToLongBits(`$this$indexOfLast$iv`[`index$iv`]) == java.lang.Double.doubleToLongBits(element)) {
                     return `index$iv`;
                  }
               } while (0 <= var5);
            }

            return -1;
         }
      };
   }

   @JvmStatic
   public fun BooleanArray.asList(): List<Boolean> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(boolean element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public java.lang.Boolean get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(boolean element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(boolean element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun CharArray.asList(): List<Char> {
      return new RandomAccess(`$this$asList`) {
         {
            this.$this_asList = `$receiver`;
         }

         @Override
         public int getSize() {
            return this.$this_asList.length;
         }

         @Override
         public boolean isEmpty() {
            return this.$this_asList.length == 0;
         }

         public boolean contains(char element) {
            return ArraysKt.contains(this.$this_asList, element);
         }

         public Character get(int index) {
            return this.$this_asList[index];
         }

         public int indexOf(char element) {
            return ArraysKt.indexOf(this.$this_asList, element);
         }

         public int lastIndexOf(char element) {
            return ArraysKt.lastIndexOf(this.$this_asList, element);
         }
      };
   }

   @JvmStatic
   public fun <T> Array<out T>.binarySearch(element: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element, comparator);
   }

   @JvmStatic
   public fun <T> Array<out T>.binarySearch(element: T, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun ByteArray.binarySearch(element: Byte, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun ShortArray.binarySearch(element: Short, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun IntArray.binarySearch(element: Int, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun LongArray.binarySearch(element: Long, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun FloatArray.binarySearch(element: Float, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun DoubleArray.binarySearch(element: Double, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun CharArray.binarySearch(element: Char, fromIndex: Int = 0, toIndex: Int = `$this$binarySearch`.length): Int {
      return Arrays.binarySearch(`$this$binarySearch`, fromIndex, toIndex, element);
   }

   @SinceKotlin(version = "1.1")
   @LowPriorityInOverloadResolution
   @JvmName(name = "contentDeepEqualsInline")
   @InlineOnly
   @JvmStatic
   public inline infix fun <T> Array<out T>.contentDeepEquals(other: Array<out T>): Boolean {
      return ArraysKt.contentDeepEquals(`$this$contentDeepEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @JvmName(name = "contentDeepEqualsNullable")
   @InlineOnly
   @JvmStatic
   public inline infix fun <T> Array<out T>?.contentDeepEquals(other: Array<out T>?): Boolean {
      return ArraysKt.contentDeepEquals(`$this$contentDeepEquals`, other);
   }

   @SinceKotlin(version = "1.1")
   @LowPriorityInOverloadResolution
   @JvmName(name = "contentDeepHashCodeInline")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>.contentDeepHashCode(): Int {
      return ArraysKt.contentDeepHashCode(`$this$contentDeepHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @JvmName(name = "contentDeepHashCodeNullable")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>?.contentDeepHashCode(): Int {
      return ArraysKt.contentDeepHashCode(`$this$contentDeepHashCode`);
   }

   @SinceKotlin(version = "1.1")
   @LowPriorityInOverloadResolution
   @JvmName(name = "contentDeepToStringInline")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>.contentDeepToString(): String {
      return ArraysKt.contentDeepToString(`$this$contentDeepToString`);
   }

   @SinceKotlin(version = "1.4")
   @JvmName(name = "contentDeepToStringNullable")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>?.contentDeepToString(): String {
      return ArraysKt.contentDeepToString(`$this$contentDeepToString`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun <T> Array<out T>?.contentEquals(other: Array<out T>?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun ShortArray?.contentEquals(other: ShortArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun IntArray?.contentEquals(other: IntArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun LongArray?.contentEquals(other: LongArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun FloatArray?.contentEquals(other: FloatArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun DoubleArray?.contentEquals(other: DoubleArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun BooleanArray?.contentEquals(other: BooleanArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline infix fun CharArray?.contentEquals(other: CharArray?): Boolean {
      return Arrays.equals(`$this$contentEquals`, other);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun ByteArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun ShortArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun IntArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun LongArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun FloatArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun CharArray?.contentHashCode(): Int {
      return Arrays.hashCode(`$this$contentHashCode`);
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun ByteArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun ShortArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun IntArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun LongArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun FloatArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @SinceKotlin(version = "1.4")
   @InlineOnly
   @JvmStatic
   public inline fun CharArray?.contentToString(): String {
      val var10000: java.lang.String = Arrays.toString(`$this$contentToString`);
      return var10000;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun <T> Array<out T>.copyInto(destination: Array<T>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): Array<
         T
      > {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return (T[])destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun ByteArray.copyInto(destination: ByteArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): ByteArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun ShortArray.copyInto(destination: ShortArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): ShortArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun IntArray.copyInto(destination: IntArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): IntArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun LongArray.copyInto(destination: LongArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): LongArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun FloatArray.copyInto(destination: FloatArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): FloatArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun DoubleArray.copyInto(destination: DoubleArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): DoubleArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun BooleanArray.copyInto(destination: BooleanArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): BooleanArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @IgnorableReturnValue
   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun CharArray.copyInto(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = `$this$copyInto`.length): CharArray {
      System.arraycopy(`$this$copyInto`, startIndex, destination, destinationOffset, endIndex - startIndex);
      return destination;
   }

   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<T>.copyOf(): Array<T> {
      val var10000: Array<Any> = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return (T[])var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.copyOf(): ByteArray {
      val var10000: ByteArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.copyOf(): ShortArray {
      val var10000: ShortArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun IntArray.copyOf(): IntArray {
      val var10000: IntArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun LongArray.copyOf(): LongArray {
      val var10000: LongArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.copyOf(): FloatArray {
      val var10000: FloatArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.copyOf(): DoubleArray {
      val var10000: DoubleArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.copyOf(): BooleanArray {
      val var10000: BooleanArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun CharArray.copyOf(): CharArray {
      val var10000: CharArray = Arrays.copyOf(`$this$copyOf`, `$this$copyOf`.length);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.copyOf(newSize: Int): ByteArray {
      val var10000: ByteArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.copyOf(newSize: Int): ShortArray {
      val var10000: ShortArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun IntArray.copyOf(newSize: Int): IntArray {
      val var10000: IntArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun LongArray.copyOf(newSize: Int): LongArray {
      val var10000: LongArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.copyOf(newSize: Int): FloatArray {
      val var10000: FloatArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.copyOf(newSize: Int): DoubleArray {
      val var10000: DoubleArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.copyOf(newSize: Int): BooleanArray {
      val var10000: BooleanArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun CharArray.copyOf(newSize: Int): CharArray {
      val var10000: CharArray = Arrays.copyOf(`$this$copyOf`, newSize);
      return var10000;
   }

   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<T>.copyOf(newSize: Int): Array<T?> {
      val var10000: Array<Any> = Arrays.copyOf(`$this$copyOf`, newSize);
      return (T[])var10000;
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<T>.copyOfRange(fromIndex: Int, toIndex: Int): Array<T> {
      return (T[])ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.copyOfRange(fromIndex: Int, toIndex: Int): ByteArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.copyOfRange(fromIndex: Int, toIndex: Int): ShortArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun IntArray.copyOfRange(fromIndex: Int, toIndex: Int): IntArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun LongArray.copyOfRange(fromIndex: Int, toIndex: Int): LongArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.copyOfRange(fromIndex: Int, toIndex: Int): FloatArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.copyOfRange(fromIndex: Int, toIndex: Int): DoubleArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.copyOfRange(fromIndex: Int, toIndex: Int): BooleanArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @JvmName(name = "copyOfRangeInline")
   @InlineOnly
   @JvmStatic
   public inline fun CharArray.copyOfRange(fromIndex: Int, toIndex: Int): CharArray {
      return ArraysKt.copyOfRange(`$this$copyOfRange`, fromIndex, toIndex);
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun <T> Array<T>.copyOfRangeImpl(fromIndex: Int, toIndex: Int): Array<T> {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: Array<Any> = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return (T[])var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun ByteArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): ByteArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: ByteArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun ShortArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): ShortArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: ShortArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun IntArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): IntArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: IntArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun LongArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): LongArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: LongArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun FloatArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): FloatArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: FloatArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun DoubleArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): DoubleArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: DoubleArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun BooleanArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): BooleanArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: BooleanArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @PublishedApi
   @JvmName(name = "copyOfRange")
   @JvmStatic
   internal fun CharArray.copyOfRangeImpl(fromIndex: Int, toIndex: Int): CharArray {
      ArraysKt.copyOfRangeToIndexCheck(toIndex, `$this$copyOfRangeImpl`.length);
      val var10000: CharArray = Arrays.copyOfRange(`$this$copyOfRangeImpl`, fromIndex, toIndex);
      return var10000;
   }

   @JvmStatic
   public fun <T> Array<T>.fill(element: T, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun ByteArray.fill(element: Byte, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun ShortArray.fill(element: Short, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun IntArray.fill(element: Int, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun LongArray.fill(element: Long, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun FloatArray.fill(element: Float, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun DoubleArray.fill(element: Double, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun BooleanArray.fill(element: Boolean, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public fun CharArray.fill(element: Char, fromIndex: Int = 0, toIndex: Int = `$this$fill`.length) {
      Arrays.fill(`$this$fill`, fromIndex, toIndex, element);
   }

   @JvmStatic
   public operator fun <T> Array<T>.plus(element: T): Array<T> {
      val result: Array<Any> = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return (T[])result;
   }

   @JvmStatic
   public operator fun ByteArray.plus(element: Byte): ByteArray {
      val result: ByteArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun ShortArray.plus(element: Short): ShortArray {
      val result: ShortArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun IntArray.plus(element: Int): IntArray {
      val result: IntArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun LongArray.plus(element: Long): LongArray {
      val result: LongArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun FloatArray.plus(element: Float): FloatArray {
      val result: FloatArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun DoubleArray.plus(element: Double): DoubleArray {
      val result: DoubleArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun BooleanArray.plus(element: Boolean): BooleanArray {
      val result: BooleanArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun CharArray.plus(element: Char): CharArray {
      val result: CharArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + 1);
      result[`$this$plus`.length] = element;
      return result;
   }

   @JvmStatic
   public operator fun <T> Array<T>.plus(elements: Collection<T>): Array<T> {
      var index: Int = `$this$plus`.length;
      val result: Array<Any> = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());

      for (Object element : elements) {
         result[index++] = element;
      }

      return (T[])result;
   }

   @JvmStatic
   public operator fun ByteArray.plus(elements: Collection<Byte>): ByteArray {
      var index: Int = `$this$plus`.length;
      val result: ByteArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).byteValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun ShortArray.plus(elements: Collection<Short>): ShortArray {
      var index: Int = `$this$plus`.length;
      val result: ShortArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).shortValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun IntArray.plus(elements: Collection<Int>): IntArray {
      var index: Int = `$this$plus`.length;
      val result: IntArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).intValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun LongArray.plus(elements: Collection<Long>): LongArray {
      var index: Int = `$this$plus`.length;
      val result: LongArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).longValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun FloatArray.plus(elements: Collection<Float>): FloatArray {
      var index: Int = `$this$plus`.length;
      val result: FloatArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).floatValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun DoubleArray.plus(elements: Collection<Double>): DoubleArray {
      var index: Int = `$this$plus`.length;
      val result: DoubleArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());
      val var4: java.util.Iterator = elements.iterator();

      while (var4.hasNext()) {
         result[index++] = (var4.next() as java.lang.Number).doubleValue();
      }

      return result;
   }

   @JvmStatic
   public operator fun BooleanArray.plus(elements: Collection<Boolean>): BooleanArray {
      var index: Int = `$this$plus`.length;
      val result: BooleanArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());

      for (boolean element : elements) {
         result[index++] = element;
      }

      return result;
   }

   @JvmStatic
   public operator fun CharArray.plus(elements: Collection<Char>): CharArray {
      var index: Int = `$this$plus`.length;
      val result: CharArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.size());

      for (char element : elements) {
         result[index++] = element;
      }

      return result;
   }

   @JvmStatic
   public operator fun <T> Array<T>.plus(elements: Array<out T>): Array<T> {
      val result: Array<Any> = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return (T[])result;
   }

   @JvmStatic
   public operator fun ByteArray.plus(elements: ByteArray): ByteArray {
      val result: ByteArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun ShortArray.plus(elements: ShortArray): ShortArray {
      val result: ShortArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun IntArray.plus(elements: IntArray): IntArray {
      val result: IntArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun LongArray.plus(elements: LongArray): LongArray {
      val result: LongArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun FloatArray.plus(elements: FloatArray): FloatArray {
      val result: FloatArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun DoubleArray.plus(elements: DoubleArray): DoubleArray {
      val result: DoubleArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun BooleanArray.plus(elements: BooleanArray): BooleanArray {
      val result: BooleanArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @JvmStatic
   public operator fun CharArray.plus(elements: CharArray): CharArray {
      val result: CharArray = Arrays.copyOf(`$this$plus`, `$this$plus`.length + elements.length);
      System.arraycopy(elements, 0, result, `$this$plus`.length, elements.length);
      return result;
   }

   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<T>.plusElement(element: T): Array<T> {
      return (T[])ArraysKt.plus(`$this$plusElement`, element);
   }

   @JvmStatic
   public fun IntArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun LongArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun ByteArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun ShortArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun DoubleArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun FloatArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @JvmStatic
   public fun CharArray.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @InlineOnly
   @JvmStatic
   public inline fun <T : Comparable<T>> Array<out T>.sort() {
      ArraysKt.sort(`$this$sort`);
   }

   @JvmStatic
   public fun <T> Array<out T>.sort() {
      if (`$this$sort`.length > 1) {
         Arrays.sort(`$this$sort`);
      }
   }

   @SinceKotlin(version = "1.4")
   @JvmStatic
   public fun <T : Comparable<T>> Array<out T>.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort((Object[])`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun ByteArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun ShortArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun IntArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun LongArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun FloatArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun DoubleArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun CharArray.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun <T> Array<out T>.sort(fromIndex: Int = 0, toIndex: Int = `$this$sort`.length) {
      Arrays.sort(`$this$sort`, fromIndex, toIndex);
   }

   @JvmStatic
   public fun <T> Array<out T>.sortWith(comparator: Comparator<in T>) {
      if (`$this$sortWith`.length > 1) {
         Arrays.sort(`$this$sortWith`, comparator);
      }
   }

   @JvmStatic
   public fun <T> Array<out T>.sortWith(comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = `$this$sortWith`.length) {
      Arrays.sort(`$this$sortWith`, fromIndex, toIndex, comparator);
   }

   @JvmStatic
   public fun ByteArray.toTypedArray(): Array<Byte> {
      val result: Array<java.lang.Byte> = new java.lang.Byte[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun ShortArray.toTypedArray(): Array<Short> {
      val result: Array<java.lang.Short> = new java.lang.Short[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun IntArray.toTypedArray(): Array<Int> {
      val result: Array<Int> = new Integer[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun LongArray.toTypedArray(): Array<Long> {
      val result: Array<java.lang.Long> = new java.lang.Long[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun FloatArray.toTypedArray(): Array<Float> {
      val result: Array<java.lang.Float> = new java.lang.Float[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun DoubleArray.toTypedArray(): Array<Double> {
      val result: Array<java.lang.Double> = new java.lang.Double[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun BooleanArray.toTypedArray(): Array<Boolean> {
      val result: Array<java.lang.Boolean> = new java.lang.Boolean[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun CharArray.toTypedArray(): Array<Char> {
      val result: Array<Character> = new Character[`$this$toTypedArray`.length];
      var index: Int = 0;

      for (int var3 = $this$toTypedArray.length; index < var3; index++) {
         result[index] = `$this$toTypedArray`[index];
      }

      return result;
   }

   @JvmStatic
   public fun <T : Comparable<T>> Array<out T>.toSortedSet(): SortedSet<T> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet()) as SortedSet<T>;
   }

   @JvmStatic
   public fun ByteArray.toSortedSet(): SortedSet<Byte> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun ShortArray.toSortedSet(): SortedSet<Short> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun IntArray.toSortedSet(): SortedSet<Int> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun LongArray.toSortedSet(): SortedSet<Long> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun FloatArray.toSortedSet(): SortedSet<Float> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun DoubleArray.toSortedSet(): SortedSet<Double> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun BooleanArray.toSortedSet(): SortedSet<Boolean> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun CharArray.toSortedSet(): SortedSet<Char> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet<>());
   }

   @JvmStatic
   public fun <T> Array<out T>.toSortedSet(comparator: Comparator<in T>): SortedSet<T> {
      return ArraysKt.toCollection(`$this$toSortedSet`, new TreeSet(comparator)) as SortedSet<T>;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>.sumOf(selector: (T) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (Object element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.sumOf(selector: (Byte) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (byte element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.sumOf(selector: (Short) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (short element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun IntArray.sumOf(selector: (Int) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (int element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun LongArray.sumOf(selector: (Long) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (long element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.sumOf(selector: (Float) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (float element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.sumOf(selector: (Double) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (double element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.sumOf(selector: (Boolean) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (boolean element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigDecimal")
   @InlineOnly
   @JvmStatic
   public inline fun CharArray.sumOf(selector: (Char) -> BigDecimal): BigDecimal {
      var var10000: BigDecimal = BigDecimal.valueOf(0L);
      var sum: BigDecimal = var10000;

      for (char element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigDecimal);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Array<out T>.sumOf(selector: (T) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (Object element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun ByteArray.sumOf(selector: (Byte) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (byte element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun ShortArray.sumOf(selector: (Short) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (short element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun IntArray.sumOf(selector: (Int) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (int element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun LongArray.sumOf(selector: (Long) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (long element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun FloatArray.sumOf(selector: (Float) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (float element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun DoubleArray.sumOf(selector: (Double) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (double element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun BooleanArray.sumOf(selector: (Boolean) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (boolean element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   @SinceKotlin(version = "1.4")
   @OverloadResolutionByLambdaReturnType
   @JvmName(name = "sumOfBigInteger")
   @InlineOnly
   @JvmStatic
   public inline fun CharArray.sumOf(selector: (Char) -> BigInteger): BigInteger {
      var var10000: BigInteger = BigInteger.valueOf(0L);
      var sum: BigInteger = var10000;

      for (char element : $this$sumOf) {
         var10000 = sum.add(selector.invoke(element) as BigInteger);
         sum = var10000;
      }

      return sum;
   }

   open fun ArraysKt___ArraysJvmKt() {
   }
}
