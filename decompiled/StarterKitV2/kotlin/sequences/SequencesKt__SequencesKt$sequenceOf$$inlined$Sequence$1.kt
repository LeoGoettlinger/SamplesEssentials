package kotlin.sequences

import java.util.NoSuchElementException
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nSequences.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Sequences.kt\nkotlin/sequences/SequencesKt__SequencesKt$Sequence$1\n+ 2 Sequences.kt\nkotlin/sequences/SequencesKt__SequencesKt\n*L\n1#1,730:1\n49#2,11:731\n*E\n"])
public class `SequencesKt__SequencesKt$sequenceOf$$inlined$Sequence$1` : Sequence<T> {
   fun `SequencesKt__SequencesKt$sequenceOf$$inlined$Sequence$1`(var1: Any) {
      this.$element$inlined = var1;
   }

   public override operator fun iterator(): Iterator<Any> {
      return new java.util.Iterator<T>(this.$element$inlined) {
         private boolean _hasNext;

         {
            this.$element = (T)`$element`;
            this._hasNext = true;
         }

         @Override
         public T next() {
            if (!this._hasNext) {
               throw new NoSuchElementException();
            } else {
               this._hasNext = false;
               return this.$element;
            }
         }

         @Override
         public boolean hasNext() {
            return this._hasNext;
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
         }
      };
   }
}
