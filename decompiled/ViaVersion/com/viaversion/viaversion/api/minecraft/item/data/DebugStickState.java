package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;

public record DebugStickState(CompoundTag tag) implements Rewritable, Copyable {
   public static final Type<DebugStickState> TYPE = TransformingType.of(Types.COMPOUND_TAG, DebugStickState.class, DebugStickState::new, DebugStickState::tag);

   public DebugStickState rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      CompoundTag updatedTag = this.tag;
      if (protocol.getMappingData() != null && protocol.getMappingData().changedBlocks() != null) {
         updatedTag = this.tag.copy();
         updatedTag.entrySet().removeIf(entry -> {
            int blockId = protocol.getMappingData().getFullBlockMappings().id(entry.getKey());
            return blockId == -1 || protocol.getMappingData().changedBlocks().contains(blockId);
         });
      }

      return new DebugStickState(updatedTag);
   }

   public DebugStickState copy() {
      return new DebugStickState(this.tag.copy());
   }
}
