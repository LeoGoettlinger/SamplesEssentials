package me.clip.placeholderapi.libs.kyori.adventure.text.serializer.craftbukkit;

import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class BukkitComponentSerializer {
   private BukkitComponentSerializer() {
   }

   @NotNull
   public static LegacyComponentSerializer legacy() {
      return me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitComponentSerializer.legacy();
   }

   @NotNull
   public static GsonComponentSerializer gson() {
      return me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitComponentSerializer.gson();
   }
}
