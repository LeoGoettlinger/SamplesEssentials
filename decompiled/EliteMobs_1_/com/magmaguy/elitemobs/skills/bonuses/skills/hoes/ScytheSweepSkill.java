package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ScytheSweepSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "hoes_scythe_sweep";
   private static final double BASE_PROC_CHANCE = 0.17;
   private static final double SWEEP_RADIUS = 3.0;
   private static final double BASE_SWEEP_MULTIPLIER = 0.45;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ScytheSweepSkill() {
      super(SkillType.HOES, 50, "Scythe Sweep", "Attacks have a chance to sweep and hit nearby enemies.", SkillBonusType.PROC, 3, "hoes_scythe_sweep");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.17, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            double originalDamage = event.getDamage();
            int skillLevel = this.getPlayerSkillLevel(player);
            double sweepDamage = originalDamage * this.calculateSweepMultiplier(skillLevel);
            target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0.0, 0.5, 0.0), 3, 1.0, 0.2, 1.0, 0.0);
            target.getNearbyEntities(3.0, 3.0, 3.0)
               .stream()
               .filter(e -> e instanceof LivingEntity && !(e instanceof Player) && !e.equals(target))
               .forEach(e -> CombatDamageContext.runPlayerToEliteBypass(() -> ((LivingEntity)e).damage(sweepDamage, player)));
         }
      }
   }

   private double calculateSweepMultiplier(int skillLevel) {
      return scaled(0.45, 0.006, skillLevel);
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
            "sweepPercent",
            String.format("%.0f", this.calculateSweepMultiplier(skillLevel) * 100.0),
            "radius",
            String.format("%.1f", 3.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateSweepMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("sweepPercent", String.format("%.0f", this.calculateSweepMultiplier(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
