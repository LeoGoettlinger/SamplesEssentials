package org.enginehub.linbus.tree;

import java.nio.LongBuffer;
import java.util.Arrays;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public final class LinLongArrayTag extends LinTag<long[]> {
   private final long[] value;

   public static LinLongArrayTag of(long... value) {
      return new LinLongArrayTag((long[])value.clone());
   }

   private LinLongArrayTag(long[] value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinLongArrayTag> type() {
      return LinTagType.longArrayTag();
   }

   public long[] value() {
      return (long[])this.value.clone();
   }

   public LongBuffer view() {
      return LongBuffer.wrap(this.value).asReadOnlyBuffer();
   }

   @Override
   public LinStream linStream() {
      return new SurroundingLinStream(new LinToken.LongArrayStart(this.value.length), new LinStream() {
         private static final int BUFFER_SIZE = 4096;
         private int i = 0;

         @Nullable
         @Override
         public LinToken nextOrNull() {
            if (this.i >= LinLongArrayTag.this.value.length) {
               return null;
            } else {
               int length = Math.min(4096, LinLongArrayTag.this.value.length - this.i);
               LongBuffer buffer = LongBuffer.wrap(LinLongArrayTag.this.value, this.i, length).asReadOnlyBuffer();
               this.i += length;
               return new LinToken.LongArrayContent(buffer);
            }
         }
      }, new LinToken.LongArrayEnd());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LinLongArrayTag that = (LinLongArrayTag)o;
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
