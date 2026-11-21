package com.viaversion.viaversion.api.minecraft.blockentity;

import com.viaversion.nbt.tag.CompoundTag;

public record BlockEntityImpl(byte packedXZ, short y, int typeId, CompoundTag tag) implements BlockEntity {
   @Override
   public BlockEntity withTypeId(int typeId) {
      return new BlockEntityImpl(this.packedXZ, this.y, typeId, this.tag);
   }
}
