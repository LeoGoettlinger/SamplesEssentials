package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttackRange;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage.ProtocolStorables1_21_11;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_11 extends StructuredItemRewriter<ClientboundPacket1_21_9, ServerboundPacket1_21_9, Protocol1_21_9To1_21_11> {
   public BlockItemPacketRewriter1_21_11(Protocol1_21_9To1_21_11 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_21_9.SET_BORDER_LERP_SIZE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.VAR_LONG, wrapper.read(Types.VAR_LONG) / 50L);
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_9.INITIALIZE_BORDER, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.VAR_LONG, wrapper.read(Types.VAR_LONG) / 50L);
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_9.SET_TIME, wrapper -> {
         long gameTime = wrapper.passthrough(Types.LONG);
         ProtocolStorables1_21_11 storables = wrapper.user().storables(this.protocol);
         storables.gameTimeStorage().setGameTime(gameTime);
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_6.CLIENT_TICK_END, wrapper -> {
         ProtocolStorables1_21_11 storables = wrapper.user().storables(this.protocol);
         storables.gameTimeStorage().incrementGameTime();
      });
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeData(item, container);
      this.appendItemDataFixComponents(connection, item);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeData(item, container);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   private void appendItemDataFixComponents(UserConnection connection, Item item) {
      ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
      if (Via.getConfig().use1_8HitboxMargin() && serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)) {
         item.dataContainer().set(StructuredDataKey.ATTACK_RANGE, new AttackRange(0.0F, 3.0F, 0.0F, 4.0F, 0.1F, 1.0F));
      }
   }

   public static void upgradeData(Item item, StructuredDataContainer container) {
      container.replaceKey(StructuredDataKey.CONSUMABLE1_21_5, StructuredDataKey.CONSUMABLE1_21_2);
   }

   public static void downgradeData(Item item, StructuredDataContainer container) {
      container.replaceKey(StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.CONSUMABLE1_21_5);
      container.remove(StructuredDataKey.SWING_ANIMATION);
      container.remove(StructuredDataKey.KINETIC_WEAPON);
      container.remove(StructuredDataKey.PIERCING_WEAPON);
      container.remove(StructuredDataKey.DAMAGE_TYPE1_21_11);
      container.remove(StructuredDataKey.MINIMUM_ATTACK_CHARGE);
      container.remove(StructuredDataKey.USE_EFFECTS);
      container.remove(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11);
      container.remove(StructuredDataKey.ATTACK_RANGE);
   }
}
