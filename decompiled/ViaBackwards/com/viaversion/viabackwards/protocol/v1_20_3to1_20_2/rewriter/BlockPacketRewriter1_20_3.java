package com.viaversion.viabackwards.protocol.v1_20_3to1_20_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.protocol.v1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public final class BlockPacketRewriter1_20_3 extends BlockRewriter<ClientboundPacket1_20_3> {
   public BlockPacketRewriter1_20_3(Protocol1_20_3To1_20_2 protocol) {
      super(protocol, Types.BLOCK_POSITION1_14, Types.COMPOUND_TAG, ChunkType1_20_2::new, null);
   }

   public void handleBlockEntity(UserConnection connection, BlockEntity blockEntity) {
      CompoundTag tag = blockEntity.tag();
      if (tag != null) {
         StringTag customName = tag.getStringTag("CustomName");
         if (customName != null) {
            try {
               JsonElement updatedComponent = ComponentUtil.convertJson(customName.getValue(), SerializerVersion.V1_20_3, SerializerVersion.V1_19_4);
               customName.setValue(updatedComponent.toString());
            } catch (Exception var6) {
               if (Via.getConfig().logTextComponentConversionErrors()) {
                  this.protocol.getLogger().log(Level.SEVERE, "Error during custom name conversion: " + StringUtil.forLogging(customName.getValue()), var6);
               }
            }
         }
      }
   }
}
