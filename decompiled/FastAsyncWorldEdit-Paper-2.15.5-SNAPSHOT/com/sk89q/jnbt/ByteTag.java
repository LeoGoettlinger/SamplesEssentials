package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinByteTag;

@Deprecated
public final class ByteTag extends NumberTag<LinByteTag> {
   public ByteTag(byte value) {
      this(LinByteTag.of(value));
   }

   public ByteTag(LinByteTag tag) {
      super(tag);
   }

   public Byte getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 1;
   }
}
