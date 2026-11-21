package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinByteTag extends LinNumberTag<Byte> {
   private final byte value;

   public static LinByteTag fromInt(int value) {
      return of((byte)value);
   }

   public static LinByteTag of(byte value) {
      return new LinByteTag(value);
   }

   private LinByteTag(byte value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinByteTag> type() {
      return LinTagType.byteTag();
   }

   public Byte value() {
      return this.value;
   }

   public byte valueAsByte() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.Byte(this.value));
   }
}
