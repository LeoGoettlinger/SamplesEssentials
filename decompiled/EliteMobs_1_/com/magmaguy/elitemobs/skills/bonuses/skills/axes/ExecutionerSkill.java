package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

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

public class ExecutionerSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "axes_executioner";
   private static final double BASE_DAMAGE_BONUS = 0.4;
   private static final double HEALTH_THRESHOLD = 0.4;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ExecutionerSkill() {
      super(SkillType.AXES, 25, "Executioner", "Deal bonus damage to enemies below 40% health.", SkillBonusType.CONDITIONAL, 2, "axes_executioner");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            LivingEntity entity = target.getLivingEntity();
            return entity.getHealth() / entity.getMaxHealth() <= 0.4;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.4, 0.008, skillLevel);
   }

   public static double applyExecuteBonus(Player player, EliteMobDamagedByPlayerEvent event, double damage) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return damage;
      } else {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            LivingEntity entity = target.getLivingEntity();
            if (entity.getHealth() / entity.getMaxHealth() > 0.4) {
               return damage;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
               double bonus = 0.4 + skillLevel * 0.008;
               return damage * (1.0 + bonus);
            }
         } else {
            return damage;
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
      return this.applyLoreTemplates(Map.of("executeBonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
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
      return this.applyFormattedBonusTemplate(Map.of("executeBonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
