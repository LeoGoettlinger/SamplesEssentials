package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.RegistryAndTags;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectMap;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;

public final class RegistryDataRewriter1_21_6 extends BackwardsRegistryRewriter {
   public RegistryDataRewriter1_21_6(BackwardsProtocol<?, ?, ?, ?> protocol) {
      super(protocol);
      this.remove("dialog");
   }

   @Override
   public RegistryEntry[] handle(UserConnection connection, String key, RegistryEntry[] entries) {
      if (Key.stripMinecraftNamespace(key).equals("dialog")) {
         String[] keys = new String[entries.length];

         for (int i = 0; i < entries.length; i++) {
            keys[i] = Key.stripMinecraftNamespace(entries[i].key());
         }

         Object2ObjectMap<String, CompoundTag> dialogs = new Object2ObjectArrayMap();

         for (RegistryEntry entry : entries) {
            if (entry.tag() instanceof CompoundTag tag) {
               dialogs.put(Key.stripMinecraftNamespace(entry.key()), tag);
            }
         }

         RegistryAndTags registryAndTags = (RegistryAndTags)connection.get(RegistryAndTags.class);
         registryAndTags.storeRegistry(new KeyMappings(keys), dialogs);
      }

      return super.handle(connection, key, entries);
   }
}
