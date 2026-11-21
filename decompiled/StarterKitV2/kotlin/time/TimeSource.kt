package kotlin.time

@SinceKotlin(version = "1.9")
@WasExperimental(markerClass = [ExperimentalTime::class])
public interface TimeSource {
   public abstract fun markNow(): TimeMark {
   }

   public companion object

   public object Monotonic : TimeSource.WithComparableMarks {
      public open fun markNow(): kotlin.time.TimeSource.Monotonic.ValueTimeMark {
         return MonotonicTimeSource.INSTANCE.markNow-z9LOYto();
      }

      public override fun toString(): String {
         return MonotonicTimeSource.INSTANCE.toString();
      }

      @JvmInline
      @SinceKotlin(version = "1.9")
      @WasExperimental(markerClass = [ExperimentalTime::class])
      public inline class ValueTimeMark : ComparableTimeMark {
         internal final val reading: Long

         @JvmStatic
         public open fun elapsedNow(): Duration {
            return MonotonicTimeSource.INSTANCE.elapsedFrom-6eNON_k(arg0);
         }

         override fun `elapsedNow-UwyO8pc`(): Long {
            return elapsedNow-UwyO8pc(this.reading);
         }

         @JvmStatic
         public open operator fun plus(duration: Duration): kotlin.time.TimeSource.Monotonic.ValueTimeMark {
            return MonotonicTimeSource.INSTANCE.adjustReading-6QKq23U(arg0, duration);
         }

         fun `plus-LRDsOJo`(duration: Long): Long {
            return plus-LRDsOJo(this.reading, duration);
         }

         @JvmStatic
         public open operator fun minus(duration: Duration): kotlin.time.TimeSource.Monotonic.ValueTimeMark {
            return MonotonicTimeSource.INSTANCE.adjustReading-6QKq23U(arg0, Duration.unaryMinus-UwyO8pc(duration));
         }

         fun `minus-LRDsOJo`(duration: Long): Long {
            return minus-LRDsOJo(this.reading, duration);
         }

         @JvmStatic
         public open fun hasPassedNow(): Boolean {
            return !Duration.isNegative-impl(elapsedNow-UwyO8pc(arg0));
         }

         override fun hasPassedNow(): Boolean {
            return hasPassedNow-impl(this.reading);
         }

         @JvmStatic
         public open fun hasNotPassedNow(): Boolean {
            return Duration.isNegative-impl(elapsedNow-UwyO8pc(arg0));
         }

         override fun hasNotPassedNow(): Boolean {
            return hasNotPassedNow-impl(this.reading);
         }

         @JvmStatic
         public open operator fun minus(other: ComparableTimeMark): Duration {
            if (other !is TimeSource.Monotonic.ValueTimeMark) {
               throw new IllegalArgumentException(
                  "Subtracting or comparing time marks from different time sources is not possible: ${toString-impl(arg0)} and $other"
               );
            } else {
               return minus-6eNON_k(arg0, (other as TimeSource.Monotonic.ValueTimeMark).unbox-impl());
            }
         }

         override fun `minus-UwyO8pc`(other: ComparableTimeMark): Long {
            return minus-UwyO8pc(this.reading, other);
         }

         @JvmStatic
         public operator fun minus(other: kotlin.time.TimeSource.Monotonic.ValueTimeMark): Duration {
            return MonotonicTimeSource.INSTANCE.differenceBetween-fRLX17w(arg0, other);
         }

         @JvmStatic
         public operator fun compareTo(other: kotlin.time.TimeSource.Monotonic.ValueTimeMark): Int {
            return Duration.compareTo-LRDsOJo(minus-6eNON_k(arg0, other), Duration.Companion.getZERO-UwyO8pc());
         }

         @JvmStatic
         fun `toString-impl`(arg0: Long): java.lang.String {
            return "ValueTimeMark(reading=$arg0)";
         }

         public override fun toString(): String {
            return toString-impl(this.reading);
         }

         @JvmStatic
         fun `hashCode-impl`(arg0: Long): Int {
            return java.lang.Long.hashCode(arg0);
         }

         public override fun hashCode(): Int {
            return hashCode-impl(this.reading);
         }

         @JvmStatic
         fun `equals-impl`(arg0: Long, other: Any): Boolean {
            if (other !is TimeSource.Monotonic.ValueTimeMark) {
               return false;
            } else {
               return arg0 == (other as TimeSource.Monotonic.ValueTimeMark).unbox-impl();
            }
         }

         public override operator fun equals(other: Any?): Boolean {
            return equals-impl(this.reading, other);
         }

         @JvmStatic
         fun `compareTo-impl`(arg0: Long, other: ComparableTimeMark): Int {
            return box-impl(arg0).compareTo(other);
         }

         @JvmStatic
         fun `constructor-impl`(reading: Long): Long {
            return reading;
         }

         @JvmStatic
         fun `equals-impl0`(p1: Long, p2: Long): Boolean {
            return p1 == p2;
         }
      }
   }

   @SinceKotlin(version = "1.9")
   @WasExperimental(markerClass = [ExperimentalTime::class])
   public interface WithComparableMarks : TimeSource {
      public abstract fun markNow(): ComparableTimeMark {
      }
   }
}
