package com.viaversion.viaversion.protocols.template;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

final class ComponentRewriter99_1 extends NBTComponentRewriter<ClientboundPacket26_1> {
   public ComponentRewriter99_1(Protocol98_1To99_1 protocol) {
      super(protocol);
   }

   @Override
   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         ;
      }
   }
}
