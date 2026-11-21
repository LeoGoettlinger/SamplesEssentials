package com.sk89q.jnbt;

import org.enginehub.linbus.tree.LinByteArrayTag;

@Deprecated
public final class ByteArrayTag extends Tag<byte[], LinByteArrayTag> {
   public ByteArrayTag(byte[] value) {
      this(LinByteArrayTag.of(value));
   }

   public ByteArrayTag(LinByteArrayTag tag) {
      super(tag);
   }

   @Override
   public int getTypeCode() {
      return 7;
   }
}
