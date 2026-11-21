package kotlin.time

import kotlin.internal.InlineOnly
import kotlin.math.MathKt

private final val fractionMultiplier: Double
   private final get() {
      var var10000: Double;
      switch (DurationKt.WhenMappings.$EnumSwitchMapping$0[$this$fractionMultiplier.ordinal()]) {
         case 1:
            var10000 = 1.0E-12;
            break;
         case 2:
            var10000 = 1.0E-15;
            break;
         case 3:
            var10000 = 1.0E-9;
            break;
         case 4:
            var10000 = 1.0E-6;
            break;
         case 5:
            var10000 = 6.0E-5;
            break;
         case 6:
            var10000 = 0.0036;
            break;
         case 7:
            var10000 = 0.0864;
            break;
         default:
            throw new IllegalStateException(("Unknown unit: $`$this$fractionMultiplier`").toString());
      }

      return var10000;
   }


private final val fallbackFractionMultiplier: Long
   private final get() {
      var var10000: Long;
      switch (DurationKt.WhenMappings.$EnumSwitchMapping$0[$this$fallbackFractionMultiplier.ordinal()]) {
         case 5:
            var10000 = 60000000000L;
            break;
         case 6:
            var10000 = 3600000000000L;
            break;
         case 7:
            var10000 = 86400000000000L;
            break;
         default:
            throw new IllegalStateException(("Invalid unit: $`$this$fallbackFractionMultiplier` for fallback fraction multiplier").toString());
      }

      return var10000;
   }


private final val shortNameLength: Int
   private final get() {
      var var10000: Byte;
      switch (DurationKt.WhenMappings.$EnumSwitchMapping$0[$this$shortNameLength.ordinal()]) {
         case 1:
         case 2:
         case 3:
            var10000 = 2;
            break;
         default:
            var10000 = 1;
      }

      return var10000;
   }


internal const val NANOS_IN_MILLIS: Int = 1000000
internal const val MICROS_IN_MILLIS: Long = 1000L
internal const val NANOS_IN_MICROS: Long = 1000L
internal const val MAX_NANOS: Long = 4611686018426999999L
internal const val MAX_MILLIS: Long = 4611686018427387903L
private const val MAX_NANOS_IN_MILLIS: Long = 4611686018426L
internal const val MILLIS_IN_SECOND: Long = 1000L
internal const val MILLIS_IN_MINUTE: Long = 60000L
internal const val MILLIS_IN_HOUR: Long = 3600000L
internal const val MILLIS_IN_DAY: Long = 86400000L
private const val INFINITY_STRING: String = "Infinity"
private const val FRACTION_LIMIT: Int = 15

@SinceKotlin(version = "1.6")
public fun Int.toDuration(unit: DurationUnit): Duration {
   return if (unit.compareTo(DurationUnit.SECONDS) <= 0)
      durationOfNanos(DurationUnitKt.convertDurationUnitOverflow((long)`$this$toDuration`, unit, DurationUnit.NANOSECONDS))
      else
      toDuration((long)`$this$toDuration`, unit);
}

@SinceKotlin(version = "1.6")
public fun Long.toDuration(unit: DurationUnit): Duration {
   val maxNsInUnit: Long = DurationUnitKt.convertDurationUnitOverflow(4611686018426999999L, DurationUnit.NANOSECONDS, unit);
   return if (-maxNsInUnit <= `$this$toDuration` && `$this$toDuration` <= maxNsInUnit)
      durationOfNanos(DurationUnitKt.convertDurationUnitOverflow(`$this$toDuration`, unit, DurationUnit.NANOSECONDS))
      else
      (
         if (unit.compareTo(DurationUnit.MILLISECONDS) >= 0)
            durationOfMillis(
               (long)MathKt.getSign(`$this$toDuration`)
                  * DurationUnitKt.convertDurationUnitToMilliseconds(Math.abs(RangesKt.coerceAtLeast(`$this$toDuration`, -9223372036854775807L)), unit)
            )
            else
            durationOfMillis(
               RangesKt.coerceIn(
                  DurationUnitKt.convertDurationUnit(`$this$toDuration`, unit, DurationUnit.MILLISECONDS), -4611686018427387903L, 4611686018427387903L
               )
            )
      );
}

@SinceKotlin(version = "1.6")
public fun Double.toDuration(unit: DurationUnit): Duration {
   val valueInNs: Double = DurationUnitKt.convertDurationUnit(`$this$toDuration`, unit, DurationUnit.NANOSECONDS);
   if (java.lang.Double.isNaN(valueInNs)) {
      throw new IllegalArgumentException("Duration value cannot be NaN.".toString());
   } else {
      val nanos: Long = MathKt.roundToLong(valueInNs);
      return if (-4611686018426999999L <= nanos && nanos < 4611686018427000000L)
         durationOfNanos(nanos)
         else
         durationOfMillisNormalized(MathKt.roundToLong(DurationUnitKt.convertDurationUnit(`$this$toDuration`, unit, DurationUnit.MILLISECONDS)));
   }
}

@SinceKotlin(version = "1.6")
@InlineOnly
public inline operator fun Int.times(duration: Duration): Duration {
   return Duration.times-UwyO8pc(duration, `$this$times_u2dmvk6XK0`);
}

@SinceKotlin(version = "1.6")
@InlineOnly
public inline operator fun Double.times(duration: Duration): Duration {
   return Duration.times-UwyO8pc(duration, `$this$times_u2dkIfJnKk`);
}

private fun parseDuration(value: String, strictIso: Boolean, throwException: Boolean = ...): Duration {
   if (value.length() == 0) {
      if (throwException) {
         throw new IllegalArgumentException("The string is empty");
      } else {
         return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
      }
   } else {
      val firstChar: Char = value.charAt(0);
      var isNegative: Boolean = false;
      switch (firstChar) {
         case '+':
            0++;
         case ',':
         default:
            break;
         case '-':
            isNegative = true;
            0++;
      }

      val hasSign: Boolean = 0 > 0;
      if (value.length() <= 0) {
         if (throwException) {
            throw new IllegalArgumentException("No components");
         } else {
            return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
         }
      } else {
         val var10000: Long;
         if (value.charAt(0) == 'P') {
            var10000 = parseIsoStringFormat(value, 0 + 1, throwException);
         } else {
            if (strictIso) {
               if (throwException) {
                  throw new IllegalArgumentException("");
               }

               return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
            }

            var10000 = if (StringsKt.regionMatches(value, 0, "Infinity", 0, Math.max(value.length() - 0, 8), true))
               Duration.Companion.getINFINITE-UwyO8pc()
               else
               parseDefaultStringFormat(value, 0, hasSign, throwException);
         }

         return if (isNegative && !Duration.equals-impl0(var10000, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib()))
            Duration.unaryMinus-UwyO8pc(var10000)
            else
            var10000;
      }
   }
}

@JvmSynthetic
fun `parseDuration$default`(var0: java.lang.String, var1: Boolean, var2: Boolean, var3: Int, var4: Any): Long {
   if ((var3 and 4) != 0) {
      var2 = true;
   }

   return parseDuration(var0, var1, var2);
}

private fun parseIsoStringFormat(value: String, startIndex: Int, throwException: Boolean): Duration {
   var var45: Int = startIndex;
   if (startIndex == value.length()) {
      if (throwException) {
         throw new IllegalArgumentException("");
      } else {
         return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
      }
   } else {
      var totalMillis: Long = 0L;
      var totalNanos: Long = 0L;
      var isTimeComponent: Boolean = false;
      var prevUnit: DurationUnit = null;

      while (var45 < value.length()) {
         val ch: Char = value.charAt(var45);
         if (ch == 'T') {
            if (!isTimeComponent) {
               if (++var45 != value.length()) {
                  isTimeComponent = true;
                  continue;
               }
            }

            if (throwException) {
               throw new IllegalArgumentException("");
            }

            return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
         } else {
            val longStartIndex: Int = var45;
            val unit: LongParser = LongParser.Companion.getIso();
            val `value$iv`: java.lang.String = value;
            var var20: Int = 1;
            var `$i$f$parse`: Int = var45;
            if (LongParser.access$getAllowSign$p(unit)) {
               switch (value.charAt(var45)) {
                  case '+':
                     `$i$f$parse` = var45 + 1;
                  case ',':
                  default:
                     break;
                  case '-':
                     var20 = -1;
                     `$i$f$parse` = var45 + 1;
               }
            }

            val var61: java.lang.String = value;

            var `startIndex$iv$iv`: Int;
            for (i$iv$iv = index$iv; i$iv$iv < var61.length(); i$iv$iv++) {
               if (var61.charAt(`startIndex$iv$iv`) != '0') {
                  break;
               }
            }

            `$i$f$parse` = `startIndex$iv$iv`;
            var `it$iv`: Long = 0L;

            while (true) {
               label299: {
                  label330: {
                     var var49: Int;
                     var var10000: Long;
                     label301: {
                        if (`$i$f$parse` < `value$iv`.length()) {
                           val var63: Char = `value$iv`.charAt(`$i$f$parse`);
                           if ('0' <= var63 && var63 < ':') {
                              `startIndex$iv$iv` = var63 - '0';
                              if (`it$iv` <= LongParser.access$getOverflowThreshold$p(unit)
                                 && (`it$iv` != LongParser.access$getOverflowThreshold$p(unit) || var63 - '0' <= LongParser.access$getLastDigitMax$p(unit))) {
                                 `it$iv` = (`it$iv` shl 3) + (`it$iv` shl 1) + `startIndex$iv$iv`;
                                 `$i$f$parse`++;
                                 continue;
                              }

                              val var68: java.lang.String = `value$iv`;

                              var `i$iv$ivx`: Int;
                              for (i$iv$ivx = index$iv; i$iv$ivx < var68.length(); i$iv$ivx++) {
                                 val `ch$iv$iv`: Char = var68.charAt(`i$iv$ivx`);
                                 if ('0' > `ch$iv$iv` || `ch$iv$iv` >= ':') {
                                    break;
                                 }
                              }

                              var45 = `i$iv$ivx`;
                              if (`i$iv$ivx` == value.length()) {
                                 break label299;
                              }

                              var var94: Byte;
                              switch (ch) {
                                 case '+':
                                 case '-':
                                    var94 = 1;
                                    break;
                                 case ',':
                                 default:
                                    var94 = 0;
                              }

                              if (`i$iv$ivx` == longStartIndex + var94) {
                                 break label299;
                              }

                              var49 = var20;
                              var10000 = LongParser.access$getOverflowLimit$p(unit);
                              break label301;
                           }
                        }

                        var45 = `$i$f$parse`;
                        if (`$i$f$parse` == value.length()) {
                           break label330;
                        }

                        var var10002: Byte;
                        switch (ch) {
                           case '+':
                           case '-':
                              var10002 = 1;
                              break;
                           case ',':
                           default:
                              var10002 = 0;
                        }

                        if (`$i$f$parse` == longStartIndex + var10002) {
                           break label330;
                        }

                        var49 = var20;
                        var10000 = `it$iv`;
                     }

                     if (value.charAt(var45) == '.') {
                        var45++;
                        val it: FractionalParser = FractionalParser.INSTANCE;
                        var var64: java.lang.String = value;
                        var var75: Int = var45;
                        var var29: Int = Math.min(var45 + 6, value.length());

                        var var80: Int;
                        for (result$iv$iv = 0; index$iv$iv < endIndex$iv$iv; index$iv$iv++) {
                           val var82: Char = var64.charAt(var75);
                           if ('0' > var82 || var82 >= ':') {
                              break;
                           }

                           var80 = (var80 shl 3) + (var80 shl 1) + (var82 - '0');
                        }

                        var `result$iv$ivx`: Int = 6 - (var75 - var45);

                        for (int var85 = 0; var85 < result$iv$ivx; var85++) {
                           var80 = (var80 shl 3) + (var80 shl 1);
                        }

                        val var67: java.lang.String = value;
                        var29 = var75;
                        var80 = Math.min(var75 + 9, value.length());

                        for (result$iv$ivx = 0; endIndex$iv$iv < result$iv$iv; endIndex$iv$iv++) {
                           val var86: Char = var67.charAt(var29);
                           if ('0' > var86 || var86 >= ':') {
                              break;
                           }

                           `result$iv$ivx` = (`result$iv$ivx` shl 3) + (`result$iv$ivx` shl 1) + (var86 - '0');
                        }

                        val var87: Int = 9 - (var29 - var75);

                        for (int var88 = 0; var88 < var87; var88++) {
                           `result$iv$ivx` = (`result$iv$ivx` shl 3) + (`result$iv$ivx` shl 1);
                        }

                        var64 = value;
                        var `i$iv$ivxx`: Int = var29;

                        while (true) {
                           if (`i$iv$ivxx` < var64.length()) {
                              val var77: Char = var64.charAt(`i$iv$ivxx`);
                              if ('0' <= var77 && var77 < ':') {
                                 `i$iv$ivxx`++;
                                 continue;
                              }
                           }

                           if (`i$iv$ivxx` == var45 || `i$iv$ivxx` == value.length() || value.charAt(`i$iv$ivxx`) != 'S') {
                              if (throwException) {
                                 throw new IllegalArgumentException("");
                              }

                              return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                           }

                           var45 = `i$iv$ivxx`;
                           totalNanos = var49 * fractionDigitsToNanos((long)var80 * (long)1000000000 + (long)`result$iv$ivx`, DurationUnit.SECONDS);
                           break;
                        }
                     }

                     val var93: DurationUnit = isoDurationUnitByShortNameOrNull(value, var45);
                     if (var93 == null) {
                        val var56: java.lang.String = "Unknown duration unit short name: ${value.charAt(var45)}";
                        if (throwException) {
                           throw new IllegalArgumentException(var56);
                        }

                        return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                     }

                     if (prevUnit != null && prevUnit.compareTo(var93) <= 0) {
                        if (throwException) {
                           throw new IllegalArgumentException("Unexpected order of duration components");
                        }

                        return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                     }

                     prevUnit = var93;
                     if (var93 === DurationUnit.DAYS) {
                        if (isTimeComponent) {
                           if (throwException) {
                              throw new IllegalArgumentException("");
                           }

                           return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                        }

                        totalMillis = var49 * DurationUnitKt.convertDurationUnitToMilliseconds(var10000, var93);
                     } else {
                        if (!isTimeComponent) {
                           if (throwException) {
                              throw new IllegalArgumentException("");
                           }

                           return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                        }

                        val var53: Long = addMillisWithoutOverflow(totalMillis, (long)var49 * DurationUnitKt.convertDurationUnitToMilliseconds(var10000, var93));
                        if (var53 == 9223372036854759646L) {
                           if (throwException) {
                              throw new IllegalArgumentException("");
                           }

                           return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                        }

                        totalMillis = var53;
                     }

                     var45++;
                     break;
                  }

                  if (throwException) {
                     throw new IllegalArgumentException("");
                  }

                  return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
               }

               if (throwException) {
                  throw new IllegalArgumentException("");
               }

               return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
            }
         }
      }

      return Duration.plus-LRDsOJo(toDuration(totalMillis, DurationUnit.MILLISECONDS), toDuration(totalNanos, DurationUnit.NANOSECONDS));
   }
}

private fun parseDefaultStringFormat(value: String, startIndex: Int, hasSign: Boolean, throwException: Boolean): Duration {
   var var45: Int = startIndex;
   var var48: Int = value.length();
   var allowSpaces: Boolean = !hasSign;
   if (hasSign && value.charAt(startIndex) == '(' && value.charAt(var48 - 1) == ')') {
      allowSpaces = true;
      var45 = startIndex + 1;
      var48 += -1;
      if (var45 == var48) {
         if (throwException) {
            throw new IllegalArgumentException("No components");
         }

         return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
      }
   }

   var totalMillis: Long = 0L;
   var totalNanos: Long = 0L;
   var prevUnit: DurationUnit = null;
   var isFirstComponent: Boolean = true;

   while (var45 < var48) {
      if (!isFirstComponent && allowSpaces) {
         val longStartIndex: java.lang.String = value;

         var hasFractionalPart: Int;
         for (i$iv = var45; i$iv < longStartIndex.length(); i$iv++) {
            if (longStartIndex.charAt(hasFractionalPart) != ' ') {
               break;
            }
         }

         var45 = hasFractionalPart;
      }

      isFirstComponent = false;
      val var50: LongParser = LongParser.Companion.getDefault();
      val var52: java.lang.String = value;
      var `value$iv`: Int = var45;
      if (LongParser.access$getAllowSign$p(var50)) {
         switch (value.charAt(var45)) {
            case '+':
               `value$iv` = var45 + 1;
            case ',':
            default:
               break;
            case '-':
               `value$iv` = var45 + 1;
         }
      }

      var var63: java.lang.String = value;

      var `lowPrecisionDigits$iv`: Int;
      for (i$iv$iv = index$iv; i$iv$iv < var63.length(); i$iv$iv++) {
         if (var63.charAt(`lowPrecisionDigits$iv`) != '0') {
            break;
         }
      }

      `value$iv` = `lowPrecisionDigits$iv`;
      var `$i$f$skipWhile`: Long = 0L;

      while (true) {
         var var10000: Long;
         label323: {
            if (`value$iv` < var52.length()) {
               val var65: Char = var52.charAt(`value$iv`);
               if ('0' <= var65 && var65 < ':') {
                  `lowPrecisionDigits$iv` = var65 - '0';
                  if (`$i$f$skipWhile` <= LongParser.access$getOverflowThreshold$p(var50)
                     && (`$i$f$skipWhile` != LongParser.access$getOverflowThreshold$p(var50) || var65 - '0' <= LongParser.access$getLastDigitMax$p(var50))) {
                     `$i$f$skipWhile` = (`$i$f$skipWhile` shl 3) + (`$i$f$skipWhile` shl 1) + `lowPrecisionDigits$iv`;
                     `value$iv`++;
                     continue;
                  }

                  val var68: java.lang.String = var52;

                  var `i$iv$ivx`: Int;
                  for (i$iv$ivx = index$iv; i$iv$ivx < var68.length(); i$iv$ivx++) {
                     val `endIndex$iv$iv`: Char = var68.charAt(`i$iv$ivx`);
                     if ('0' > `endIndex$iv$iv` || `endIndex$iv$iv` >= ':') {
                        break;
                     }
                  }

                  if (`i$iv$ivx` == var45 || `i$iv$ivx` == var48 || true) {
                     if (throwException) {
                        throw new IllegalArgumentException("");
                     }

                     return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
                  }

                  var45 = `i$iv$ivx`;
                  var10000 = LongParser.access$getOverflowLimit$p(var50);
                  break label323;
               }
            }

            if (`value$iv` == var45 || `value$iv` == var48 || false) {
               if (throwException) {
                  throw new IllegalArgumentException("");
               }

               return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
            }

            var45 = `value$iv`;
            var10000 = `$i$f$skipWhile`;
         }

         val var51: Boolean = value.charAt(var45) == '.';
         val var54: Int;
         val var56: Long;
         if (!var51) {
            var54 = -1;
            var56 = 0L;
         } else {
            var54 = var45++;
            val var58: FractionalParser = FractionalParser.INSTANCE;
            var var69: java.lang.String = value;
            var var77: Int = var45;
            var var80: Int = Math.min(var45 + 6, value.length());

            var var83: Int;
            for (result$iv$iv = 0; index$iv$iv < endIndex$iv$iv; index$iv$iv++) {
               val var85: Char = var69.charAt(var77);
               if ('0' > var85 || var85 >= ':') {
                  break;
               }

               var83 = (var83 shl 3) + (var83 shl 1) + (var85 - '0');
            }

            var `result$iv$ivx`: Int = 6 - (var77 - var45);

            for (int var89 = 0; var89 < result$iv$ivx; var89++) {
               var83 = (var83 shl 3) + (var83 shl 1);
            }

            val var71: java.lang.String = value;
            var80 = var77;
            var83 = Math.min(var77 + 9, value.length());

            for (result$iv$ivx = 0; endIndex$iv$iv < result$iv$iv; endIndex$iv$iv++) {
               val var90: Char = var71.charAt(var80);
               if ('0' > var90 || var90 >= ':') {
                  break;
               }

               `result$iv$ivx` = (`result$iv$ivx` shl 3) + (`result$iv$ivx` shl 1) + (var90 - '0');
            }

            val var91: Int = 9 - (var80 - var77);

            for (int it$iv$iv = 0; it$iv$iv < var91; it$iv$iv++) {
               `result$iv$ivx` = (`result$iv$ivx` shl 3) + (`result$iv$ivx` shl 1);
            }

            var69 = value;
            var `i$iv$ivxx`: Int = var80;

            while (true) {
               if (`i$iv$ivxx` < var69.length()) {
                  val var79: Char = var69.charAt(`i$iv$ivxx`);
                  if ('0' <= var79 && var79 < ':') {
                     `i$iv$ivxx`++;
                     continue;
                  }
               }

               if (`i$iv$ivxx` == var45 || `i$iv$ivxx` == var48) {
                  if (throwException) {
                     throw new IllegalArgumentException("");
                  }

                  return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
               }

               var45 = `i$iv$ivxx`;
               var56 = (long)var83 * 1000000000 + `result$iv$ivx`;
               break;
            }
         }

         val var95: DurationUnit = defaultDurationUnitByShortNameOrNull(value, var45);
         if (var95 == null) {
            var63 = "Unknown duration unit short name: ${value.charAt(var45)}";
            if (throwException) {
               throw new IllegalArgumentException(var63);
            }

            return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
         }

         if (prevUnit != null && prevUnit.compareTo(var95) <= 0) {
            if (throwException) {
               throw new IllegalArgumentException("Unexpected order of duration components");
            }

            return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
         }

         prevUnit = var95;
         switch (DurationKt.WhenMappings.$EnumSwitchMapping$0[var95.ordinal()]) {
            case 1:
               totalMillis += var10000 / 1000L;
               if (totalMillis <= 4611686018426L) {
                  totalNanos = var10000 % 1000L * 1000L;
               }
               break;
            case 2:
               totalMillis += var10000 / 1000000;
               totalNanos += var10000 % 1000000;
               break;
            default:
               totalMillis = addMillisWithoutOverflow(totalMillis, DurationUnitKt.convertDurationUnitToMilliseconds(var10000, var95));
         }

         var45 = var45 + getShortNameLength(var95);
         if (!var51) {
            break;
         }

         if (var45 < var48) {
            if (throwException) {
               throw new IllegalArgumentException("Fractional component must be last");
            }

            return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
         }

         totalNanos += if (var95.compareTo(DurationUnit.MINUTES) >= 0 && var45 - var54 > 15)
            parseFractionFallback(value, var54, var45 - getShortNameLength(var95), var95)
            else
            fractionDigitsToNanos(var56, var95);
         break;
      }
   }

   return Duration.plus-LRDsOJo(toDuration(totalMillis, DurationUnit.MILLISECONDS), toDuration(totalNanos, DurationUnit.NANOSECONDS));
}

private fun Long.addMillisWithoutOverflow(other: Long): Long {
   return if (`$this$addMillisWithoutOverflow` == 4611686018427387903L || `$this$addMillisWithoutOverflow` == -4611686018427387903L)
      (
         if ((-4611686018427387903L >= other || other >= 4611686018427387903L) && (`$this$addMillisWithoutOverflow` xor other) < 0L)
            9223372036854759646L
            else
            `$this$addMillisWithoutOverflow`
      )
      else
      (
         if (other == 4611686018427387903L || other == -4611686018427387903L)
            other
            else
            RangesKt.coerceIn(`$this$addMillisWithoutOverflow` + other, -4611686018427387903L, 4611686018427387903L)
      );
}

@InlineOnly
private inline fun Long.isInfiniteMillis(): Boolean {
   return `$this$isInfiniteMillis` == 4611686018427387903L || `$this$isInfiniteMillis` == -4611686018427387903L;
}

@InlineOnly
private inline fun Long.isFiniteMillis(): Boolean {
   return -4611686018427387903L < `$this$isFiniteMillis` && `$this$isFiniteMillis` < 4611686018427387903L;
}

@InlineOnly
private inline fun sameSign(a: Long, b: Long): Boolean {
   return (a xor b) >= 0L;
}

private fun String.parseFractionFallback(startIndex: Int, endIndex: Int, unit: DurationUnit): Long {
   val var10000: java.lang.String = `$this$parseFractionFallback`.substring(startIndex, endIndex);
   return MathKt.roundToLong(java.lang.Double.parseDouble(var10000) * (double)getFallbackFractionMultiplier(unit));
}

private fun Long.fractionDigitsToNanos(unit: DurationUnit): Long {
   return MathKt.roundToLong((double)`$this$fractionDigitsToNanos` * getFractionMultiplier(unit));
}

@InlineOnly
private inline fun handleError(throwException: Boolean, message: String = ...): Duration {
   if (throwException) {
      throw new IllegalArgumentException(message);
   } else {
      return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
   }
}

@JvmSynthetic
fun `handleError$default`(throwException: Boolean, message: java.lang.String, var2: Int, var3: Any): Long {
   if ((var2 and 2) != 0) {
      message = "";
   }

   if (throwException) {
      throw new IllegalArgumentException(message);
   } else {
      return Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib();
   }
}

private inline fun Duration.onInvalid(block: () -> Duration?): Duration? {
   return if (Duration.equals-impl0(`$this$onInvalid_u2dge6A_vg`, Duration.Companion.getINVALID-UwyO8pc$kotlin_stdlib()))
      block.invoke() as Duration
      else
      Duration.box-impl(`$this$onInvalid_u2dge6A_vg`);
}

private fun String.defaultDurationUnitByShortNameOrNull(start: Int): DurationUnit? {
   val first: Char = `$this$defaultDurationUnitByShortNameOrNull`.charAt(start);
   val second: Char = if (start < StringsKt.getLastIndex(`$this$defaultDurationUnitByShortNameOrNull`))
      `$this$defaultDurationUnitByShortNameOrNull`.charAt(start + 1)
      else
      0;
   var var10000: DurationUnit;
   switch (first) {
      case 'd':
         var10000 = DurationUnit.DAYS;
         break;
      case 'h':
         var10000 = DurationUnit.HOURS;
         break;
      case 'm':
         var10000 = if (second == 's') DurationUnit.MILLISECONDS else DurationUnit.MINUTES;
         break;
      case 'n':
         var10000 = if (second == 's') DurationUnit.NANOSECONDS else null;
         break;
      case 's':
         var10000 = DurationUnit.SECONDS;
         break;
      case 'u':
         var10000 = if (second == 's') DurationUnit.MICROSECONDS else null;
         break;
      default:
         var10000 = null;
   }

   return var10000;
}

private fun String.isoDurationUnitByShortNameOrNull(start: Int): DurationUnit? {
   var var10000: DurationUnit;
   switch ($this$isoDurationUnitByShortNameOrNull.charAt(start)) {
      case 'D':
         var10000 = DurationUnit.DAYS;
         break;
      case 'H':
         var10000 = DurationUnit.HOURS;
         break;
      case 'M':
         var10000 = DurationUnit.MINUTES;
         break;
      case 'S':
         var10000 = DurationUnit.SECONDS;
         break;
      default:
         var10000 = null;
   }

   return var10000;
}

@InlineOnly
private inline fun Long.multiplyBy10(): Long {
   return (`$this$multiplyBy10` shl 3) + (`$this$multiplyBy10` shl 1);
}

@InlineOnly
private inline fun Int.multiplyBy10(): Int {
   return (`$this$multiplyBy10` shl 3) + (`$this$multiplyBy10` shl 1);
}

private fun nanosToMillis(nanos: Long): Long {
   return nanos / 1000000;
}

private fun millisToNanos(millis: Long): Long {
   return millis * 1000000;
}

private fun durationOfNanos(normalNanos: Long): Duration {
   return Duration.Companion.fromRawValue-UwyO8pc$kotlin_stdlib(normalNanos shl 1);
}

private fun durationOfMillis(normalMillis: Long): Duration {
   return Duration.Companion.fromRawValue-UwyO8pc$kotlin_stdlib((normalMillis shl 1) + 1L);
}

private fun durationOf(normalValue: Long, unitDiscriminator: Int): Duration {
   return Duration.Companion.fromRawValue-UwyO8pc$kotlin_stdlib((normalValue shl 1) + (long)unitDiscriminator);
}

private fun durationOfNanosNormalized(nanos: Long): Duration {
   return if (-4611686018426999999L <= nanos && nanos < 4611686018427000000L) durationOfNanos(nanos) else durationOfMillis(nanosToMillis(nanos));
}

private fun durationOfMillisNormalized(millis: Long): Duration {
   return if (-4611686018426L <= millis && millis < 4611686018427L)
      durationOfNanos(millisToNanos(millis))
      else
      durationOfMillis(RangesKt.coerceIn(millis, -4611686018427387903L, 4611686018427387903L));
}

@JvmSynthetic
fun `access$parseDuration`(value: java.lang.String, strictIso: Boolean, throwException: Boolean): Long {
   return parseDuration(value, strictIso, throwException);
}

@JvmSynthetic
fun `access$durationOf`(normalValue: Long, unitDiscriminator: Int): Long {
   return durationOf(normalValue, unitDiscriminator);
}

@JvmSynthetic
fun `access$durationOfNanosNormalized`(nanos: Long): Long {
   return durationOfNanosNormalized(nanos);
}

@JvmSynthetic
fun `access$addMillisWithoutOverflow`(`$receiver`: Long, other: Long): Long {
   return addMillisWithoutOverflow(`$receiver`, other);
}

@JvmSynthetic
fun `access$durationOfMillis`(normalMillis: Long): Long {
   return durationOfMillis(normalMillis);
}

@JvmSynthetic
fun `access$durationOfMillisNormalized`(millis: Long): Long {
   return durationOfMillisNormalized(millis);
}

@JvmSynthetic
fun `access$nanosToMillis`(nanos: Long): Long {
   return nanosToMillis(nanos);
}

@JvmSynthetic
fun `access$millisToNanos`(millis: Long): Long {
   return millisToNanos(millis);
}

@JvmSynthetic
fun `access$durationOfNanos`(normalNanos: Long): Long {
   return durationOfNanos(normalNanos);
}
// $VF: Class flags could not be determined
@JvmSynthetic
internal class WhenMappings {
   @JvmStatic
   fun {
      val var0: IntArray = new int[DurationUnit.values().length];

      try {
         var0[DurationUnit.MICROSECONDS.ordinal()] = 1;
      } catch (var8: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.NANOSECONDS.ordinal()] = 2;
      } catch (var7: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.MILLISECONDS.ordinal()] = 3;
      } catch (var6: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.SECONDS.ordinal()] = 4;
      } catch (var5: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.MINUTES.ordinal()] = 5;
      } catch (var4: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.HOURS.ordinal()] = 6;
      } catch (var3: NoSuchFieldError) {
      }

      try {
         var0[DurationUnit.DAYS.ordinal()] = 7;
      } catch (var2: NoSuchFieldError) {
      }

      $EnumSwitchMapping$0 = var0;
   }
}
