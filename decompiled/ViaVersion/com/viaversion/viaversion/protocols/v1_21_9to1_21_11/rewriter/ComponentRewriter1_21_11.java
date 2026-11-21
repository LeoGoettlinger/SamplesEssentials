package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;

public final class ComponentRewriter1_21_11 extends NBTComponentRewriter<ClientboundPacket1_21_9> {
   public ComponentRewriter1_21_11(Protocol1_21_9To1_21_11 protocol) {
      super(protocol);
   }

   @Override
   protected void processCompoundTag(UserConnection connection, CompoundTag tag) {
      super.processCompoundTag(connection, tag);
      StringTag sprite = tag.getStringTag("sprite");
      if (sprite != null) {
         String strippedSprite = Key.stripNamespace(sprite.getValue());
         if (strippedSprite.startsWith("item/")) {
            tag.putString("atlas", "items");
         } else if (strippedSprite.startsWith("block/")) {
            tag.putString("atlas", "blocks");
         }
      }
   }
}
