package com.magmaguy.elitemobs.skills.bonuses.skills.magic;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class MagicWeaponSkill extends SkillBonus {
   private final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private final Map<UUID, Long> cooldownEnds = new ConcurrentHashMap<>();

   protected MagicWeaponSkill(SkillType skillType, int unlockTier, String name, String description, SkillBonusType bonusType, String skillId) {
      super(skillType, getLevelForTier(unlockTier), name, description, bonusType, unlockTier, skillId);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      this.activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.deactivate(player);
   }

   @Override
   public void onActivate(Player player) {
      this.activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.deactivate(player);
   }

   @Override
   public boolean isActive(Player player) {
      return this.activePlayers.contains(player.getUniqueId());
   }

   private void deactivate(Player player) {
      this.activePlayers.remove(player.getUniqueId());
      this.clearPlayer(player.getUniqueId());
   }

   protected void clearPlayer(UUID playerId) {
   }

   @Override
   public void shutdown() {
      this.activePlayers.clear();
      this.cooldownEnds.clear();
   }

   protected int skillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, this.skillType);
   }

   protected static EliteMobDamagedByPlayerEvent strike(Object context, MagicStrike strike) {
      return context instanceof EliteMobDamagedByPlayerEvent event && event.getMagicStrike() == strike ? event : null;
   }

   protected boolean wieldedBy(Player player) {
      if (this.isEnabled() && this.isActive(player)) {
         return WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) != this.skillType
            ? false
            : this.meetsLevelRequirement(this.skillLevel(player));
      } else {
         return false;
      }
   }

   protected static List<LivingEntity> enemiesNear(Player player, Location center, double radius, LivingEntity exclude) {
      World world = center.getWorld();
      if (world == null) {
         return List.of();
      } else {
         List<LivingEntity> enemies = new ArrayList<>();

         for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity living
               && !living.equals(exclude)
               && !living.isDead()
               && !(living.getLocation().distanceSquared(center) > radius * radius)) {
               EliteEntity elite = EntityTracker.getEliteMobEntity(living);
               if (elite != null && elite.isValid() && AdvancedCombatEnemyAuthorization.canTargetWithMagicWeapon(player, living)) {
                  enemies.add(living);
               }
            }
         }

         enemies.sort(Comparator.comparingDouble(livingx -> livingx.getLocation().distanceSquared(center)));
         return enemies;
      }
   }

   protected static void sideDamage(Player player, LivingEntity target, double damage) {
      if (player.isOnline() && !target.isDead() && target.isValid() && Double.isFinite(damage) && !(damage <= 0.0)) {
         CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(damage, player));
      }
   }

   protected static Location chest(LivingEntity entity) {
      return entity.getLocation().add(0.0, entity.getHeight() * 0.6, 0.0);
   }

   protected static void beam(Location from, Location to, Particle particle) {
      World world = from.getWorld();
      if (world != null && world.equals(to.getWorld())) {
         Vector step = to.toVector().subtract(from.toVector());
         double length = step.length();
         if (!(length < 0.01)) {
            int points = (int)Math.min(40.0, Math.ceil(length / 0.4));
            step.multiply(1.0 / points);
            Location point = from.clone();

            for (int i = 0; i <= points; i++) {
               world.spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0);
               point.add(step);
            }
         }
      }
   }

   public long getCooldownSeconds(int skillLevel) {
      return 0L;
   }

   public boolean isOnCooldown(Player player) {
      Long end = this.cooldownEnds.get(player.getUniqueId());
      if (end == null) {
         return false;
      } else if (System.currentTimeMillis() < end) {
         return true;
      } else {
         this.cooldownEnds.remove(player.getUniqueId());
         return false;
      }
   }

   public void startCooldown(Player player, int skillLevel) {
      this.cooldownEnds.put(player.getUniqueId(), System.currentTimeMillis() + this.getCooldownSeconds(skillLevel) * 1000L);
   }

   public long getRemainingCooldown(Player player) {
      Long end = this.cooldownEnds.get(player.getUniqueId());
      return end == null ? 0L : Math.max(0L, (end - System.currentTimeMillis()) / 1000L);
   }

   public void endCooldown(Player player) {
      this.cooldownEnds.remove(player.getUniqueId());
   }
}
