package com.viaversion.viabackwards.protocol.template;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;

final class ComponentRewriter99_1 extends NBTComponentRewriter<ClientboundPacket26_1> {
   public ComponentRewriter99_1(BackwardsProtocol<ClientboundPacket26_1, ?, ?, ?> protocol) {
      super(protocol);
   }

   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         ;
      }
   }
}
