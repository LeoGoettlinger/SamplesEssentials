package com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;

public final class ComponentRewriter26_2 extends NBTComponentRewriter<ClientboundPacket26_1> {
   public ComponentRewriter26_2(BackwardsProtocol<ClientboundPacket26_1, ?, ?, ?> protocol) {
      super(protocol);
   }

   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         this.removeDataComponents(componentsTag, new String[]{"sulfur_cube_content"});
      }
   }
}
