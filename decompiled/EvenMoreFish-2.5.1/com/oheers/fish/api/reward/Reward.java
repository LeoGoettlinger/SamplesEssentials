package com.oheers.fish.api.reward;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Reward {
   private static final Map<UUID, List<Reward.RewardData>> rewardCache = new HashMap<>();
   @NonNull
   private final String key;
   @NonNull
   private final String value;
   private RewardType rewardType = null;
   private Vector fishVelocity;

   public Reward(@NonNull String identifier) {
      String[] split = identifier.split(":");
      if (split.length < 2) {
         EMFPlugin.getInstance().getLogger().warning(identifier + " is not formatted correctly. It won't be given as a reward");
         this.key = "";
         this.value = "";
      } else {
         this.key = split[0];
         this.value = String.join(":", Arrays.copyOfRange(split, 1, split.length));
      }

      RewardType rewardType = EMFRegistry.REWARD_TYPE.get(this.key);
      if (rewardType != null) {
         this.rewardType = rewardType;
      }
   }

   public RewardType getRewardType() {
      return this.rewardType;
   }

   @NonNull
   public String getKey() {
      return this.key;
   }

   @NonNull
   public String getValue() {
      return this.value;
   }

   public void give(@NonNull Player player, @Nullable Location location) {
      if (this.rewardType == null) {
         EMFPlugin.getInstance().getLogger().warning("No reward type found for key: " + this.getKey());
      } else {
         this.rewardType.doReward(player, this.getKey(), this.getValue(), location);
      }
   }

   public void give(@NonNull OfflinePlayer player, @Nullable Location location) {
      this.give(player, location, true);
   }

   public void give(@NonNull OfflinePlayer player, @Nullable Location location, boolean shouldAddToCache) {
      Player online = player.getPlayer();
      if (online != null) {
         this.give(online, location);
      } else {
         if (shouldAddToCache) {
            Logging.debug("Player " + player.getUniqueId() + " was not online. Caching their rewards.");
            this.addToCache(player.getUniqueId(), location);
         }
      }
   }

   private void addToCache(@NonNull UUID uuid, @Nullable Location location) {
      List<Reward.RewardData> cached = rewardCache.computeIfAbsent(uuid, u -> new ArrayList<>());
      cached.add(new Reward.RewardData(this, location));
   }

   public static void checkCache(@NonNull UUID uuid) {
      if (!rewardCache.containsKey(uuid)) {
         Logging.debug("Player has no cached rewards.");
      } else {
         Player player = Bukkit.getPlayer(uuid);
         if (player == null) {
            Logging.debug("Player was not online. Cannot give rewards.");
         } else {
            Logging.debug("Found cached rewards for " + uuid + ". Giving them all now.");
            EMFPlugin.getInstance().sendMessage("REWARD_CATCHUP", player);
            List<Reward.RewardData> cached = rewardCache.remove(uuid);
            cached.forEach(data -> data.reward().give(player, data.hookLocation()));
         }
      }
   }

   public void setFishVelocity(@Nullable Vector fishVelocity) {
      this.fishVelocity = fishVelocity;
   }

   @Nullable
   public Vector getFishVelocity() {
      return this.fishVelocity;
   }

   @Deprecated(
      since = "2.4.5"
   )
   public void rewardPlayer(@NonNull Player player, @Nullable Location location) {
      this.give(player, location);
   }

   @Deprecated(
      since = "2.4.5"
   )
   public void rewardPlayer(@NonNull OfflinePlayer player, @Nullable Location location) {
      this.give(player, location);
   }

   @Deprecated(
      since = "2.4.5"
   )
   public void rewardPlayer(@NonNull OfflinePlayer player, @Nullable Location location, boolean shouldAddToCache) {
      this.give(player, location, shouldAddToCache);
   }

   record RewardData(@NonNull Reward reward, @Nullable Location hookLocation) {
   }
}
