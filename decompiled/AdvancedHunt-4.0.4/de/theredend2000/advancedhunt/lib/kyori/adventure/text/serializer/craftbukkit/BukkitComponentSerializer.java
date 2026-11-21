package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.craftbukkit;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class BukkitComponentSerializer {
   private BukkitComponentSerializer() {
   }

   @NotNull
   public static LegacyComponentSerializer legacy() {
      return de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.BukkitComponentSerializer.legacy();
   }

   @NotNull
   public static GsonComponentSerializer gson() {
      return de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.BukkitComponentSerializer.gson();
   }
}
