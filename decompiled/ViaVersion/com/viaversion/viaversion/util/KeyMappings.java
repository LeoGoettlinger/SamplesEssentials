package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class KeyMappings {
   private final Object2IntMap<String> keyToId;
   private final Key[] keys;

   public KeyMappings(Key... keys) {
      this.keys = keys;
      this.keyToId = new Object2IntOpenHashMap<>(keys.length);
      this.keyToId.defaultReturnValue(-1);

      for (int i = 0; i < keys.length; i++) {
         this.keyToId.put(keys[i].minimized(), i);
      }
   }

   public KeyMappings(String... keys) {
      this(Arrays.stream(keys).map(Key::of).toArray(Key[]::new));
   }

   public KeyMappings(ListTag<StringTag> keys) {
      this(keys.getValue().stream().map(StringTag::getValue).toArray(String[]::new));
   }

   @Nullable
   public String idToKey(int id) {
      return id >= 0 && id < this.keys.length ? this.keys[id].minimized() : null;
   }

   @Nullable
   public Key key(int id) {
      return id >= 0 && id < this.keys.length ? this.keys[id] : null;
   }

   public int keyToId(String identifier) {
      return this.keyToId.getInt(Key.stripMinecraftNamespace(identifier));
   }

   public Key[] keys() {
      return this.keys;
   }

   public int size() {
      return this.keys.length;
   }
}
