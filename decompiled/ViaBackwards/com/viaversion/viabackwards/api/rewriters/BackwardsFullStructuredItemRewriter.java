package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public class BackwardsFullStructuredItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType, T extends BackwardsProtocol<C, ?, ?, S>>
   extends BackwardsStructuredItemRewriter<C, S, T> {
   public BackwardsFullStructuredItemRewriter(T protocol) {
      super(protocol);
   }

   public Item handleItemToServer(UserConnection connection, Item item) {
      if (Item.isEmpty(item)) {
         return item;
      } else {
         ProtocolInfo protocolInfo = connection.getProtocolInfo();
         if (protocolInfo.serverProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_5)
            && !((BackwardsProtocol)this.protocol).getEntityRewriter().tracker(connection).canInstaBuild()) {
            CompoundTag customData = (CompoundTag)item.dataContainer().get(StructuredDataKey.CUSTOM_DATA);
            if (customData == null) {
               item.dataContainer().data().clear();
            } else {
               customData.keySet().removeIf(key -> !key.equals("VV|original_hashes"));
               item.dataContainer().data().keySet().removeIf(key -> key != StructuredDataKey.CUSTOM_DATA);
            }
         }

         return super.handleItemToServer(connection, item);
      }
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      Tag hashes = customData.get("VV|original_hashes");
      super.restoreBackupData(item, container, customData);
      if (hashes != null) {
         customData.put("VV|original_hashes", hashes);
         if (!container.has(StructuredDataKey.CUSTOM_DATA)) {
            container.set(StructuredDataKey.CUSTOM_DATA, customData);
            customData.putBoolean("VV|custom_data", true);
         }
      }
   }
}
