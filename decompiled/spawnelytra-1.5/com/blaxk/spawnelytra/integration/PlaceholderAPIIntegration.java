package com.blaxk.spawnelytra.integration;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.data.PlayerDataManager;
import com.blaxk.spawnelytra.listener.SpawnElytra;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {
   private final Main plugin;
   private final PlayerDataManager playerDataManager;

   public PlaceholderAPIIntegration(Main plugin, PlayerDataManager playerDataManager) {
      this.plugin = plugin;
      this.playerDataManager = playerDataManager;
   }

   @NotNull
   public String getIdentifier() {
      return "spawnelytra";
   }

   @NotNull
   public String getAuthor() {
      return "blaxk";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String onPlaceholderRequest(Player player, @NotNull String identifier) {
      if (player == null) {
         return "";
      } else if ("fly_count".equals(identifier)) {
         return String.valueOf(this.playerDataManager.getPlayerData(player.getUniqueId()).getFlyCount());
      } else if ("boost_count".equals(identifier)) {
         return String.valueOf(this.playerDataManager.getPlayerData(player.getUniqueId()).getBoostCount());
      } else if ("total_count".equals(identifier)) {
         PlayerDataManager.PlayerData data = this.playerDataManager.getPlayerData(player.getUniqueId());
         return String.valueOf(data.getFlyCount() + data.getBoostCount());
      } else if ("flying".equals(identifier)) {
         SpawnElytra instance = this.plugin.getSpawnElytraInstance(player.getWorld().getName());
         return String.valueOf(instance != null && instance.isFlying(player));
      } else if (!"in_area".equals(identifier)) {
         if ("boosts_remaining".equals(identifier)) {
            SpawnElytra instance = this.plugin.getSpawnElytraInstance(player.getWorld().getName());
            return String.valueOf(instance != null ? instance.getBoostsRemaining(player) : 0);
         } else {
            return null;
         }
      } else {
         SpawnElytra instance = this.plugin.getSpawnElytraInstance(player.getWorld().getName());
         return String.valueOf(instance != null && instance.isInSpawnArea(player));
      }
   }
}
