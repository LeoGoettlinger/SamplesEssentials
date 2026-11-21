package kotlin.time

import kotlin.contracts.InvocationKind
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nDuration.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Duration.kt\nkotlin/time/LongParser\n+ 2 Strings.kt\nkotlin/text/StringsKt__StringsKt\n*L\n1#1,1613:1\n1656#2,3:1614\n1656#2,3:1617\n*S KotlinDebug\n*F\n+ 1 Duration.kt\nkotlin/time/LongParser\n*L\n1295#1:1614,3\n1302#1:1617,3\n*E\n"])
internal class LongParser private constructor(overflowLimit: Long, allowSign: Boolean) {
   private final val overflowLimit: Long
   private final val allowSign: Boolean
   private final val overflowThreshold: Long
   private final val lastDigitMax: Long

   init {
      this.overflowLimit = overflowLimit;
      this.allowSign = allowSign;
      this.overflowThreshold = this.overflowLimit / 10;
      this.lastDigitMax = this.overflowLimit % 10;
   }

   public inline fun parse(value: String, startIndex: Int, callback: (Int, Int, Boolean) -> Unit): Long {
      contract {
         callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
      }

      var sign: Int = 1;
      var index: Int = startIndex;
      if (access$getAllowSign$p(this)) {
         switch (value.charAt(startIndex)) {
            case '+':
               index = startIndex + 1;
            case ',':
            default:
               break;
            case '-':
               sign = -1;
               index = startIndex + 1;
         }
      }

      val var18: java.lang.String = value;

      var digit: Int;
      for (i$iv = index; i$iv < var18.length(); i$iv++) {
         if (var18.charAt(digit) != '0') {
            break;
         }
      }

      index = digit;

      for (result = 0L; index < value.length(); index++) {
         val var20: Char = value.charAt(index);
         if ('0' <= var20 && var20 < ':') {
            digit = var20 - '0';
            if (var19 > access$getOverflowThreshold$p(this) || var19 == access$getOverflowThreshold$p(this) && var20 - '0' > access$getLastDigitMax$p(this)) {
               val var22: java.lang.String = value;

               var `i$ivx`: Int;
               for (i$ivx = index; i$ivx < var22.length(); i$ivx++) {
                  val it: Char = var22.charAt(`i$ivx`);
                  if ('0' > it || it >= ':') {
                     break;
                  }
               }

               callback.invoke(`i$ivx`, sign, true);
               return access$getOverflowLimit$p(this);
            }

            var19 = (var19 shl 3) + (var19 shl 1) + digit;
            continue;
         }
         break;
      }

      callback.invoke(index, sign, false);
      return var19;
   }

   public companion object {
      public final val iso: LongParser
      public final val default: LongParser
   }
}
