package com.viaversion.viaversion.api.type.types.misc;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class LengthPrefixedTagType extends Type<Tag> {
   private final int maxLength;

   public LengthPrefixedTagType(int maxLength) {
      super(Tag.class);
      this.maxLength = maxLength;
   }

   public Tag read(ByteBuf buffer) {
      int length = Types.VAR_INT.readPrimitive(buffer);
      Preconditions.checkArgument(length <= this.maxLength, "Cannot receive tag longer than %s bytes (got %s bytes)", new Object[]{this.maxLength, length});
      return Types.TAG.read(buffer.readSlice(length));
   }

   public void write(ByteBuf buffer, Tag tag) {
      ByteBuf tempBuf = buffer.alloc().buffer();

      try {
         Types.TAG.write(tempBuf, tag);
         Preconditions.checkArgument(
            tempBuf.readableBytes() <= this.maxLength,
            "Cannot send tag longer than %s bytes (got %s bytes)",
            new Object[]{this.maxLength, tempBuf.readableBytes()}
         );
         Types.VAR_INT.writePrimitive(buffer, tempBuf.readableBytes());
         buffer.writeBytes(tempBuf);
      } finally {
         tempBuf.release();
      }
   }
}
