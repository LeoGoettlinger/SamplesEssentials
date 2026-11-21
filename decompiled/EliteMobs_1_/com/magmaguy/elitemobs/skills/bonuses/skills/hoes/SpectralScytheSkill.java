package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpectralScytheSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "hoes_spectral_scythe";
   private static final long BASE_COOLDOWN = 30L;
   private static final double PROJECTILE_RANGE = 10.0;
   private static final double BASE_DAMAGE_MULTIPLIER = 2.0;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public SpectralScytheSkill() {
      super(
         SkillType.HOES,
         75,
         "Spectral Scythe",
         "Launch a spectral scythe projectile that pierces through enemies.",
         SkillBonusType.COOLDOWN,
         4,
         "hoes_spectral_scythe"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      if (this.configFields != null && this.configFields.getCooldownSeconds() > 0.0) {
         return Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)));
      } else {
         long reduction = (long)(skillLevel * 0.2);
         return Math.max(12L, 30L - reduction);
      }
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
      if (this.isOnCooldown(player)) {
         long remaining = this.getRemainingCooldown(player);
         ActionBarCompositor.show(
            player, ActionBarCompositor.Source.SKILL_FEEDBACK, DungeonsConfig.getSpectralScytheCooldownMessage().replace("$time", String.valueOf(remaining))
         );
      } else {
         int skillLevel = this.getPlayerSkillLevel(player);
         this.launchSpectralScythe(player, skillLevel);
         this.startCooldown(player, skillLevel);
      }
   }

   private void launchSpectralScythe(final Player player, int skillLevel) {
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0F, 0.5F);
      final Vector direction = player.getLocation().getDirection().normalize();
      final Location startLoc = player.getEyeLocation();
      final double baseDamage = this.calculateDamage(skillLevel);
      final Set<UUID> hitEntities = new HashSet<>();
      (new BukkitRunnable() {
            Location currentLoc = startLoc.clone();
            double distance = 0.0;

            public void run() {
               if (this.distance > 10.0) {
                  this.cancel();
               } else {
                  this.currentLoc.add(direction.clone().multiply(0.5));
                  this.distance += 0.5;
                  this.currentLoc.getWorld().spawnParticle(Particle.SOUL, this.currentLoc, 3, 0.1, 0.1, 0.1, 0.02);
                  this.currentLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, this.currentLoc, 1, 0.1, 0.1, 0.1, 0.0);
                  this.currentLoc
                     .getWorld()
                     .getNearbyEntities(this.currentLoc, 1.0, 1.0, 1.0)
                     .stream()
                     .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                     .filter(e -> !hitEntities.contains(e.getUniqueId()))
                     .forEach(e -> {
                        LivingEntity target = (LivingEntity)e;
                        CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(baseDamage, player));
                        hitEntities.add(e.getUniqueId());
                     });
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   private double calculateDamage(int skillLevel) {
      return scaled(2.0, 0.04, skillLevel);
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
      this.endCooldown(player);
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.endCooldown(player);
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
            "damagePercent",
            String.format("%.0f", this.calculateDamage(skillLevel) * 100.0),
            "damageAmount",
            String.format("%.1f", this.calculateDamage(skillLevel)),
            "range",
            String.format("%.1f", 10.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateDamage(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "damagePercent",
            String.format("%.0f", this.calculateDamage(skillLevel) * 100.0),
            "damageAmount",
            String.format("%.1f", this.calculateDamage(skillLevel))
         )
      );
   }

   @Override
   public void shutdown() {
      cooldowns.clear();
      activePlayers.clear();
   }
}
