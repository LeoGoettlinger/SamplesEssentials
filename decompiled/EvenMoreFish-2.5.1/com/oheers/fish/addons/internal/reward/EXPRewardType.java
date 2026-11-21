package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class EXPRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      int experience;
      try {
         experience = Integer.parseInt(value);
      } catch (NumberFormatException var7) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + this.getIdentifier() + ": " + value);
         return;
      }

      player.giveExp(experience);
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "EXP";
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
