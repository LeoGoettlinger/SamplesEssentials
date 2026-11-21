package org.enginehub.linbus.tree;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

public final class LinByteArrayTag extends LinTag<byte[]> {
   private final byte[] value;

   public static LinByteArrayTag of(byte... value) {
      return new LinByteArrayTag((byte[])value.clone());
   }

   private LinByteArrayTag(byte[] value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinByteArrayTag> type() {
      return LinTagType.byteArrayTag();
   }

   public byte[] value() {
      return (byte[])this.value.clone();
   }

   public ByteBuffer view() {
      return ByteBuffer.wrap(this.value).asReadOnlyBuffer();
   }

   @Override
   public LinStream linStream() {
      return new SurroundingLinStream(new LinToken.ByteArrayStart(this.value.length), new LinStream() {
         private static final int BUFFER_SIZE = 4096;
         private int i = 0;

         @Nullable
         @Override
         public LinToken nextOrNull() {
            if (this.i >= LinByteArrayTag.this.value.length) {
               return null;
            } else {
               int length = Math.min(4096, LinByteArrayTag.this.value.length - this.i);
               ByteBuffer buffer = ByteBuffer.wrap(LinByteArrayTag.this.value, this.i, length).asReadOnlyBuffer();
               this.i += length;
               return new LinToken.ByteArrayContent(buffer);
            }
         }
      }, new LinToken.ByteArrayEnd());
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LinByteArrayTag that = (LinByteArrayTag)o;
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
