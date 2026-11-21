package com.viaversion.viaversion.protocols.v1_20to1_20_2.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LastTags {
   private final List<LastTags.RegistryTags> registryTags = new ArrayList<>();
   private boolean sentDuringConfigPhase;

   public LastTags(PacketWrapper wrapper) {
      int length = wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < length; i++) {
         List<TagData> tags = new ArrayList<>();
         String registryKey = wrapper.passthrough(Types.STRING);
         int tagsSize = wrapper.passthrough(Types.VAR_INT);

         for (int j = 0; j < tagsSize; j++) {
            String key = wrapper.passthrough(Types.STRING);
            int[] ids = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            tags.add(new TagData(key, ids));
         }

         this.registryTags.add(new LastTags.RegistryTags(registryKey, tags));
      }
   }

   public boolean sendLastTags(UserConnection connection) {
      if (this.registryTags.isEmpty()) {
         return false;
      } else {
         PacketWrapper packet = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, connection);
         packet.write(Types.VAR_INT, this.registryTags.size());

         for (LastTags.RegistryTags registryTag : this.registryTags) {
            packet.write(Types.STRING, registryTag.registryKey);
            packet.write(Types.VAR_INT, registryTag.tags.size());

            for (TagData tag : registryTag.tags) {
               packet.write(Types.STRING, tag.identifier());
               packet.write(Types.VAR_INT_ARRAY_PRIMITIVE, Arrays.copyOf(tag.entries(), tag.entries().length));
            }
         }

         packet.send(Protocol1_20To1_20_2.class);
         return true;
      }
   }

   public void setSentDuringConfigPhase(boolean sentDuringConfigPhase) {
      this.sentDuringConfigPhase = sentDuringConfigPhase;
   }

   public boolean sentDuringConfigPhase() {
      return this.sentDuringConfigPhase;
   }

   private record RegistryTags(String registryKey, List<TagData> tags) {
   }
}
