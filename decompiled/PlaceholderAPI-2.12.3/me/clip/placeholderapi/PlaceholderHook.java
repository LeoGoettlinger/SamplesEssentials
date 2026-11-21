package me.clip.placeholderapi;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholderHook {
   @Nullable
   public String onRequest(OfflinePlayer player, @NotNull String params) {
      return player != null && player.isOnline() ? this.onPlaceholderRequest(player.getPlayer(), params) : this.onPlaceholderRequest(null, params);
   }

   @Nullable
   public String onPlaceholderRequest(Player player, @NotNull String params) {
      return null;
   }
}
