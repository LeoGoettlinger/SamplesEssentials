package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DepthChargeSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "tridents_depth_charge";
   private static final double AOE_RADIUS = 4.0;
   private static final double BASE_DAMAGE_BONUS = 0.3;
   private static final double DAMAGE_BONUS_PER_LEVEL = 0.0025;
   private static final double BASE_AOE_DAMAGE = 0.1;
   private static final double AOE_DAMAGE_PER_LEVEL = 0.001;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public DepthChargeSkill() {
      super(
         SkillType.TRIDENTS,
         75,
         "Depth Charge",
         "When target is in water, creates an explosion that damages nearby aquatic enemies.",
         SkillBonusType.CONDITIONAL,
         4,
         "tridents_depth_charge"
      );
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         return eliteEntity != null && eliteEntity.getLivingEntity() != null ? eliteEntity.getLivingEntity().isInWater() : false;
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      double approvedCurve = scaled(0.3, 0.0025, skillLevel);
      if (this.configFields == null) {
         return approvedCurve;
      } else {
         double configuredBonus = this.configFields.calculateValue(skillLevel);
         return !Double.isFinite(configuredBonus) ? approvedCurve : Math.max(0.0, Math.min(configuredBonus, approvedCurve));
      }
   }

   @Override
   public void onConditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (this.isActive(player)) {
            EliteEntity eliteEntity = event.getEliteMobEntity();
            if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
               LivingEntity target = eliteEntity.getLivingEntity();
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
               target.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, target.getLocation(), 50, 2.0, 2.0, 2.0, 0.1);
               double aoeDamage = event.getDamage() * this.calculateAoeDamage(skillLevel);
               target.getNearbyEntities(4.0, 4.0, 4.0)
                  .stream()
                  .filter(entity -> entity instanceof LivingEntity livingEntity && !(entity instanceof Player) && livingEntity.isInWater())
                  .forEach(entity -> CombatDamageContext.runPlayerToEliteBypass(() -> ((LivingEntity)entity).damage(aoeDamage, player)));
            }
         }
      }
   }

   private double calculateAoeDamage(int skillLevel) {
      return scaled(0.1, 0.001, skillLevel);
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
      double damageBonus = this.getConditionalBonus(skillLevel) * 100.0;
      double aoeDamage = this.calculateAoeDamage(skillLevel) * 100.0;
      return this.applyLoreTemplates(
         Map.of("damageBonus", String.format("%.1f", damageBonus), "aoeDamage", String.format("%.1f", aoeDamage), "radius", String.valueOf(4.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damageBonus", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
