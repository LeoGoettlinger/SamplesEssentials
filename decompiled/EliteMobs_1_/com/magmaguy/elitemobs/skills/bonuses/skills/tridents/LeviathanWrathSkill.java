package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class LeviathanWrathSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "tridents_leviathan_wrath";
   private static final long BASE_COOLDOWN = 30L;
   private static final double AOE_RADIUS = 6.0;
   private static final double BASE_DAMAGE_MULTIPLIER = 3.0;
   private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public LeviathanWrathSkill() {
      super(
         SkillType.TRIDENTS,
         75,
         "Leviathan's Wrath",
         "Massive AOE water attack with lightning strikes. Deals massive damage to all nearby enemies.",
         SkillBonusType.COOLDOWN,
         4,
         "tridents_leviathan_wrath"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(15L, 30L - skillLevel / 5);
   }

   @Override
   public boolean isOnCooldown(Player player) {
      Long endTime = cooldownMap.get(player.getUniqueId());
      if (endTime == null) {
         return false;
      } else if (System.currentTimeMillis() >= endTime) {
         cooldownMap.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      long cooldownMs = this.getCooldownSeconds(skillLevel) * 1000L;
      cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long endTime = cooldownMap.get(player.getUniqueId());
      if (endTime == null) {
         return 0L;
      } else {
         long remaining = (endTime - System.currentTimeMillis()) / 1000L;
         return Math.max(0L, remaining);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldownMap.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (!this.isOnCooldown(player)) {
            EliteEntity eliteEntity = damageEvent.getEliteMobEntity();
            if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
               LivingEntity target = eliteEntity.getLivingEntity();
               int skillLevel = this.getPlayerSkillLevel(player);
               target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 200, 3.0, 2.0, 3.0, 0.5);
               target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0F, 0.5F);
               if (!CombatSimulator.isTestingActive(player)) {
                  for (int i = 0; i < 3; i++) {
                     target.getWorld().strikeLightningEffect(target.getLocation().add((Math.random() - 0.5) * 6.0, 0.0, (Math.random() - 0.5) * 6.0));
                  }
               }

               double aoeDamage = damageEvent.getDamage() * this.calculateDamageMultiplier(skillLevel);
               target.getNearbyEntities(6.0, 6.0, 6.0)
                  .stream()
                  .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                  .forEach(e -> CombatDamageContext.runPlayerToEliteBypass(() -> ((LivingEntity)e).damage(aoeDamage, player)));
            }
         }
      }
   }

   private double calculateDamageMultiplier(int skillLevel) {
      return scaled(3.0, 0.04, skillLevel);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      this.endCooldown(player);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      this.endCooldown(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      long cooldown = this.getCooldownSeconds(skillLevel);
      double damageMultiplier = this.calculateDamageMultiplier(skillLevel);
      return this.applyLoreTemplates(
         Map.of("cooldown", String.valueOf(cooldown), "multiplier", String.format("%.1f", damageMultiplier), "radius", String.valueOf(6.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateDamageMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("multiplier", String.format("%.1f", this.calculateDamageMultiplier(skillLevel)), "radius", String.valueOf(6.0))
      );
   }

   @Override
   public void shutdown() {
      cooldownMap.clear();
      activePlayers.clear();
   }
}
