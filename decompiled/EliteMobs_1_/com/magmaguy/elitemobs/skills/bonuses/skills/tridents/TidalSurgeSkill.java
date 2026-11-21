package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TidalSurgeSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "tridents_tidal_surge";
   private static final long BASE_COOLDOWN = 20L;
   private static final double SURGE_RADIUS = 5.0;
   private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public TidalSurgeSkill() {
      super(
         SkillType.TRIDENTS,
         25,
         "Tidal Surge",
         "Creates a water surge that knocks back and damages nearby enemies.",
         SkillBonusType.COOLDOWN,
         2,
         "tridents_tidal_surge"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(10L, 20L - skillLevel / 10);
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
               this.startCooldown(player, skillLevel);
               target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 100, 2.0, 1.0, 2.0, 0.1);
               double knockbackStrength = this.calculateKnockback(skillLevel);
               double aoeDamageMultiplier = this.calculateAoeDamage(skillLevel);
               target.getNearbyEntities(5.0, 5.0, 5.0).stream().filter(e -> e instanceof LivingEntity && !(e instanceof Player)).forEach(e -> {
                  Vector direction = e.getLocation().toVector().subtract(target.getLocation().toVector());
                  if (direction.lengthSquared() < 0.001) {
                     direction = new Vector(Math.random() - 0.5, 0.0, Math.random() - 0.5);
                  }

                  direction.normalize().multiply(knockbackStrength);
                  direction.setY(0.5);
                  e.setVelocity(direction);
                  CombatDamageContext.runPlayerToEliteBypass(() -> ((LivingEntity)e).damage(damageEvent.getDamage() * aoeDamageMultiplier, player));
               });
            }
         }
      }
   }

   private double calculateKnockback(int skillLevel) {
      return scaled(1.5, 0.02, skillLevel);
   }

   private double calculateAoeDamage(int skillLevel) {
      return scaled(0.3, 0.006, skillLevel);
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
      double knockback = this.calculateKnockback(skillLevel);
      return this.applyLoreTemplates(
         Map.of(
            "cooldown",
            String.valueOf(cooldown),
            "knockback",
            String.format("%.1f", knockback),
            "radius",
            String.valueOf(5.0),
            "aoeDamage",
            String.format("%.0f", this.calculateAoeDamage(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateKnockback(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("knockback", String.format("%.1f", this.calculateKnockback(skillLevel)), "radius", String.valueOf(5.0)));
   }

   @Override
   public void shutdown() {
      cooldownMap.clear();
      activePlayers.clear();
   }
}
