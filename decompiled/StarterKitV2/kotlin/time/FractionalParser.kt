package kotlin.time

import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nDuration.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Duration.kt\nkotlin/time/FractionalParser\n+ 2 Strings.kt\nkotlin/text/StringsKt__StringsKt\n*L\n1#1,1613:1\n1351#1,14:1614\n1351#1,14:1628\n1656#2,3:1642\n*S KotlinDebug\n*F\n+ 1 Duration.kt\nkotlin/time/FractionalParser\n*L\n1343#1:1614,14\n1344#1:1628,14\n1345#1:1642,3\n*E\n"])
internal object FractionalParser {
   public inline fun parse(value: String, startIndex: Int, callback: (Int) -> Unit): Long {
      var `$this$skipWhile$iv`: java.lang.String = value;
      var it: Int = startIndex;
      var var13: Int = Math.min(startIndex + 6, value.length());

      var `endIndex$iv`: Int;
      for (result$iv = 0; index$iv < endIndex$iv; index$iv++) {
         val `result$iv`: Char = `$this$skipWhile$iv`.charAt(it);
         if ('0' > `result$iv` || `result$iv` >= ':') {
            break;
         }

         `endIndex$iv` = (`endIndex$iv` shl 3) + (`endIndex$iv` shl 1) + (`result$iv` - '0');
      }

      var `result$ivx`: Int = 6 - (it - startIndex);

      for (int ch$iv = 0; ch$iv < result$ivx; ch$iv++) {
         `endIndex$iv` = (`endIndex$iv` shl 3) + (`endIndex$iv` shl 1);
      }

      val `startIndex$iv`: java.lang.String = value;
      var13 = it;
      `endIndex$iv` = Math.min(it + 9, value.length());

      for (result$ivx = 0; endIndex$iv < result$iv; endIndex$iv++) {
         val var35: Char = `startIndex$iv`.charAt(var13);
         if ('0' > var35 || var35 >= ':') {
            break;
         }

         `result$ivx` = (`result$ivx` shl 3) + (`result$ivx` shl 1) + (var35 - '0');
      }

      val var36: Int = 9 - (var13 - it);

      for (int it$iv = 0; it$iv < var36; it$iv++) {
         `result$ivx` = (`result$ivx` shl 3) + (`result$ivx` shl 1);
      }

      `$this$skipWhile$iv` = value;

      var var27: Int;
      for (i$iv = endIndex$iv; i$iv < $this$skipWhile$iv.length(); i$iv++) {
         val var29: Char = `$this$skipWhile$iv`.charAt(var27);
         if ('0' > var29 || var29 >= ':') {
            break;
         }
      }

      callback.invoke(var27);
      return (long)`endIndex$iv` * 1000000000 + `result$ivx`;
   }

   private inline fun String.parseDigits(startIndex: Int, maxDigits: Int, callback: (Int) -> Unit): Int {
      var index: Int = startIndex;
      val endIndex: Int = Math.min(startIndex + maxDigits, `$this$parseDigits`.length());

      var result: Int;
      for (result = 0; index < endIndex; index++) {
         val ch: Char = `$this$parseDigits`.charAt(index);
         if ('0' > ch || ch >= ':') {
            break;
         }

         result = (result shl 3) + (result shl 1) + (ch - '0');
      }

      val var14: Int = maxDigits - (index - startIndex);

      for (int var10 = 0; var10 < var14; var10++) {
         result = (result shl 3) + (result shl 1);
      }

      callback.invoke(index);
      return result;
   }
}
