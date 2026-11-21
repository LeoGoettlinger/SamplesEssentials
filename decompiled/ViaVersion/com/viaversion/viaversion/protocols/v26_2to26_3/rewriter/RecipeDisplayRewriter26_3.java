package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;

public class RecipeDisplayRewriter26_3<C extends ClientboundPacketType> extends RecipeDisplayRewriter1_21_5<C> {
   public RecipeDisplayRewriter26_3(Protocol<C, ?, ?, ?> protocol) {
      super(protocol);
   }

   @Override
   protected void handleTag(PacketWrapper wrapper) {
      this.handleIngredient(wrapper);
   }
}
