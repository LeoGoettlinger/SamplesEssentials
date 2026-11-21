package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R1.items.datacomponents.impl;

import net.kyori.adventure.key.Key;
import net.minecraft.resources.MinecraftKey;

public final class IdentifierDataComponent implements DataComponentHandler<Key, MinecraftKey> {
   public MinecraftKey toNMS(Key from) {
      return MinecraftKey.a(from.namespace(), from.value());
   }

   public Key fromNMS(MinecraftKey data) {
      return Key.key(data.b(), data.a());
   }
}
