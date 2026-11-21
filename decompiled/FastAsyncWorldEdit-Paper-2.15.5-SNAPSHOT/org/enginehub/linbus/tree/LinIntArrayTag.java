package org.enginehub.linbus.tree;

import java.nio.IntBuffer;
import java.util.Arrays;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public final class LinIntArrayTag extends LinTag<int[]> {
   private final int[] value;

   public static LinIntArrayTag of(int... value) {
      return new LinIntArrayTag((int[])value.clone());
   }

   private LinIntArrayTag(int[] value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinIntArrayTag> type() {
      return LinTagType.intArrayTag();
   }

   public int[] value() {
      return (int[])this.value.clone();
   }

   public IntBuffer view() {
      return IntBuffer.wrap(this.value).asReadOnlyBuffer();
   }

   @Override
   public LinStream linStream() {
      return new SurroundingLinStream(new LinToken.IntArrayStart(this.value.length), new LinStream() {
         private static final int BUFFER_SIZE = 4096;
         private int i = 0;

         @Nullable
         @Override
         public LinToken nextOrNull() {
            if (this.i >= LinIntArrayTag.this.value.length) {
               return null;
            } else {
               int length = Math.min(4096, LinIntArrayTag.this.value.length - this.i);
               IntBuffer buffer = IntBuffer.wrap(LinIntArrayTag.this.value, this.i, length).asReadOnlyBuffer();
               this.i += length;
               return new LinToken.IntArrayContent(buffer);
            }
         }
      }, new LinToken.IntArrayEnd());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LinIntArrayTag that = (LinIntArrayTag)o;
         return Arrays.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.value);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + Arrays.toString(this.value);
   }
}
