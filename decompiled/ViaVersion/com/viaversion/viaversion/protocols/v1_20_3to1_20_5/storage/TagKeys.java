package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import java.util.HashSet;
import java.util.Set;

public final class TagKeys {
   private final Set<String> identifiers = new HashSet<>();

   public TagKeys(PacketWrapper wrapper) {
      int length = wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < length; i++) {
         wrapper.passthrough(Types.STRING);
         int tagsSize = wrapper.passthrough(Types.VAR_INT);

         for (int j = 0; j < tagsSize; j++) {
            String key = wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            this.identifiers.add(Key.stripMinecraftNamespace(key));
         }
      }
   }

   public boolean isValidIdentifier(String identifier) {
      return this.identifiers.contains(Key.stripMinecraftNamespace(identifier));
   }
}
