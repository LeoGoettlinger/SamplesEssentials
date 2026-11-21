package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SoulDrainSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "hoes_soul_drain";
   private static final double BASE_PROC_CHANCE = 0.2;
   private static final double BASE_DRAIN_MULTIPLIER = 0.1;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public SoulDrainSkill() {
      super(SkillType.HOES, 10, "Soul Drain", "Attacks have a chance to drain health from enemies.", SkillBonusType.PROC, 1, "hoes_soul_drain");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.2, 0.002, 0.5, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            double damage = event.getDamage();
            int skillLevel = this.getPlayerSkillLevel(player);
            double drainAmount = damage * this.calculateDrainMultiplier(skillLevel);
            drainAmount = Math.min(drainAmount, player.getMaxHealth() * 0.1);
            double newHealth = Math.min(player.getHealth() + drainAmount, player.getMaxHealth());
            player.setHealth(newHealth);
            eliteEntity.getLivingEntity()
               .getWorld()
               .spawnParticle(Particle.SOUL, eliteEntity.getLivingEntity().getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.05);
         }
      }
   }

   private double calculateDrainMultiplier(int skillLevel) {
      return this.configFields != null ? Math.min(1.0, Math.max(0.0, this.configFields.calculateValue(skillLevel))) : scaled(0.1, 0.002, skillLevel);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
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
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "drainPercent",
            String.format("%.1f", this.calculateDrainMultiplier(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateDrainMultiplier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("drainPercent", String.format("%.1f", this.calculateDrainMultiplier(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
