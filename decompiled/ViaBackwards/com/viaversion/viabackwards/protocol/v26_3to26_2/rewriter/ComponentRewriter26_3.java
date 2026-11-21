package com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPacket26_3;

public final class ComponentRewriter26_3 extends NBTComponentRewriter<ClientboundPacket26_3> {
   public ComponentRewriter26_3(BackwardsProtocol<ClientboundPacket26_3, ?, ?, ?> protocol) {
      super(protocol);
   }

   protected void handleShowItem(UserConnection connection, CompoundTag itemTag, CompoundTag componentsTag) {
      super.handleShowItem(connection, itemTag, componentsTag);
      if (componentsTag != null) {
         this.removeDataComponents(
            componentsTag,
            new StructuredDataKey[]{
               StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3,
               StructuredDataKey.PROVIDES_POTTERY_PATTERN,
               StructuredDataKey.BLOCK_TRANSFORMER,
               StructuredDataKey.COMPOSTABLE,
               StructuredDataKey.INTERACT_ANIMATION,
               StructuredDataKey.ATTACK_ANIMATION,
               StructuredDataKey.TRIM26_3,
               StructuredDataKey.COOKING_FUEL,
               StructuredDataKey.BREWING_FUEL,
               StructuredDataKey.VILLAGER_FOOD,
               StructuredDataKey.MOB_VISIBILITY,
               StructuredDataKey.SIGN_TEXT_BACK,
               StructuredDataKey.SIGN_TEXT_FRONT,
               StructuredDataKey.WAXED,
               StructuredDataKey.CUSHION_COLOR
            }
         );
         this.removeDataComponents(componentsTag, new String[]{"pot_decorations"});
      }
   }
}
