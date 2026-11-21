package com.viaversion.viaversion.protocols.template;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

final class BlockItemPacketRewriter99_1 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket1_21_9, Protocol98_1To99_1> {
   public BlockItemPacketRewriter99_1(Protocol98_1To99_1 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer container, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, container, backupTag);
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeData(container);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeData(container);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   public static void upgradeData(StructuredDataContainer container) {
   }

   public static void downgradeData(StructuredDataContainer container) {
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
   }
}
