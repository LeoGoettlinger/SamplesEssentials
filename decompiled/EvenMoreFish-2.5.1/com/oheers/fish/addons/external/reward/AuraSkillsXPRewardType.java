package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class AuraSkillsXPRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      String[] split = value.split(",");
      if (split.length < 2) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid format for RewardType " + this.getIdentifier() + ": " + value);
         EvenMoreFish.getInstance().getLogger().warning("Expected \"Name,Amount\"");
      } else {
         String name = split[0];

         double amount;
         try {
            amount = Double.parseDouble(split[1]);
         } catch (NumberFormatException var15) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + this.getIdentifier() + ": " + split[2]);
            return;
         }

         AuraSkillsApi api = AuraSkillsApi.get();

         Skill skill;
         try {
            skill = Skills.valueOf(name);
         } catch (IllegalArgumentException var14) {
            try {
               skill = api.getGlobalRegistry().getSkill(NamespacedId.fromString(name));
            } catch (IllegalArgumentException var13) {
               EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + this.getIdentifier() + ": " + name);
               return;
            }
         }

         if (skill == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + this.getIdentifier() + ": " + name);
         } else {
            api.getUser(player.getUniqueId()).addSkillXp(skill, amount);
         }
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "AURASKILLS_XP";
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
