package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.Protocol1_21_6To1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.Dialog;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.provider.DialogViewProvider;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectMap;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.util.Key;
import java.util.UUID;

public final class ClickEvents implements StorableObject {
   private final Object2ObjectMap<String, CompoundTag> clickEvents = new Object2ObjectArrayMap();

   public String storeClickEvent(CompoundTag clickEvent) {
      String id = "vv_" + UUID.randomUUID();
      if (this.clickEvents.containsKey(id)) {
         return this.storeClickEvent(clickEvent);
      } else {
         this.clickEvents.put(id, clickEvent);
         return id;
      }
   }

   public boolean handleChatCommand(UserConnection connection, String command) {
      CompoundTag clickEvent = (CompoundTag)this.clickEvents.get(command);
      if (clickEvent != null) {
         handleClickEvent(connection, clickEvent);
         return true;
      } else {
         return false;
      }
   }

   public static void handleClickEvent(UserConnection connection, CompoundTag clickEvent) {
      String action = Key.stripMinecraftNamespace(clickEvent.getString("action"));
      if ("show_dialog".equals(action)) {
         if (!ViaBackwards.getConfig().dialogsViaChests()) {
            return;
         }

         RegistryAndTags registryAndTags = (RegistryAndTags)connection.get(RegistryAndTags.class);
         ServerLinks serverLinks = (ServerLinks)connection.get(ServerLinks.class);
         CompoundTag dialogTag = clickEvent.getCompoundTag("dialog");
         if (dialogTag == null) {
            StringTag dialogReferenceTag = clickEvent.getStringTag("dialog");
            if (dialogReferenceTag != null) {
               dialogTag = registryAndTags.fromRegistry(Key.stripMinecraftNamespace(dialogReferenceTag.getValue()));
            }
         }

         DialogViewProvider provider = (DialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
         provider.openDialog(connection, new Dialog(registryAndTags, serverLinks, dialogTag));
      } else if ("custom".equals(action)) {
         String id = clickEvent.getString("id");
         CompoundTag payload = clickEvent.getCompoundTag("payload");
         PacketWrapper customClickAction = PacketWrapper.create(ServerboundPackets1_21_6.CUSTOM_CLICK_ACTION, connection);
         customClickAction.write(Types.STRING, id);
         customClickAction.write(Types.CUSTOM_CLICK_ACTION_TAG, payload);
         customClickAction.sendToServer(Protocol1_21_6To1_21_5.class);
      }
   }
}
