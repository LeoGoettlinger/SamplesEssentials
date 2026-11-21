package com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.rewriter;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.rewriters.BackwardsFullStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.Protocol1_21_4To1_21_2;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.CustomModelData1_21_4;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;

public final class BlockItemPacketRewriter1_21_4
   extends BackwardsFullStructuredItemRewriter<ClientboundPacket1_21_2, ServerboundPacket1_21_2, Protocol1_21_4To1_21_2> {
   public BlockItemPacketRewriter1_21_4(Protocol1_21_4To1_21_2 protocol) {
      super(protocol);
   }

   public void registerPackets() {
      ((Protocol1_21_4To1_21_2)this.protocol).registerClientbound(ClientboundPackets1_21_2.SET_HELD_SLOT, wrapper -> {
         int slot = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.BYTE, (byte)slot);
      });
      ((Protocol1_21_4To1_21_2)this.protocol).cancelServerbound(ServerboundPackets1_21_2.PICK_ITEM);
   }

   public Item handleItemToClient(UserConnection connection, Item item) {
      item = super.handleItemToClient(connection, item);
      StructuredDataContainer dataContainer = item.dataContainer();
      CustomModelData1_21_4 modelData = (CustomModelData1_21_4)dataContainer.get(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4);
      if (modelData != null) {
         this.saveTag(this.createCustomTag(item), this.customModelDataToTag(modelData), "custom_model_data");
         if (ViaBackwards.getConfig().mapCustomModelData() && modelData.floats().length > 0) {
            int data = (int)modelData.floats()[0];
            dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, data);
         }
      }

      com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter.BlockItemPacketRewriter1_21_4.downgradeItemData(item);
      return item;
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      item = super.handleItemToServer(connection, item);
      StructuredDataContainer dataContainer = item.dataContainer();
      CompoundTag customData = (CompoundTag)dataContainer.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null && customData.remove(this.nbtTagName("custom_model_data")) instanceof CompoundTag customModelData) {
         dataContainer.set(StructuredDataKey.CUSTOM_MODEL_DATA1_21_4, this.customModelDataFromTag(customModelData));
         this.removeCustomTag(dataContainer, customData);
      }

      com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter.BlockItemPacketRewriter1_21_4.updateItemData(item);
      return item;
   }

   private CustomModelData1_21_4 customModelDataFromTag(CompoundTag tag) {
      IntArrayTag floatsTag = tag.getIntArrayTag("floats");
      float[] floats = new float[floatsTag.getValue().length];

      for (int i = 0; i < floats.length; i++) {
         floats[i] = Float.intBitsToFloat(floatsTag.get(i));
      }

      ByteArrayTag booleansTag = tag.getByteArrayTag("booleans");
      boolean[] booleans = new boolean[booleansTag.getValue().length];

      for (int i = 0; i < booleans.length; i++) {
         booleans[i] = booleansTag.get(i) != 0;
      }

      ListTag<StringTag> stringsTag = tag.getListTag("strings", StringTag.class);
      String[] strings = new String[stringsTag.size()];

      for (int i = 0; i < strings.length; i++) {
         strings[i] = ((StringTag)stringsTag.get(i)).getValue();
      }

      IntArrayTag colorsTag = tag.getIntArrayTag("colors");
      return new CustomModelData1_21_4(floats, booleans, strings, colorsTag.getValue());
   }

   private CompoundTag customModelDataToTag(CustomModelData1_21_4 customModelData) {
      CompoundTag tag = new CompoundTag();
      int[] floats = new int[customModelData.floats().length];

      for (int i = 0; i < floats.length; i++) {
         floats[i] = Float.floatToIntBits(customModelData.floats()[i]);
      }

      tag.put("floats", new IntArrayTag(floats));
      byte[] booleans = new byte[customModelData.booleans().length];

      for (int i = 0; i < booleans.length; i++) {
         booleans[i] = (byte)(customModelData.booleans()[i] ? 1 : 0);
      }

      tag.put("booleans", new ByteArrayTag(booleans));
      ListTag<StringTag> strings = new ListTag(StringTag.class);

      for (String string : customModelData.strings()) {
         strings.add(new StringTag(string));
      }

      tag.put("strings", strings);
      tag.put("colors", new IntArrayTag(customModelData.colors()));
      return tag;
   }
}
