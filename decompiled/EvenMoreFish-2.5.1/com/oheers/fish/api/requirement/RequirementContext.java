package com.oheers.fish.api.requirement;

import com.oheers.fish.api.fishing.FishingType;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RequirementContext {
   private NamespacedKey worldKey;
   private Location location;
   private UUID player;
   private YamlDocument config;
   private String configPath;
   private FishingType fishingType;

   public RequirementContext(
      @Nullable World world,
      @Nullable Location location,
      @Nullable Player player,
      @Nullable YamlDocument config,
      @Nullable String configPath,
      @Nullable FishingType fishingType
   ) {
      this.worldKey = world == null ? null : world.getKey();
      this.location = location;
      this.player = player == null ? null : player.getUniqueId();
      this.config = config;
      this.configPath = configPath;
      this.fishingType = fishingType == null ? FishingType.VANILLA : fishingType;
   }

   @NonNull
   public static RequirementContext empty() {
      return new RequirementContext(null, null, null, null, null, null);
   }

   @NonNull
   public static RequirementContext player(@NonNull Player player) {
      return new RequirementContext(player.getWorld(), player.getLocation(), player, null, null, null);
   }

   @Nullable
   public World getWorld() {
      return Bukkit.getWorld(this.worldKey);
   }

   public void setWorld(@Nullable World world) {
      this.worldKey = world == null ? null : world.getKey();
   }

   @Nullable
   public Location getLocation() {
      return this.location;
   }

   public void setConfig(@Nullable YamlDocument config) {
      this.config = config;
   }

   @Nullable
   public YamlDocument getConfig() {
      return this.config;
   }

   public void setConfigPath(@Nullable String configPath) {
      this.configPath = configPath;
   }

   @Nullable
   public String getConfigPath() {
      return this.configPath;
   }

   public void setLocation(@Nullable Location location) {
      this.location = location;
      this.worldKey = location == null ? null : location.getWorld().getKey();
   }

   @Nullable
   public Player getPlayer() {
      return this.player == null ? null : Bukkit.getPlayer(this.player);
   }

   public void setPlayer(@Nullable Player player) {
      this.player = player == null ? null : player.getUniqueId();
   }

   @Nullable
   public Location getHookOrPlayerLocation() {
      if (this.location != null) {
         return this.location;
      } else {
         Player player = this.getPlayer();
         return player != null ? player.getLocation() : null;
      }
   }

   @Nullable
   public FishingType getFishingType() {
      return this.fishingType;
   }

   public void setFishingType(@Nullable FishingType fishingType) {
      this.fishingType = fishingType;
   }
}
