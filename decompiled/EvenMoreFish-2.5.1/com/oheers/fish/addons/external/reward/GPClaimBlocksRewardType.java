package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class GPClaimBlocksRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      int rewardBlocks;
      try {
         rewardBlocks = Integer.parseInt(value);
      } catch (NumberFormatException var8) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + this.getIdentifier() + ": " + value);
         return;
      }

      PlayerData data = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
      int currentBonus = data.getBonusClaimBlocks();
      data.setBonusClaimBlocks(currentBonus + rewardBlocks);
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "GP_CLAIM_BLOCKS";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   public JavaPlugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
