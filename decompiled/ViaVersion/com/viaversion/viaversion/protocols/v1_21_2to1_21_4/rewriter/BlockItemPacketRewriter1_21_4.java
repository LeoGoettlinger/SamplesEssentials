package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPacket1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.provider.PickItemProvider;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

public final class BlockItemPacketRewriter1_21_4 extends StructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_4, Protocol1_21_2To1_21_4> {
   public BlockItemPacketRewriter1_21_4(Protocol1_21_2To1_21_4 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_21_2.SET_HELD_SLOT, wrapper -> {
         byte slot = wrapper.read(Types.BYTE);
         wrapper.write(Types.VAR_INT, Integer.valueOf(slot));
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_4.PICK_ITEM_FROM_BLOCK, null, wrapper -> {
         BlockPosition blockPosition = wrapper.read(Types.BLOCK_POSITION1_14);
         boolean includeData = wrapper.read(Types.BOOLEAN);
         Via.getManager().getProviders().get(PickItemProvider.class).pickItemFromBlock(wrapper.user(), blockPosition, includeData);
         wrapper.cancel();
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_4.PICK_ITEM_FROM_ENTITY, null, wrapper -> {
         int entityId = wrapper.read(Types.VAR_INT);
         boolean includeData = wrapper.read(Types.BOOLEAN);
         Via.getManager().getProviders().get(PickItemProvider.class).pickItemFromEntity(wrapper.user(), entityId, includeData);
         wrapper.cancel();
      });
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      super.handleItemToClient(connection, item);
      StructuredDataContainer dataContainer = item.dataContainer();
      Integer modelData = dataContainer.get(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5);
      if (modelData != null) {
         dataContainer.set(
            StructuredDataKey.CUSTOM_MODEL_DATA1_21_4,
            new CustomModelData1_21_4(new float[]{modelData.floatValue()}, new boolean[0], new String[0], new int[0])
         );
         this.saveTag(this.createCustomTag(item), new IntTag(modelData), "custom_model_data");
      }

      updateItemData(item);
      this.appendItemDataFixComponents(connection, item);
      return item;
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      super.handleItemToServer(connection, item);
      StructuredDataContainer dataContainer = item.dataContainer();
      CompoundTag customData = dataContainer.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null && customData.remove(this.nbtTagName("custom_model_data")) instanceof IntTag customModelData) {
         dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, customModelData.asInt());
         this.removeCustomTag(dataContainer, customData);
      }

      downgradeItemData(item);
      return item;
   }

   private void appendItemDataFixComponents(UserConnection connection, Item item) {
      ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
      if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)
         && (item.identifier() == 849 || item.identifier() == 854 || item.identifier() == 859 || item.identifier() == 864 || item.identifier() == 869)) {
         item.dataContainer()
            .set(
               StructuredDataKey.CONSUMABLE1_21_2,
               new Consumable1_21_2(3600.0F, 3, Holder.of(new SoundEvent("minecraft:intentionally_empty", null)), false, new ConsumeEffect[0])
            );
      }
   }

   public static void updateItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.TRIM1_21_2, StructuredDataKey.TRIM1_21_4);
      dataContainer.remove(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5);
   }

   public static void downgradeItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replaceKey(StructuredDataKey.TRIM1_21_4, StructuredDataKey.TRIM1_21_2);
      dataContainer.remove(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4);
   }
}
