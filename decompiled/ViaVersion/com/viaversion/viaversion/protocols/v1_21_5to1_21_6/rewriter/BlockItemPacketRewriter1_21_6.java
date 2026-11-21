package com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_6 extends StructuredItemRewriter<ClientboundPacket1_21_5, ServerboundPacket1_21_6, Protocol1_21_5To1_21_6> {
   public BlockItemPacketRewriter1_21_6(Protocol1_21_5To1_21_6 protocol) {
      super(protocol);
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeItemData(item);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeItemData(item);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   public static void upgradeItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_5, StructuredDataKey.EQUIPPABLE1_21_6);
      dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6);
   }

   public static void downgradeItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.EQUIPPABLE1_21_6, StructuredDataKey.EQUIPPABLE1_21_5);
      dataContainer.replaceKey(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_5);
   }
}
