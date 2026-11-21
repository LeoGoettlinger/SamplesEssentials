package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.craftbukkit;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Deprecated
public final class BukkitComponentSerializer {
   private BukkitComponentSerializer() {
   }

   @NotNull
   public static LegacyComponentSerializer legacy() {
      return xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.BukkitComponentSerializer.legacy();
   }

   @NotNull
   public static GsonComponentSerializer gson() {
      return xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.BukkitComponentSerializer.gson();
   }
}
