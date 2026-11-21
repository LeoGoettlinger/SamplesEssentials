package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ItemTagStorage {
   private Map<String, int[]> itemTags = new HashMap<>();

   @Nullable
   public int[] itemTag(String key) {
      return this.itemTags.get(Key.stripMinecraftNamespace(key));
   }

   public void readItemTags(PacketWrapper wrapper) {
      int length = (Integer)wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < length; i++) {
         String registryKey = (String)wrapper.passthrough(Types.STRING);
         int tagsSize = (Integer)wrapper.passthrough(Types.VAR_INT);
         boolean itemRegistry = Key.stripMinecraftNamespace(registryKey).equals("item");
         if (itemRegistry) {
            this.itemTags = new HashMap<>(tagsSize);
         }

         for (int j = 0; j < tagsSize; j++) {
            String key = (String)wrapper.passthrough(Types.STRING);
            int[] ids = (int[])wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            if (itemRegistry) {
               this.itemTags.put(Key.stripMinecraftNamespace(key), ids);
            }
         }
      }
   }
}
