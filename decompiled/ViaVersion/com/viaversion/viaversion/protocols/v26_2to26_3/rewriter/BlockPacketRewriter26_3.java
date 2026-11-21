package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;

public final class BlockPacketRewriter26_3 extends BlockRewriter1_21_5<ClientboundPacket26_1> {
   public static final String[] POT_DECORATION_SIDES = new String[]{"back", "left", "right", "front"};

   public BlockPacketRewriter26_3(Protocol26_2To26_3 protocol) {
      super(protocol, ChunkType26_1::new);
   }

   @Override
   public void handleBlockEntity(UserConnection connection, BlockEntity blockEntity) {
      super.handleBlockEntity(connection, blockEntity);
      CompoundTag tag = blockEntity.tag();
      if (tag != null) {
         ListTag<StringTag> sherds = tag.getListTag("sherds", StringTag.class);
         if (sherds != null) {
            CompoundTag decorations = new CompoundTag();

            for (int i = 0; i < POT_DECORATION_SIDES.length; i++) {
               StringTag sherd = i < sherds.size() ? sherds.get(i) : null;
               CompoundTag sideTag = new CompoundTag();
               sideTag.putString("id", sherd != null ? sherd.getValue() : "minecraft:brick");
               decorations.put(POT_DECORATION_SIDES[i], sideTag);
            }

            tag.put("sherds", decorations);
         }
      }
   }
}
