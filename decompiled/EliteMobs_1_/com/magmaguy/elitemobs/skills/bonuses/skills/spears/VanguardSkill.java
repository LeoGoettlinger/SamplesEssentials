package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VanguardSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "spears_vanguard";
   private static final long BASE_COOLDOWN_SECONDS = 20L;
   private static final double CHARGE_DISTANCE = 10.0;
   private static final double CHARGE_WIDTH = 2.0;
   private static final int CHARGE_DURATION_TICKS = 10;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public VanguardSkill() {
      super(SkillType.SPEARS, 50, "Vanguard", "Charge forward, damaging enemies in your path.", SkillBonusType.COOLDOWN, 3, "spears_vanguard");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(12L, 20L - skillLevel / 8);
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
      this.activateVanguard(player);
   }

   public void activateVanguard(final Player player) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
         final double damageMultiplier = this.getDamageMultiplier(skillLevel);
         final Vector direction = player.getLocation().getDirection().setY(0).normalize();
         final Set<UUID> hitEntities = new HashSet<>();
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0F, 1.2F);
         (new BukkitRunnable() {
            int ticks = 0;
            double distanceTraveled = 0.0;

            public void run() {
               if (this.ticks < 10 && !(this.distanceTraveled >= 10.0)) {
                  double moveDistance = 1.0;
                  Vector movement = direction.clone().multiply(moveDistance);
                  Location newLoc = player.getLocation().add(movement);
                  if (newLoc.getBlock().isPassable()) {
                     player.setVelocity(movement.multiply(2));
                     player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 5, 0.3, 0.1, 0.3, 0.02);
                  }

                  for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 2.0, 2.0, 2.0)) {
                     if (entity instanceof LivingEntity living && !entity.equals(player) && !hitEntities.contains(entity.getUniqueId())) {
                        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                        if (eliteEntity != null) {
                           double baseDamage = player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
                           double damage = baseDamage * damageMultiplier;
                           CombatDamageContext.runPlayerToEliteBypass(() -> living.damage(damage, player));
                           hitEntities.add(entity.getUniqueId());
                           living.setVelocity(direction.clone().multiply(0.5).setY(0.3));
                           living.getWorld().spawnParticle(Particle.SWEEP_ATTACK, living.getLocation().add(0.0, 1.0, 0.0), 1, 0.0, 0.0, 0.0, 0.0);
                        }
                     }
                  }

                  this.distanceTraveled += moveDistance;
                  this.ticks++;
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         this.startCooldown(player, skillLevel);
      }
   }

   public double getDamageMultiplier(int skillLevel) {
      return scaled(1.0, 0.033, skillLevel);
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
      return this.applyLoreTemplates(
         Map.of(
            "damage",
            String.format("%.0f", this.getDamageMultiplier(skillLevel) * 100.0),
            "distance",
            String.valueOf(10),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("damage", String.format("%.0f", this.getDamageMultiplier(skillLevel) * 100.0), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldowns.clear();
   }
}
