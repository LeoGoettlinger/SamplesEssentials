package kotlin.text

import kotlin.jvm.functions.Function1
import kotlin.jvm.internal.SourceDebugExtension

// $VF: Class flags could not be determined
@SourceDebugExtension(["SMAP\nRegex.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Regex.kt\nkotlin/text/RegexKt$fromInt$1$1\n*L\n1#1,420:1\n*E\n"])
internal class `Regex$special$$inlined$fromInt$1` : Function1<RegexOption, java.lang.Boolean> {
   fun `Regex$special$$inlined$fromInt$1`(`$value`: Int) {
      this.$value = `$value`;
   }

   fun invoke(it: RegexOption): java.lang.Boolean {
      return (this.$value and (it as FlagEnum).getMask()) == (it as FlagEnum).getValue();
   }
}
