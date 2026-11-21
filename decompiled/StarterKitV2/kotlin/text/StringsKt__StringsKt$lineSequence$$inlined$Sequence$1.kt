package kotlin.text

import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nSequences.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Sequences.kt\nkotlin/sequences/SequencesKt__SequencesKt$Sequence$1\n+ 2 Strings.kt\nkotlin/text/StringsKt__StringsKt\n*L\n1#1,730:1\n1550#2:731\n*E\n"])
public class `StringsKt__StringsKt$lineSequence$$inlined$Sequence$1` : Sequence<java.lang.String> {
   fun `StringsKt__StringsKt$lineSequence$$inlined$Sequence$1`(var1: java.lang.CharSequence) {
      this.$this_lineSequence$inlined = var1;
   }

   public override operator fun iterator(): Iterator<Any> {
      return new LinesIterator(this.$this_lineSequence$inlined);
   }
}
