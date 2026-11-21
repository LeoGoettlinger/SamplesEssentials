package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class CompoundTagHolderType extends HolderType<CompoundTag> {
   public CompoundTag readDirect(ByteBuf buffer) {
      return Types.TRUSTED_COMPOUND_TAG.read(buffer);
   }

   public void writeDirect(ByteBuf buffer, CompoundTag value) {
      Types.TRUSTED_COMPOUND_TAG.write(buffer, value);
   }
}
