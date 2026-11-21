package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FirstStrikeSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "spears_first_strike";
   private static final double BASE_DAMAGE_BONUS = 0.83;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public FirstStrikeSkill() {
      super(SkillType.SPEARS, 10, "First Strike", "Deal bonus damage to full-health enemies.", SkillBonusType.CONDITIONAL, 1, "spears_first_strike");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            return target.getHealth() >= target.getMaxHealth();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.getDamageBonus(skillLevel);
   }

   public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
      if (!this.isActive(player)) {
         return 1.0;
      } else {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            if (target.getHealth() < target.getMaxHealth()) {
               return 1.0;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
               double bonus = this.getDamageBonus(skillLevel);
               target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0.0, 1.5, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
               player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5F, 1.5F);
               return 1.0 + bonus;
            }
         } else {
            return 1.0;
         }
      }
   }

   public double getDamageBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.83, 0.01, skillLevel);
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
      return this.applyLoreTemplates(Map.of("value", String.format("%.0f", this.getDamageBonus(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.0f", this.getDamageBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
