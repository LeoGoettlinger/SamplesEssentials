package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

public final class ComponentRewriter1_21_9 extends NBTComponentRewriter<ClientboundPacket1_21_6> {
   public ComponentRewriter1_21_9(Protocol1_21_7To1_21_9 protocol) {
      super(protocol);
   }

   @Override
   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         this.removeDataComponents(
            componentsTag,
            new StructuredDataKey[]{StructuredDataKey.ENTITY_DATA1_20_5, StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, StructuredDataKey.BEES1_20_5}
         );
      }
   }
}
