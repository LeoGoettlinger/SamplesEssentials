package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FinishingFlourishSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "swords_finishing_flourish";
   private static final double BASE_EXECUTE_BONUS = 0.5;
   private static final double HEALTH_THRESHOLD = 0.3;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public FinishingFlourishSkill() {
      super(
         SkillType.SWORDS,
         50,
         "Finishing Flourish",
         "Deal bonus damage to enemies below 30% health.",
         SkillBonusType.CONDITIONAL,
         3,
         "swords_finishing_flourish"
      );
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            LivingEntity entity = target.getLivingEntity();
            double healthPercent = entity.getHealth() / entity.getMaxHealth();
            return healthPercent <= 0.3;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.5, 0.01, skillLevel);
   }

   public static double applyExecuteBonus(Player player, EliteMobDamagedByPlayerEvent event, double currentDamage) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return currentDamage;
      } else {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            LivingEntity entity = target.getLivingEntity();
            double healthPercent = entity.getHealth() / entity.getMaxHealth();
            if (healthPercent > 0.3) {
               return currentDamage;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
               double bonus = 0.5 + skillLevel * 0.01;
               return currentDamage * (1.0 + bonus);
            }
         } else {
            return currentDamage;
         }
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double bonus = this.getConditionalBonus(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("value", String.format("%.0f", bonus)));
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
