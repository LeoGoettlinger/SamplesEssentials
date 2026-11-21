package com.oheers.fish.addons.external.reward;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.InvalidSkillException;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class McMMOXPRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      String[] split = value.split(",");
      if (split.length < 2) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid format for RewardType " + this.getIdentifier() + ": " + value);
         EvenMoreFish.getInstance().getLogger().warning("Expected \"Name,Amount\"");
      } else {
         String name = split[0];

         int amount;
         try {
            amount = Integer.parseInt(split[1]);
         } catch (NumberFormatException var10) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + this.getIdentifier() + ": " + split[1]);
            return;
         }

         try {
            ExperienceAPI.addXP(player, name, amount, "unknown");
         } catch (InvalidSkillException var9) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + this.getIdentifier() + ": " + name);
         }
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "MCMMO_XP";
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
