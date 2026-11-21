package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PrecisionThrustSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "spears_precision_thrust";
   private static final double BASE_PROC_CHANCE = 0.1;
   private static final double BASE_CRIT_MULTIPLIER = 1.5;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public PrecisionThrustSkill() {
      super(SkillType.SPEARS, 10, "Precision Thrust", "Chance to deal critical damage.", SkillBonusType.PROC, 1, "spears_precision_thrust");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.1, 0.002, 0.3, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0.0, 1.0, 0.0), 5, 0.2, 0.2, 0.2, 0.1);
            target.getWorld().spawnParticle(Particle.ENCHANTED_HIT, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.2);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.2F);
         }
      }
   }

   public double getCritMultiplier(int skillLevel) {
      return this.configFields != null ? Math.min(3.0, this.configFields.calculateValue(skillLevel)) : scaled(1.5, 0.01, 3.0, skillLevel);
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
      return this.applyLoreTemplates(
         Map.of(
            "chance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "critDamage",
            String.format("%.0f", this.getCritMultiplier(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getCritMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "chance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "critDamage",
            String.format("%.0f", this.getCritMultiplier(skillLevel) * 100.0),
            "critBonus",
            String.format("%.0f", (this.getCritMultiplier(skillLevel) - 1.0) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
