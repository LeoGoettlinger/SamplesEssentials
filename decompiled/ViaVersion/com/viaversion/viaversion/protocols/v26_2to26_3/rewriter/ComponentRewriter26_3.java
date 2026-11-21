package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

public final class ComponentRewriter26_3 extends NBTComponentRewriter<ClientboundPacket26_1> {
   public ComponentRewriter26_3(Protocol26_2To26_3 protocol) {
      super(protocol);
   }

   @Override
   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         this.removeDataComponents(
            componentsTag,
            new StructuredDataKey[]{
               StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1,
               StructuredDataKey.POT_DECORATIONS1_20_5,
               StructuredDataKey.MAP_COLOR,
               StructuredDataKey.TRIM1_21_5,
               StructuredDataKey.SWING_ANIMATION
            }
         );
      }
   }
}
