package kotlin.io

import java.io.BufferedReader
import java.util.NoSuchElementException

private class LinesSequence(reader: BufferedReader) : Sequence<java.lang.String> {
   private final val reader: BufferedReader

   init {
      this.reader = reader;
   }

   public override operator fun iterator(): Iterator<String> {
      return new java.util.Iterator<java.lang.String>(this) {
         private java.lang.String nextValue;
         private boolean done;

         {
            this.this$0 = `$receiver`;
         }

         @Override
         public boolean hasNext() {
            if (this.nextValue == null && !this.done) {
               this.nextValue = LinesSequence.access$getReader$p(this.this$0).readLine();
               if (this.nextValue == null) {
                  this.done = true;
               }
            }

            return this.nextValue != null;
         }

         public java.lang.String next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               val answer: java.lang.String = this.nextValue;
               this.nextValue = null;
               return answer;
            }
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
         }
      };
   }
}
