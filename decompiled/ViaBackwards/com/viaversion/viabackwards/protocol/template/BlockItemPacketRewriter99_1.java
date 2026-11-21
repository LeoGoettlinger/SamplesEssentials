package com.viaversion.viabackwards.protocol.template;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;

final class BlockItemPacketRewriter99_1 extends BackwardsStructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket1_21_9, Protocol99_1To98_1> {
   public BlockItemPacketRewriter99_1(Protocol99_1To98_1 protocol) {
      super(protocol);
   }

   public void registerPackets() {
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer dataContainer, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, dataContainer, backupTag);
   }
}
