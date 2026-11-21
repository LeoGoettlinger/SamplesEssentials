package com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.protocol.v26_3to26_2.Protocol26_3To26_2;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPacket26_3;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;

public final class BlockPacketRewriter26_3 extends BlockRewriter1_21_5<ClientboundPacket26_3> {
   public BlockPacketRewriter26_3(Protocol26_3To26_2 protocol) {
      super(protocol, ChunkType26_1::new);
   }

   public void handleBlockEntity(UserConnection connection, BlockEntity blockEntity) {
      super.handleBlockEntity(connection, blockEntity);
      CompoundTag tag = blockEntity.tag();
      if (tag != null) {
         CompoundTag decorations = tag.getCompoundTag("sherds");
         if (decorations != null) {
            ListTag<StringTag> sherds = new ListTag(StringTag.class);

            for (String side : com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.BlockPacketRewriter26_3.POT_DECORATION_SIDES) {
               CompoundTag sideTag = decorations.getCompoundTag(side);
               String item = sideTag != null ? sideTag.getString("id") : null;
               sherds.add(new StringTag(item != null ? item : "minecraft:brick"));
            }

            tag.put("sherds", sherds);
         }
      }
   }
}
