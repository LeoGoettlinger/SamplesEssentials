package kotlin.time

import kotlin.contracts.InvocationKind
import kotlin.internal.InlineOnly
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.SourceDebugExtension
import kotlin.math.MathKt

@JvmInline
@SinceKotlin(version = "1.6")
@SourceDebugExtension(["SMAP\nDuration.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Duration.kt\nkotlin/time/Duration\n+ 2 _Strings.kt\nkotlin/text/StringsKt___StringsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,1613:1\n41#1:1614\n41#1:1615\n41#1:1616\n41#1:1617\n41#1:1618\n572#1:1619\n589#1:1627\n173#2,6:1620\n1#3:1626\n*S KotlinDebug\n*F\n+ 1 Duration.kt\nkotlin/time/Duration\n*L\n42#1:1614\n43#1:1615\n353#1:1616\n362#1:1617\n546#1:1618\n847#1:1619\n938#1:1627\n889#1:1620,6\n*E\n"])
public inline class Duration : java.lang.Comparable<Duration> {
   private final val rawValue: Long

   private final val value: Long
      private final get() {
         return arg0 shr 1;
      }


   private final val unitDiscriminator: Int
      private final inline get() {
         return (int)arg0 and 1;
      }


   private final val storageUnit: DurationUnit
      private final get() {
         return if (isInNanos-impl(arg0)) DurationUnit.NANOSECONDS else DurationUnit.MILLISECONDS;
      }


   public final val absoluteValue: Duration
      public final get() {
         return if (isNegative-impl(arg0)) unaryMinus-UwyO8pc(arg0) else arg0;
      }


   @PublishedApi
   internal final val hoursComponent: Int
      internal final get() {
         return if (isInfinite-impl(arg0)) 0 else (int)(getInWholeHours-impl(arg0) % 24);
      }


   @PublishedApi
   internal final val minutesComponent: Int
      internal final get() {
         return if (isInfinite-impl(arg0)) 0 else (int)(getInWholeMinutes-impl(arg0) % 60);
      }


   @PublishedApi
   internal final val secondsComponent: Int
      internal final get() {
         return if (isInfinite-impl(arg0)) 0 else (int)(getInWholeSeconds-impl(arg0) % 60);
      }


   @PublishedApi
   internal final val nanosecondsComponent: Int
      internal final get() {
         return if (isInfinite-impl(arg0))
            0
            else
            (if (isInMillis-impl(arg0)) (int)DurationKt.access$millisToNanos(getValue-impl(arg0) % (long)1000) else (int)(getValue-impl(arg0) % 1000000000));
      }


   public final val inWholeDays: Long
      public final get() {
         return toLong-impl(arg0, DurationUnit.DAYS);
      }


   public final val inWholeHours: Long
      public final get() {
         return toLong-impl(arg0, DurationUnit.HOURS);
      }


   public final val inWholeMinutes: Long
      public final get() {
         return toLong-impl(arg0, DurationUnit.MINUTES);
      }


   public final val inWholeSeconds: Long
      public final get() {
         return toLong-impl(arg0, DurationUnit.SECONDS);
      }


   public final val inWholeMilliseconds: Long
      public final get() {
         return if (isInMillis-impl(arg0) && isFinite-impl(arg0)) getValue-impl(arg0) else toLong-impl(arg0, DurationUnit.MILLISECONDS);
      }


   public final val inWholeMicroseconds: Long
      public final get() {
         return toLong-impl(arg0, DurationUnit.MICROSECONDS);
      }


   public final val inWholeNanoseconds: Long
      public final get() {
         val value: Long = getValue-impl(arg0);
         return if (isInNanos-impl(arg0))
            value
            else
            (
               if (value > 9223372036854L)
                  java.lang.Long.MAX_VALUE
                  else
                  (if (value < -9223372036854L) java.lang.Long.MIN_VALUE else DurationKt.access$millisToNanos(value))
            );
      }


   @JvmStatic
   private fun isInNanos(): Boolean {
      return ((int)arg0 and 1) == 0;
   }

   @JvmStatic
   private fun isInMillis(): Boolean {
      return ((int)arg0 and 1) == 1;
   }

   @JvmStatic
   public operator fun unaryMinus(): Duration {
      return DurationKt.access$durationOf(-getValue-impl(arg0), (int)arg0 and 1);
   }

   @JvmStatic
   public operator fun plus(other: Duration): Duration {
      val var12: Long;
      if (((int)arg0 and 1) == ((int)other and 1)) {
         if (isInNanos-impl(arg0)) {
            var12 = DurationKt.access$durationOfNanosNormalized(getValue-impl(arg0) + getValue-impl(other));
         } else {
            val it: Long = DurationKt.access$addMillisWithoutOverflow(getValue-impl(arg0), getValue-impl(other));
            if (it == 9223372036854759646L) {
               throw new IllegalArgumentException("Summing infinite durations of different signs yields an undefined result.");
            }

            var12 = if (it == 4611686018427387903L || it == -4611686018427387903L)
               DurationKt.access$durationOfMillis(it)
               else
               DurationKt.access$durationOfMillisNormalized(it);
         }
      } else {
         var12 = if (isInMillis-impl(arg0))
            addValuesMixedRanges-UwyO8pc(arg0, getValue-impl(arg0), getValue-impl(other))
            else
            addValuesMixedRanges-UwyO8pc(arg0, getValue-impl(other), getValue-impl(arg0));
      }

      return var12;
   }

   @JvmStatic
   private fun addValuesMixedRanges(thisMillis: Long, otherNanos: Long): Duration {
      val otherMillis: Long = DurationKt.access$nanosToMillis(otherNanos);
      val resultMillis: Long = DurationKt.access$addMillisWithoutOverflow(thisMillis, otherMillis);
      return if (-4611686018426L <= resultMillis && resultMillis < 4611686018427L)
         DurationKt.access$durationOfNanos(DurationKt.access$millisToNanos(resultMillis) + (otherNanos - DurationKt.access$millisToNanos(otherMillis)))
         else
         DurationKt.access$durationOfMillis(resultMillis);
   }

   @JvmStatic
   public operator fun minus(other: Duration): Duration {
      return plus-LRDsOJo(arg0, unaryMinus-UwyO8pc(other));
   }

   @JvmStatic
   public operator fun times(scale: Int): Duration {
      if (isInfinite-impl(arg0)) {
         if (scale == 0) {
            throw new IllegalArgumentException("Multiplying infinite duration by zero yields an undefined result.");
         } else {
            return if (scale > 0) arg0 else unaryMinus-UwyO8pc(arg0);
         }
      } else if (scale == 0) {
         return ZERO;
      } else {
         val value: Long = getValue-impl(arg0);
         val result: Long = value * scale;
         val var10000: Long;
         if (isInNanos-impl(arg0)) {
            if (-2147483647L <= value && value < 2147483648L) {
               var10000 = DurationKt.access$durationOfNanos(result);
            } else if (result / scale == value) {
               var10000 = DurationKt.access$durationOfNanosNormalized(result);
            } else {
               val millis: Long = DurationKt.access$nanosToMillis(value);
               val totalMillis: Long = millis * scale + DurationKt.access$nanosToMillis((value - DurationKt.access$millisToNanos(millis)) * (long)scale);
               var10000 = if (millis * scale / scale == millis && (totalMillis xor millis * scale) >= 0L)
                  DurationKt.access$durationOfMillis(RangesKt.coerceIn(totalMillis, new LongRange(-4611686018427387903L, 4611686018427387903L)))
                  else
                  (if (MathKt.getSign(value) * MathKt.getSign(scale) > 0) INFINITE else NEG_INFINITE);
            }
         } else {
            var10000 = if (result / scale == value)
               DurationKt.access$durationOfMillis(RangesKt.coerceIn(result, new LongRange(-4611686018427387903L, 4611686018427387903L)))
               else
               (if (MathKt.getSign(value) * MathKt.getSign(scale) > 0) INFINITE else NEG_INFINITE);
         }

         return var10000;
      }
   }

   @JvmStatic
   public operator fun times(scale: Double): Duration {
      val intScale: Int = MathKt.roundToInt(scale);
      if (intScale == scale) {
         return times-UwyO8pc(arg0, intScale);
      } else {
         val unit: DurationUnit = getStorageUnit-impl(arg0);
         return DurationKt.toDuration(toDouble-impl(arg0, unit) * scale, unit);
      }
   }

   @JvmStatic
   public operator fun div(scale: Int): Duration {
      if (scale == 0) {
         val var10000: Long;
         if (isPositive-impl(arg0)) {
            var10000 = INFINITE;
         } else {
            if (!isNegative-impl(arg0)) {
               throw new IllegalArgumentException("Dividing zero duration by zero yields an undefined result.");
            }

            var10000 = NEG_INFINITE;
         }

         return var10000;
      } else if (isInNanos-impl(arg0)) {
         return DurationKt.access$durationOfNanos(getValue-impl(arg0) / (long)scale);
      } else {
         label32:
         if (isInfinite-impl(arg0)) {
            return times-UwyO8pc(arg0, MathKt.getSign(scale));
         } else {
            val result: Long = getValue-impl(arg0) / scale;
            return if (-4611686018426L <= result && result < 4611686018427L)
               DurationKt.access$durationOfNanos(
                  DurationKt.access$millisToNanos(result) + DurationKt.access$millisToNanos(getValue-impl(arg0) - result * (long)scale) / (long)scale
               )
               else
               DurationKt.access$durationOfMillis(result);
         }
      }
   }

   @JvmStatic
   public operator fun div(scale: Double): Duration {
      val intScale: Int = MathKt.roundToInt(scale);
      if (intScale == scale && intScale != 0) {
         return div-UwyO8pc(arg0, intScale);
      } else {
         val unit: DurationUnit = getStorageUnit-impl(arg0);
         return DurationKt.toDuration(toDouble-impl(arg0, unit) / scale, unit);
      }
   }

   @JvmStatic
   public operator fun div(other: Duration): Double {
      val coarserUnit: DurationUnit = ComparisonsKt.maxOf(getStorageUnit-impl(arg0), getStorageUnit-impl(other));
      return toDouble-impl(arg0, coarserUnit) / toDouble-impl(other, coarserUnit);
   }

   @JvmStatic
   internal fun truncateTo(unit: DurationUnit): Duration {
      val storageUnit: DurationUnit = getStorageUnit-impl(arg0);
      return if (unit.compareTo(storageUnit) > 0 && !isInfinite-impl(arg0))
         DurationKt.toDuration(getValue-impl(arg0) - getValue-impl(arg0) % DurationUnitKt.convertDurationUnit(1L, unit, storageUnit), storageUnit)
         else
         arg0;
   }

   @JvmStatic
   public fun isNegative(): Boolean {
      return arg0 < 0L;
   }

   @JvmStatic
   public fun isPositive(): Boolean {
      return arg0 > 0L;
   }

   @JvmStatic
   public fun isInfinite(): Boolean {
      return arg0 == INFINITE || arg0 == NEG_INFINITE;
   }

   @JvmStatic
   public fun isFinite(): Boolean {
      return !isInfinite-impl(arg0);
   }

   @JvmStatic
   public open operator fun compareTo(other: Duration): Int {
      if ((arg0 xor other) >= 0L && ((int)(arg0 xor other) and 1) != 0) {
         return if (isNegative-impl(arg0)) -(((int)arg0 and 1) - ((int)other and 1)) else ((int)arg0 and 1) - ((int)other and 1);
      } else {
         return Intrinsics.compare(arg0, other);
      }
   }

   fun `compareTo-LRDsOJo`(other: Long): Int {
      return compareTo-LRDsOJo(this.rawValue, other);
   }

   @JvmStatic
   public inline fun <T> toComponents(action: (Long, Int, Int, Int, Int) -> T): T {
      contract {
         callsInPlace(action, InvocationKind.EXACTLY_ONCE)
      }

      return (T)action.invoke(
         getInWholeDays-impl(arg0),
         getHoursComponent-impl(arg0),
         getMinutesComponent-impl(arg0),
         getSecondsComponent-impl(arg0),
         getNanosecondsComponent-impl(arg0)
      );
   }

   @JvmStatic
   public inline fun <T> toComponents(action: (Long, Int, Int, Int) -> T): T {
      contract {
         callsInPlace(action, InvocationKind.EXACTLY_ONCE)
      }

      return (T)action.invoke(getInWholeHours-impl(arg0), getMinutesComponent-impl(arg0), getSecondsComponent-impl(arg0), getNanosecondsComponent-impl(arg0));
   }

   @JvmStatic
   public inline fun <T> toComponents(action: (Long, Int, Int) -> T): T {
      contract {
         callsInPlace(action, InvocationKind.EXACTLY_ONCE)
      }

      return (T)action.invoke(getInWholeMinutes-impl(arg0), getSecondsComponent-impl(arg0), getNanosecondsComponent-impl(arg0));
   }

   @JvmStatic
   public inline fun <T> toComponents(action: (Long, Int) -> T): T {
      contract {
         callsInPlace(action, InvocationKind.EXACTLY_ONCE)
      }

      return (T)action.invoke(getInWholeSeconds-impl(arg0), getNanosecondsComponent-impl(arg0));
   }

   @JvmStatic
   public fun toDouble(unit: DurationUnit): Double {
      return if (arg0 == INFINITE)
         java.lang.Double.POSITIVE_INFINITY
         else
         (
            if (arg0 == NEG_INFINITE)
               java.lang.Double.NEGATIVE_INFINITY
               else
               DurationUnitKt.convertDurationUnit((double)getValue-impl(arg0), getStorageUnit-impl(arg0), unit)
         );
   }

   @JvmStatic
   public fun toLong(unit: DurationUnit): Long {
      return if (arg0 == INFINITE)
         java.lang.Long.MAX_VALUE
         else
         (if (arg0 == NEG_INFINITE) java.lang.Long.MIN_VALUE else DurationUnitKt.convertDurationUnit(getValue-impl(arg0), getStorageUnit-impl(arg0), unit));
   }

   @JvmStatic
   public fun toInt(unit: DurationUnit): Int {
      return (int)RangesKt.coerceIn(toLong-impl(arg0, unit), -2147483648L, 2147483647L);
   }

   @JvmStatic
   public open fun toString(): String {
      val var10000: java.lang.String;
      if (arg0 == 0L) {
         var10000 = "0s";
      } else if (arg0 == INFINITE) {
         var10000 = "Infinity";
      } else if (arg0 == NEG_INFINITE) {
         var10000 = "-Infinity";
      } else {
         val isNegative: Boolean = isNegative-impl(arg0);
         val var5: StringBuilder = new StringBuilder();
         if (isNegative) {
            var5.append('-');
         }

         val `arg0$iv`: Long = getAbsoluteValue-UwyO8pc(arg0);
         val var23: Long = getInWholeDays-impl(`arg0$iv`);
         val var10001: Int = getHoursComponent-impl(`arg0$iv`);
         val var10002: Int = getMinutesComponent-impl(`arg0$iv`);
         val nanoseconds: Int = getNanosecondsComponent-impl(`arg0$iv`);
         val seconds: Int = getSecondsComponent-impl(`arg0$iv`);
         val hasDays: Boolean = var23 != 0L;
         val hasHours: Boolean = var10001 != 0;
         val hasMinutes: Boolean = var10002 != 0;
         val hasSeconds: Boolean = seconds != 0 || nanoseconds != 0;
         if (hasDays) {
            var5.append(var23).append('d');
            0++;
         }

         if (hasHours || hasDays && (hasMinutes || hasSeconds)) {
            if (0++ > 0) {
               var5.append(' ');
            }

            var5.append(var10001).append('h');
         }

         if (hasMinutes || hasSeconds && (hasHours || hasDays)) {
            if (0++ > 0) {
               var5.append(' ');
            }

            var5.append(var10002).append('m');
         }

         if (hasSeconds) {
            if (0++ > 0) {
               var5.append(' ');
            }

            if (seconds != 0 || hasDays || hasHours || hasMinutes) {
               appendFractional-impl(arg0, var5, seconds, nanoseconds, 9, "s", false);
            } else if (nanoseconds >= 1000000) {
               appendFractional-impl(arg0, var5, nanoseconds / 1000000, nanoseconds % 1000000, 6, "ms", false);
            } else if (nanoseconds >= 1000) {
               appendFractional-impl(arg0, var5, nanoseconds / 1000, nanoseconds % 1000, 3, "us", false);
            } else {
               var5.append(nanoseconds).append("ns");
            }
         }

         if (isNegative && 0 > 1) {
            var5.insert(1, '(').append(')');
         }

         var10000 = var5.toString();
      }

      return var10000;
   }

   override fun toString(): java.lang.String {
      return toString-impl(this.rawValue);
   }

   @JvmStatic
   private fun StringBuilder.appendFractional(whole: Int, fractional: Int, fractionalSize: Int, unit: String, isoZeroes: Boolean) {
      `$this$appendFractional`.append(whole);
      if (fractional != 0) {
         var fracString: java.lang.String;
         var var10000: Int;
         label33: {
            `$this$appendFractional`.append('.');
            fracString = StringsKt.padStart(java.lang.String.valueOf(fractional), fractionalSize, '0');
            val `$this$indexOfLast$iv`: java.lang.CharSequence = fracString;
            var var12: Int = fracString.length() + -1;
            if (0 <= var12) {
               do {
                  val `index$iv`: Int = var12--;
                  if (`$this$indexOfLast$iv`.charAt(`index$iv`) != '0') {
                     var10000 = `index$iv`;
                     break label33;
                  }
               } while (0 <= var12);
            }

            var10000 = -1;
         }

         val nonZeroDigits: Int = var10000 + 1;
         if (!isoZeroes && var10000 + 1 < 3) {
            ;
         }
      }

      `$this$appendFractional`.append(unit);
   }

   @JvmStatic
   public fun toString(unit: DurationUnit, decimals: Int = ...): String {
      if (decimals < 0) {
         throw new IllegalArgumentException(("decimals must be not negative, but was $decimals").toString());
      } else {
         val number: Double = toDouble-impl(arg0, unit);
         return if (java.lang.Double.isInfinite(number))
            java.lang.String.valueOf(number)
            else
            "${DurationJvmKt.formatToExactDecimals(number, RangesKt.coerceAtMost(decimals, 12))}${DurationUnitKt.shortName(unit)}";
      }
   }

   @JvmStatic
   public fun toIsoString(): String {
      val var2: StringBuilder = new StringBuilder();
      if (isNegative-impl(arg0)) {
         var2.append('-');
      }

      var2.append("PT");
      val `arg0$iv`: Long = getAbsoluteValue-UwyO8pc(arg0);
      val var10000: Long = getInWholeHours-impl(`arg0$iv`);
      val var10001: Int = getMinutesComponent-impl(`arg0$iv`);
      val nanoseconds: Int = getNanosecondsComponent-impl(`arg0$iv`);
      val seconds: Int = getSecondsComponent-impl(`arg0$iv`);
      var hours: Long = var10000;
      if (isInfinite-impl(arg0)) {
         hours = 9999999999999L;
      }

      val hasHours: Boolean = hours != 0L;
      val hasSeconds: Boolean = seconds != 0 || nanoseconds != 0;
      val hasMinutes: Boolean = var10001 != 0 || (seconds != 0 || nanoseconds != 0) && hasHours;
      if (hasHours) {
         var2.append(hours).append('H');
      }

      if (hasMinutes) {
         var2.append(var10001).append('M');
      }

      if (hasSeconds || !hasHours && !hasMinutes) {
         appendFractional-impl(arg0, var2, seconds, nanoseconds, 9, "S", true);
      }

      return var2.toString();
   }

   @JvmStatic
   fun `hashCode-impl`(arg0: Long): Int {
      return java.lang.Long.hashCode(arg0);
   }

   public override fun hashCode(): Int {
      return hashCode-impl(this.rawValue);
   }

   @JvmStatic
   fun `equals-impl`(arg0: Long, other: Any): Boolean {
      if (other !is Duration) {
         return false;
      } else {
         return arg0 == (other as Duration).unbox-impl();
      }
   }

   public override operator fun equals(other: Any?): Boolean {
      return equals-impl(this.rawValue, other);
   }

   /** @deprecated */
   @Deprecated(message = "Don't call this constructor directly.", level = DeprecationLevel.ERROR)
   @JvmStatic
   fun `constructor-impl`(rawValue: Long): Long {
      return rawValue;
   }

   @JvmStatic
   fun `equals-impl0`(p1: Long, p2: Long): Boolean {
      return p1 == p2;
   }

   @SourceDebugExtension(["SMAP\nDuration.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Duration.kt\nkotlin/time/Duration$Companion\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 Duration.kt\nkotlin/time/DurationKt\n*L\n1#1,1613:1\n1#2:1614\n1449#3:1615\n1449#3:1616\n*S KotlinDebug\n*F\n+ 1 Duration.kt\nkotlin/time/Duration$Companion\n*L\n337#1:1615\n347#1:1616\n*E\n"])
   public companion object {
      public final val ZERO: Duration
      public final val INFINITE: Duration
      internal final val NEG_INFINITE: Duration
      internal const val INVALID_RAW_VALUE: Long
      internal final val INVALID: Duration

      @InlineOnly
      public final val nanoseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$nanoseconds`, DurationUnit.NANOSECONDS);
         }


      @InlineOnly
      public final val nanoseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$nanoseconds`, DurationUnit.NANOSECONDS);
         }


      @InlineOnly
      public final val nanoseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$nanoseconds`, DurationUnit.NANOSECONDS);
         }


      @InlineOnly
      public final val microseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$microseconds`, DurationUnit.MICROSECONDS);
         }


      @InlineOnly
      public final val microseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$microseconds`, DurationUnit.MICROSECONDS);
         }


      @InlineOnly
      public final val microseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$microseconds`, DurationUnit.MICROSECONDS);
         }


      @InlineOnly
      public final val milliseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$milliseconds`, DurationUnit.MILLISECONDS);
         }


      @InlineOnly
      public final val milliseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$milliseconds`, DurationUnit.MILLISECONDS);
         }


      @InlineOnly
      public final val milliseconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$milliseconds`, DurationUnit.MILLISECONDS);
         }


      @InlineOnly
      public final val seconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$seconds`, DurationUnit.SECONDS);
         }


      @InlineOnly
      public final val seconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$seconds`, DurationUnit.SECONDS);
         }


      @InlineOnly
      public final val seconds: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$seconds`, DurationUnit.SECONDS);
         }


      @InlineOnly
      public final val minutes: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$minutes`, DurationUnit.MINUTES);
         }


      @InlineOnly
      public final val minutes: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$minutes`, DurationUnit.MINUTES);
         }


      @InlineOnly
      public final val minutes: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$minutes`, DurationUnit.MINUTES);
         }


      @InlineOnly
      public final val hours: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$hours`, DurationUnit.HOURS);
         }


      @InlineOnly
      public final val hours: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$hours`, DurationUnit.HOURS);
         }


      @InlineOnly
      public final val hours: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$hours`, DurationUnit.HOURS);
         }


      @InlineOnly
      public final val days: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$days`, DurationUnit.DAYS);
         }


      @InlineOnly
      public final val days: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$days`, DurationUnit.DAYS);
         }


      @InlineOnly
      public final val days: Duration
         public final inline get() {
            return DurationKt.toDuration(`$this$days`, DurationUnit.DAYS);
         }


      internal fun fromRawValue(rawValue: Long): Duration {
         val var3: Long = Duration.constructor-impl(rawValue);
         if (DurationJvmKt.getDurationAssertionsEnabled()) {
            if (Duration.access$isInNanos-impl(var3)) {
               val var8: Long = Duration.access$getValue-impl(var3);
               if (-4611686018426999999L > var8 || var8 >= 4611686018427000000L) {
                  throw new AssertionError("${Duration.access$getValue-impl(var3)} ns is out of nanoseconds range");
               }
            } else {
               var var10: Long = Duration.access$getValue-impl(var3);
               if (-4611686018427387903L >= var10 || var10 >= 4611686018427387903L) {
                  var10 = Duration.access$getValue-impl(var3);
                  if (var10 != 4611686018427387903L && var10 != -4611686018427387903L) {
                     throw new AssertionError("${Duration.access$getValue-impl(var3)} ms is out of milliseconds range");
                  }
               }

               var10 = Duration.access$getValue-impl(var3);
               if (-4611686018426L <= var10 && var10 < 4611686018427L) {
                  throw new AssertionError("${Duration.access$getValue-impl(var3)} ms is denormalized");
               }
            }
         }

         return var3;
      }

      @ExperimentalTime
      public fun convert(value: Double, sourceUnit: DurationUnit, targetUnit: DurationUnit): Double {
         return DurationUnitKt.convertDurationUnit(value, sourceUnit, targetUnit);
      }

      public fun parse(value: String): Duration {
         try {
            val var2: Long = DurationKt.parseDuration$default(value, false, false, 4, null);
            if (Duration.equals-impl0(var2, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib())) {
               throw new IllegalStateException("invariant failed".toString());
            } else {
               return var2;
            }
         } catch (var8: IllegalArgumentException) {
            throw new IllegalArgumentException("Invalid duration string format: '$value'.", var8);
         }
      }

      public fun parseIsoString(value: String): Duration {
         try {
            val var2: Long = DurationKt.parseDuration$default(value, true, false, 4, null);
            if (Duration.equals-impl0(var2, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib())) {
               throw new IllegalStateException("invariant failed".toString());
            } else {
               return var2;
            }
         } catch (var8: IllegalArgumentException) {
            throw new IllegalArgumentException("Invalid ISO duration string format: '$value'.", var8);
         }
      }

      public fun parseOrNull(value: String): Duration? {
         val `$this$onInvalid_u2dge6A_vg$iv`: Long = DurationKt.access$parseDuration(value, false, false);
         return if (Duration.equals-impl0(`$this$onInvalid_u2dge6A_vg$iv`, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib()))
            null
            else
            Duration.box-impl(`$this$onInvalid_u2dge6A_vg$iv`);
      }

      public fun parseIsoStringOrNull(value: String): Duration? {
         val `$this$onInvalid_u2dge6A_vg$iv`: Long = DurationKt.access$parseDuration(value, true, false);
         return if (Duration.equals-impl0(`$this$onInvalid_u2dge6A_vg$iv`, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib()))
            null
            else
            Duration.box-impl(`$this$onInvalid_u2dge6A_vg$iv`);
      }
   }
}
