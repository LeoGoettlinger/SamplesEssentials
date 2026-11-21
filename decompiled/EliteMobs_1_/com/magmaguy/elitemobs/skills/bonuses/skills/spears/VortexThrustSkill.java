package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class VortexThrustSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "spears_vortex_thrust";
   private static final long BASE_COOLDOWN_SECONDS = 18L;
   private static final double PULL_RADIUS = 5.0;
   private static final double PULL_STRENGTH = 0.8;
   private static final int SLOW_DURATION_TICKS = 40;
   private static final int SLOW_AMPLIFIER = 1;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public VortexThrustSkill() {
      super(
         SkillType.SPEARS, 25, "Vortex Thrust", "Pull nearby enemies toward your target, grouping them up.", SkillBonusType.COOLDOWN, 2, "spears_vortex_thrust"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(10L, 18L - skillLevel / 10);
   }

   @Override
   public boolean isOnCooldown(Player player) {
      Long cooldownEnd = cooldowns.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return false;
      } else if (System.currentTimeMillis() >= cooldownEnd) {
         cooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      long cooldownMs = this.getCooldownSeconds(skillLevel) * 1000L;
      cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long cooldownEnd = cooldowns.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return 0L;
      } else {
         long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
         return Math.max(0L, remaining);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldowns.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (damageEvent.getEliteMobEntity() != null) {
            LivingEntity target = damageEvent.getEliteMobEntity().getLivingEntity();
            if (target != null) {
               this.activateVortexThrust(player, target);
            }
         }
      }
   }

   public void activateVortexThrust(Player player, LivingEntity target) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
         Location targetLoc = target.getLocation();
         Location playerLoc = player.getLocation();
         double pullStrength = 0.8 + skillLevel * 0.005;
         player.getWorld().playSound(targetLoc, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0F, 1.5F);
         Vector targetPull = playerLoc.toVector().subtract(target.getLocation().toVector());
         if (targetPull.lengthSquared() >= 0.1) {
            targetPull.normalize().multiply(pullStrength);
            targetPull.setY(0.2);
            target.setVelocity(target.getVelocity().add(targetPull));
            this.spawnPullParticles(target.getLocation(), playerLoc);
         }

         for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, 5.0, 5.0, 5.0)) {
            if (entity instanceof LivingEntity living && !entity.equals(player) && !entity.equals(target) && EntityTracker.getEliteMobEntity(living) != null) {
               Vector pullDirection = targetLoc.toVector().subtract(living.getLocation().toVector());
               if (!(pullDirection.lengthSquared() < 0.1)) {
                  pullDirection.normalize().multiply(pullStrength);
                  pullDirection.setY(0.2);
                  living.setVelocity(living.getVelocity().add(pullDirection));
                  this.spawnPullParticles(living.getLocation(), targetLoc);
               }
            }
         }

         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, true, true));
         targetLoc.getWorld().spawnParticle(Particle.PORTAL, targetLoc.add(0.0, 1.0, 0.0), 40, 0.5, 0.5, 0.5, 0.5);
         targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 30, 2.0, 1.0, 2.0, 0.1);
         this.startCooldown(player, skillLevel);
      }
   }

   private void spawnPullParticles(Location from, Location to) {
      Vector direction = to.toVector().subtract(from.toVector());
      double distance = direction.length();
      if (!(distance < 0.5)) {
         direction.normalize();

         for (double d = 0.0; d < distance; d += 0.8) {
            Location point = from.clone().add(direction.clone().multiply(d)).add(0.0, 1.0, 0.0);
            point.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 2, 0.1, 0.1, 0.1, 0.0);
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
      cooldowns.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("radius", String.valueOf(5), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel))));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return 0.0;
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("cooldown", String.valueOf(this.getCooldownSeconds(skillLevel))));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldowns.clear();
   }
}
