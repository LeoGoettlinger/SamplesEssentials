package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RicochetSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "bows_ricochet";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final double RICOCHET_RANGE = 5.0;
   private static final double BASE_RICOCHET_DAMAGE = 0.55;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public RicochetSkill() {
      super(SkillType.BOWS, 50, "Ricochet", "Arrows can bounce to nearby enemies.", SkillBonusType.PROC, 3, "bows_ricochet");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.002, 0.35, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            LivingEntity target = event.getEliteMobEntity().getLivingEntity();
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
            double ricochetDamage = event.getDamage() * this.getRicochetDamageMultiplier(skillLevel);
            target.getNearbyEntities(5.0, 5.0, 5.0)
               .stream()
               .filter(e -> e instanceof LivingEntity && !e.equals(target) && !(e instanceof Player))
               .map(e -> (LivingEntity)e)
               .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(target.getLocation())))
               .ifPresent(bounceTarget -> {
                  CombatDamageContext.runPlayerToEliteBypass(() -> bounceTarget.damage(ricochetDamage, player));
                  bounceTarget.getWorld().spawnParticle(Particle.CRIT, bounceTarget.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
               });
         }
      }
   }

   private double getRicochetDamageMultiplier(int skillLevel) {
      return scaled(0.55, 0.005, skillLevel);
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
            "ricochetDamage",
            String.format("%.0f", this.getRicochetDamageMultiplier(skillLevel) * 100.0),
            "range",
            String.valueOf(5)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getRicochetDamageMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("ricochetDamage", String.format("%.0f", this.getRicochetDamageMultiplier(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
