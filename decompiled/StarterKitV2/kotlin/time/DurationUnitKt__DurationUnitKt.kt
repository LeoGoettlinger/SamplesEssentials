package kotlin.time

internal class DurationUnitKt__DurationUnitKt : DurationUnitKt__DurationUnitJvmKt {
   private final val millisMultiplier: Long
      private final get() {
         var var10000: Long;
         switch (DurationUnitKt__DurationUnitKt.WhenMappings.$EnumSwitchMapping$0[$this$millisMultiplier.ordinal()]) {
            case 1:
               var10000 = 86400000L;
               break;
            case 2:
               var10000 = 3600000L;
               break;
            case 3:
               var10000 = 60000L;
               break;
            case 4:
               var10000 = 1000L;
               break;
            case 5:
               var10000 = 1L;
               break;
            default:
               throw new IllegalStateException(("Wrong unit for millisMultiplier: $`$this$millisMultiplier`").toString());
         }

         return var10000;
      }


   @JvmStatic
   internal fun convertDurationUnitToMilliseconds(value: Long, unit: DurationUnit): Long {
      return multiplyNonNegativeWithoutOverflow$DurationUnitKt__DurationUnitKt(value, getMillisMultiplier$DurationUnitKt__DurationUnitKt(unit));
   }

   @JvmStatic
   private fun Long.multiplyNonNegativeWithoutOverflow(other: Long): Long {
      val var10000: Long;
      if (`$this$multiplyNonNegativeWithoutOverflow` == 0L) {
         var10000 = 0L;
      } else if (`$this$multiplyNonNegativeWithoutOverflow` == 1L) {
         var10000 = RangesKt.coerceAtMost(other, 4611686018427387903L);
      } else if (other == 1L) {
         var10000 = RangesKt.coerceAtMost(`$this$multiplyNonNegativeWithoutOverflow`, 4611686018427387903L);
      } else {
         val bitSum: Int = 128 - java.lang.Long.numberOfLeadingZeros(`$this$multiplyNonNegativeWithoutOverflow`) - java.lang.Long.numberOfLeadingZeros(other);
         var10000 = if (bitSum < 63)
            `$this$multiplyNonNegativeWithoutOverflow` * other
            else
            (if (bitSum > 63) 4611686018427387903L else RangesKt.coerceAtMost(`$this$multiplyNonNegativeWithoutOverflow` * other, 4611686018427387903L));
      }

      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @JvmStatic
   internal fun DurationUnit.shortName(): String {
      var var10000: java.lang.String;
      switch (DurationUnitKt__DurationUnitKt.WhenMappings.$EnumSwitchMapping$0[$this$shortName.ordinal()]) {
         case 1:
            var10000 = "d";
            break;
         case 2:
            var10000 = "h";
            break;
         case 3:
            var10000 = "m";
            break;
         case 4:
            var10000 = "s";
            break;
         case 5:
            var10000 = "ms";
            break;
         case 6:
            var10000 = "ns";
            break;
         case 7:
            var10000 = "us";
            break;
         default:
            throw new IllegalStateException(("Unknown unit: $`$this$shortName`").toString());
      }

      return var10000;
   }

   open fun DurationUnitKt__DurationUnitKt() {
   }
}
