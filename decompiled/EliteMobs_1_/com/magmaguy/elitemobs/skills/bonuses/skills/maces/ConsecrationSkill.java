package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ConsecrationSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "maces_consecration";
   private static final long BASE_COOLDOWN_SECONDS = 25L;
   private static final double AOE_RADIUS = 4.0;
   private static final int DURATION_TICKS = 100;
   private static final int TICK_INTERVAL = 20;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ConsecrationSkill() {
      super(SkillType.MACES, 50, "Consecration", "Create holy ground that damages enemies within.", SkillBonusType.COOLDOWN, 3, "maces_consecration");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(15L, 25L - skillLevel / 10);
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
      this.activateConsecration(player);
   }

   public void activateConsecration(final Player player) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
         final double damagePerTick = this.getDamagePerTick(skillLevel);
         final Location center = player.getLocation();
         player.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.2F);
         (new BukkitRunnable() {
            int ticksRemaining = 100;

            public void run() {
               if (this.ticksRemaining > 0 && player.isOnline()) {
                  for (int i = 0; i < 20; i++) {
                     double angle = Math.random() * 2.0 * Math.PI;
                     double radius = Math.random() * 4.0;
                     double x = center.getX() + radius * Math.cos(angle);
                     double z = center.getZ() + radius * Math.sin(angle);
                     Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                     center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0.0, 0.1, 0.0, 0.0);
                  }

                  if (this.ticksRemaining % 20 == 0) {
                     for (Entity entity : center.getWorld().getNearbyEntities(center, 4.0, 3.0, 4.0)) {
                        if (entity instanceof LivingEntity living && !entity.equals(player)) {
                           EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                           if (eliteEntity != null) {
                              CombatDamageContext.runPlayerToEliteBypass(() -> living.damage(damagePerTick, player));
                              living.getWorld().spawnParticle(Particle.FLAME, living.getLocation().add(0.0, 1.0, 0.0), 5, 0.2, 0.2, 0.2, 0.02);
                           }
                        }
                     }
                  }

                  this.ticksRemaining--;
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         this.startCooldown(player, skillLevel);
      }
   }

   public double getDamagePerTick(int skillLevel) {
      return scaled(1.4, 0.02, skillLevel);
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
            "damagePerSecond",
            String.format("%.1f", this.getDamagePerTick(skillLevel)),
            "radius",
            String.valueOf(4.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamagePerTick(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("damagePerSecond", String.format("%.1f", this.getDamagePerTick(skillLevel)), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldowns.clear();
   }
}
