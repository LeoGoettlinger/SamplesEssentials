package com.viaversion.viaversion.protocols.v26_1to26_2.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys26_2;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter26_2 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_1, Protocol26_1To26_2> {
   public BlockItemPacketRewriter26_2(Protocol26_1To26_2 protocol) {
      super(protocol);
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeData(container);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeData(this.protocol.mappedTypes().structuredDataKeys(), container);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   public static void upgradeData(StructuredDataContainer container) {
   }

   public static void downgradeData(StructuredDataKeys26_2 dataKeys, StructuredDataContainer container) {
      container.remove(dataKeys.sulfurCubeContent);
   }
}
